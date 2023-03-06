package io.github.snowdrop.jester.core.extensions;

import static io.github.snowdrop.jester.utils.InjectUtils.getNamedValueFromDependencyContext;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.github.snowdrop.jester.api.RunOnKubernetes;
import io.github.snowdrop.jester.api.clients.KubectlClient;
import io.github.snowdrop.jester.api.extensions.ExtensionBootstrap;
import io.github.snowdrop.jester.configuration.JesterConfiguration;
import io.github.snowdrop.jester.configuration.KubernetesConfiguration;
import io.github.snowdrop.jester.configuration.KubernetesConfigurationBuilder;
import io.github.snowdrop.jester.core.DependencyContext;
import io.github.snowdrop.jester.core.JesterContext;
import io.github.snowdrop.jester.core.ServiceContext;
import io.github.snowdrop.jester.logging.Log;
import io.github.snowdrop.jester.utils.FileUtils;

public class KubernetesExtensionBootstrap implements ExtensionBootstrap {
    public static final String CLIENT = "kubectl-client";
    public static final String TARGET_KUBERNETES = "kubernetes";

    private KubectlClient client;

    @Override
    public boolean appliesFor(JesterContext context) {
        return isEnabled(context);
    }

    @Override
    public void beforeAll(JesterContext context) {
        KubernetesConfiguration configuration = context.loadCustomConfiguration(TARGET_KUBERNETES,
                new KubernetesConfigurationBuilder());

        // if deleteNamespace and ephemeral namespaces are disabled then we are in debug mode. This mode is going to
        // keep all resources in order to allow you to debug by yourself
        context.setDebug(!configuration.isDeleteNamespaceAfterAll() && !configuration.isEphemeralNamespaceEnabled());

        client = new KubectlClient();
        if (configuration.isEphemeralNamespaceEnabled()) {
            client.initializeClientUsingANewNamespace();
        } else {
            client.initializeClientUsingNamespace(new DefaultKubernetesClient().getNamespace());
        }

        if (configuration.getAdditionalResources() != null) {
            for (String additionalResource : configuration.getAdditionalResources()) {
                client.apply(Path.of(additionalResource));
            }
        }
    }

    @Override
    public void afterAll(JesterContext context) {
        KubernetesConfiguration configuration = context.getConfigurationAs(KubernetesConfiguration.class);
        if (configuration.isDeleteNamespaceAfterAll()) {
            if (configuration.isEphemeralNamespaceEnabled()) {
                client.deleteNamespace();
            } else {
                client.deleteResourcesInJesterContext(context.getId());
            }
        }
    }

    @Override
    public void updateServiceContext(ServiceContext context) {
        context.put(CLIENT, client);
    }

    @Override
    public List<Class<?>> supportedParameters() {
        return Arrays.asList(KubectlClient.class, KubernetesClient.class, Deployment.class,
                io.fabric8.kubernetes.api.model.Service.class, Ingress.class);
    }

    @Override
    public Optional<Object> getParameter(DependencyContext dependency) {
        if (dependency.getType() == KubectlClient.class) {
            return Optional.of(client);
        } else if (dependency.getType() == KubernetesClient.class) {
            return Optional.of(client.underlyingClient());
        } else {
            // named parameters
            String named = getNamedValueFromDependencyContext(dependency);
            if (named == null) {
                throw new RuntimeException(
                        "To inject Kubernetes resources, need to provide the name using @Named. Problematic field: "
                                + dependency.getName());
            }

            if (dependency.getType() == Deployment.class) {
                return Optional.of(client.underlyingClient().apps().deployments().withName(named).get());
            } else if (dependency.getType() == io.fabric8.kubernetes.api.model.Service.class) {
                return Optional.of(client.underlyingClient().services().withName(named).get());
            } else if (dependency.getType() == Ingress.class) {
                return Optional.of(client.underlyingClient().network().ingresses().withName(named).get());
            }
        }

        return Optional.empty();
    }

    @Override
    public void onError(JesterContext context, Throwable throwable) {
        if (context.getConfigurationAs(KubernetesConfiguration.class).isPrintInfoOnError()) {
            Log.error("Test " + context.getRunningTestClassAndMethodName()
                    + " failed. Printing diagnosis information from Kubernetes... ");

            FileUtils.createDirectoryIfDoesNotExist(logsTestFolder(context));
            printEvents(context);
            printPodLogs(context);
        }
    }

    private void printEvents(JesterContext context) {
        String events = client.getEvents();
        FileUtils.copyContentTo(events, logsTestFolder(context).resolve("events" + Log.LOG_SUFFIX));
        Log.error(events);
    }

    private void printPodLogs(JesterContext context) {
        Map<String, String> logs = client.logs();
        for (Entry<String, String> podLog : logs.entrySet()) {
            FileUtils.copyContentTo(podLog.getValue(),
                    logsTestFolder(context).resolve(podLog.getKey() + Log.LOG_SUFFIX));
            Log.error("Pod[%s]: '%s'", podLog.getKey(), podLog.getValue());
        }
    }

    private Path logsTestFolder(JesterContext context) {
        return context.getLogFolder().resolve(context.getRunningTestClassName());
    }

    public static final boolean isEnabled(JesterContext context) {
        return context.isAnnotationPresent(RunOnKubernetes.class)
                || TARGET_KUBERNETES.equals(context.getConfigurationAs(JesterConfiguration.class).getTarget());
    }
}
