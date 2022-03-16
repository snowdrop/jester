package io.jcloud.core.extensions;

import static io.jcloud.api.clients.KubectlClient.ENABLED_EPHEMERAL_NAMESPACES;

import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import io.jcloud.api.RunOnKubernetes;
import io.jcloud.api.clients.KubectlClient;
import io.jcloud.api.extensions.ExtensionBootstrap;
import io.jcloud.configuration.PropertyLookup;
import io.jcloud.core.ScenarioContext;
import io.jcloud.core.ServiceContext;
import io.jcloud.logging.Log;
import io.jcloud.utils.FileUtils;

public class KubernetesExtensionBootstrap implements ExtensionBootstrap {
    public static final String CLIENT = "kubectl-client";

    private static final PropertyLookup DELETE_NAMESPACE_AFTER = new PropertyLookup(
            "ts.kubernetes.delete.namespace.after.all", Boolean.TRUE.toString());

    private KubectlClient client;

    @Override
    public boolean appliesFor(ScenarioContext context) {
        boolean isValidConfig = context.isAnnotationPresent(RunOnKubernetes.class);
        if (isValidConfig && !DELETE_NAMESPACE_AFTER.getAsBoolean() && ENABLED_EPHEMERAL_NAMESPACES.getAsBoolean()) {
            Log.error("-Dts.kubernetes.delete.project.after.all=false is only supported with"
                    + " -Dts.kubernetes.ephemeral.namespaces.enabled=false");
            isValidConfig = false;
        }

        return isValidConfig;
    }

    @Override
    public void beforeAll(ScenarioContext context) {
        // if deleteNamespace and ephemeral namespaces are disabled then we are in debug mode. This mode is going to
        // keep
        // all scenario resources in order to allow you to debug by yourself
        context.setDebug(!DELETE_NAMESPACE_AFTER.getAsBoolean() && !ENABLED_EPHEMERAL_NAMESPACES.getAsBoolean());
        client = KubectlClient.create();
    }

    @Override
    public void afterAll(ScenarioContext context) {
        if (DELETE_NAMESPACE_AFTER.getAsBoolean()) {
            client.deleteNamespace(context.getId());
        }
    }

    @Override
    public void updateServiceContext(ServiceContext context) {
        context.put(CLIENT, client);
    }

    @Override
    public Optional<Object> getParameter(Class<?> clazz) {
        if (clazz == KubectlClient.class) {
            return Optional.of(client);
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
