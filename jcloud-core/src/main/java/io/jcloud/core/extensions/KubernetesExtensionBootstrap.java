package io.jcloud.core.extensions;

import static io.jcloud.api.clients.KubectlClient.ENABLED_EPHEMERAL_NAMESPACES;

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
import io.jcloud.core.DependencyContext;
import io.jcloud.core.ScenarioContext;
import io.jcloud.core.ServiceContext;
import io.jcloud.logging.Log;
import io.jcloud.utils.FileUtils;

public class KubernetesExtensionBootstrap implements ExtensionBootstrap {
    public static final String CLIENT = "kubectl-client";

    private static final boolean DELETE_NAMESPACE_AFTER = Boolean
            .parseBoolean(System.getProperty("ts.kubernetes.delete.namespace.after.all", Boolean.TRUE.toString()));

    private KubectlClient client;

    @Override
    public boolean appliesFor(ScenarioContext context) {
        return context.isAnnotationPresent(RunOnKubernetes.class);
    }

    @Override
    public void beforeAll(ScenarioContext context) {
        // if deleteNamespace and ephemeral namespaces are disabled then we are in debug mode. This mode is going to
        // keep all scenario resources in order to allow you to debug by yourself
        context.setDebug(!DELETE_NAMESPACE_AFTER && !ENABLED_EPHEMERAL_NAMESPACES);
        client = KubectlClient.create();
    }

    @Override
    public void afterAll(ScenarioContext context) {
        if (DELETE_NAMESPACE_AFTER) {
            client.deleteNamespace(context.getId());
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
        Map<String, String> logs = client.logs();
        for (Entry<String, String> podLog : logs.entrySet()) {
            FileUtils.copyContentTo(podLog.getValue(),
                    logsTestFolder(context).resolve(podLog.getKey() + Log.LOG_SUFFIX));
        }
    }

    private Path logsTestFolder(ScenarioContext context) {
        return context.getLogFolder().resolve(context.getRunningTestClassName());
    }
}
