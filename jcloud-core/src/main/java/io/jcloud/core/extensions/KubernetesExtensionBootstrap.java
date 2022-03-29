package io.jcloud.core.extensions;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.inject.Named;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.jcloud.api.RunOnKubernetes;
import io.jcloud.api.clients.KubectlClient;
import io.jcloud.api.extensions.ExtensionBootstrap;
import io.jcloud.configuration.KubernetesConfiguration;
import io.jcloud.configuration.KubernetesConfigurationBuilder;
import io.jcloud.configuration.ScenarioConfiguration;
import io.jcloud.core.DependencyContext;
import io.jcloud.core.ScenarioContext;
import io.jcloud.core.ServiceContext;
import io.jcloud.logging.Log;
import io.jcloud.utils.FileUtils;

public class KubernetesExtensionBootstrap implements ExtensionBootstrap {
    public static final String CLIENT = "kubectl-client";
    public static final String TARGET_KUBERNETES = "kubernetes";

    private KubectlClient client;

    @Override
    public boolean appliesFor(ScenarioContext context) {
        return context.isAnnotationPresent(RunOnKubernetes.class)
                || TARGET_KUBERNETES.equals(context.getConfigurationAs(ScenarioConfiguration.class).getTarget());
    }

    @Override
    public void beforeAll(ScenarioContext context) {
        KubernetesConfiguration configuration = context.loadCustomConfiguration(TARGET_KUBERNETES,
                new KubernetesConfigurationBuilder());

        // if deleteNamespace and ephemeral namespaces are disabled then we are in debug mode. This mode is going to
        // keep all scenario resources in order to allow you to debug by yourself
        context.setDebug(!configuration.isDeleteNamespaceAfterAll() && !configuration.isEphemeralNamespaceEnabled());

        if (configuration.isEphemeralNamespaceEnabled()) {
            client = KubectlClient.createClientUsingANewNamespace();
        } else {
            client = KubectlClient.createClientUsingCurrentNamespace();
        }

        if (configuration.getAdditionalResources() != null) {
            for (String additionalResource : configuration.getAdditionalResources()) {
                client.apply(Path.of(additionalResource));
            }
        }
    }

    @Override
    public void afterAll(ScenarioContext context) {
        KubernetesConfiguration configuration = context.getConfigurationAs(KubernetesConfiguration.class);
        if (configuration.isDeleteNamespaceAfterAll()) {
            if (configuration.isEphemeralNamespaceEnabled()) {
                client.deleteNamespace(context.getId());
            } else {
                client.deleteResourcesInScenario(context.getId());
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
            Named named = dependency.findAnnotation(Named.class)
                    .orElseThrow(() -> new RuntimeException(
                            "To inject Kubernetes resources, need to provide the name using @Named. Problematic field: "
                                    + dependency.getName()));
            if (dependency.getType() == Deployment.class) {
                return Optional.of(client.underlyingClient().apps().deployments().withName(named.value()).get());
            } else if (dependency.getType() == io.fabric8.kubernetes.api.model.Service.class) {
                return Optional.of(client.underlyingClient().services().withName(named.value()).get());
            } else if (dependency.getType() == Ingress.class) {
                return Optional.of(client.underlyingClient().network().ingresses().withName(named.value()).get());
            }
        }

        return Optional.empty();
    }

    @Override
    public void onError(ScenarioContext context, Throwable throwable) {
        if (context.getConfigurationAs(KubernetesConfiguration.class).isPrintInfoOnError()) {
            Log.error("Scenario " + context.getRunningTestClassAndMethodName()
                    + " failed. Printing diagnosis information from Kubernetes... ");

            FileUtils.createDirectoryIfDoesNotExist(logsTestFolder(context));
            printEvents(context);
            printPodLogs(context);
        }
    }

    private void printEvents(ScenarioContext context) {
        String events = client.getEvents();
        FileUtils.copyContentTo(events, logsTestFolder(context).resolve("events" + Log.LOG_SUFFIX));
        Log.error(events);
    }

    private void printPodLogs(ScenarioContext context) {
        Map<String, String> logs = client.logs();
        for (Entry<String, String> podLog : logs.entrySet()) {
            FileUtils.copyContentTo(podLog.getValue(),
                    logsTestFolder(context).resolve(podLog.getKey() + Log.LOG_SUFFIX));
            Log.error("Pod[%s]: '%s'", podLog.getKey(), podLog.getValue());
        }
    }

    private Path logsTestFolder(ScenarioContext context) {
        return context.getLogFolder().resolve(context.getRunningTestClassName());
    }
}
