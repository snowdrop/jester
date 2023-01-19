package io.jester.core.extensions;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.inject.Named;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;
import io.jester.api.RunOnOpenShift;
import io.jester.api.clients.OpenshiftClient;
import io.jester.api.extensions.ExtensionBootstrap;
import io.jester.configuration.JesterConfiguration;
import io.jester.configuration.OpenShiftConfiguration;
import io.jester.configuration.OpenShiftConfigurationBuilder;
import io.jester.core.DependencyContext;
import io.jester.core.JesterContext;
import io.jester.core.ServiceContext;
import io.jester.logging.Log;
import io.jester.utils.FileUtils;

public class OpenShiftExtensionBootstrap implements ExtensionBootstrap {
    public static final String CLIENT = "oc-client";
    public static final String TARGET_OPENSHIFT = "openshift";

    private OpenshiftClient client;

    @Override
    public boolean appliesFor(JesterContext context) {
        return isEnabled(context);
    }

    @Override
    public void beforeAll(JesterContext context) {
        OpenShiftConfiguration configuration = context.loadCustomConfiguration(TARGET_OPENSHIFT,
                new OpenShiftConfigurationBuilder());

        // if deleteNamespace and ephemeral namespaces are disabled then we are in debug mode. This mode is going to
        // keep all resources in order to allow you to debug by yourself
        context.setDebug(!configuration.isDeleteProjectAfterAll() && !configuration.isEphemeralStorageEnabled());

        if (configuration.isEphemeralStorageEnabled()) {
            client = OpenshiftClient.createClientUsingANewNamespace();
        } else {
            client = OpenshiftClient.createClientUsingCurrentNamespace();
        }

        if (configuration.getAdditionalResources() != null) {
            for (String additionalResource : configuration.getAdditionalResources()) {
                client.apply(Path.of(additionalResource));
            }
        }
    }

    @Override
    public void afterAll(JesterContext context) {
        OpenShiftConfiguration configuration = context.getConfigurationAs(OpenShiftConfiguration.class);
        if (configuration.isDeleteProjectAfterAll()) {
            if (configuration.isEphemeralStorageEnabled()) {
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

        return Arrays.asList(OpenshiftClient.class, OpenShiftClient.class, DeploymentConfig.class, Service.class,
                Route.class);
    }

    @Override
    public Optional<Object> getParameter(DependencyContext dependency) {
        if (dependency.getType() == OpenshiftClient.class) {
            return Optional.of(client);
        } else if (dependency.getType() == OpenShiftClient.class) {
            return Optional.of(client.underlyingClient());
        } else {
            // named parameters
            Named named = dependency.findAnnotation(Named.class)
                    .orElseThrow(() -> new RuntimeException(
                            "To inject Openshift resources, need to provide the name using @Named. Problematic field: "
                                    + dependency.getName()));
            if (dependency.getType() == DeploymentConfig.class) {
                return Optional.of(client.underlyingClient().deploymentConfigs().withName(named.value()).get());
            } else if (dependency.getType() == Service.class) {
                return Optional.of(client.underlyingClient().services().withName(named.value()).get());
            } else if (dependency.getType() == Route.class) {
                return Optional.of(client.underlyingClient().routes().withName(named.value()).get());
            }
        }

        return Optional.empty();
    }

    @Override
    public void onError(JesterContext context, Throwable throwable) {
        if (context.getConfigurationAs(OpenShiftConfiguration.class).isPrintInfoOnError()) {
            Log.error("Test " + context.getRunningTestClassAndMethodName()
                    + " failed. Printing diagnosis information from Openshift... ");
            Log.error("Test " + throwable + ": " + Arrays.toString(throwable.getStackTrace()));

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
        return context.isAnnotationPresent(RunOnOpenShift.class)
                || TARGET_OPENSHIFT.equals(context.getConfigurationAs(JesterConfiguration.class).getTarget());
    }
}
