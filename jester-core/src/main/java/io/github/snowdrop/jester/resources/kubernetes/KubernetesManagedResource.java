package io.github.snowdrop.jester.resources.kubernetes;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.github.snowdrop.jester.api.clients.KubernetesClient;
import io.github.snowdrop.jester.configuration.KubernetesServiceConfiguration;
import io.github.snowdrop.jester.configuration.KubernetesServiceConfigurationBuilder;
import io.github.snowdrop.jester.core.ManagedResource;
import io.github.snowdrop.jester.core.ServiceContext;
import io.github.snowdrop.jester.core.extensions.KubernetesExtensionBootstrap;
import io.github.snowdrop.jester.logging.KubernetesLoggingHandler;
import io.github.snowdrop.jester.logging.LoggingHandler;
import io.github.snowdrop.jester.utils.DeploymentResourceUtils;
import io.github.snowdrop.jester.utils.FileUtils;

public abstract class KubernetesManagedResource extends ManagedResource {

    private KubernetesClient client;
    private LoggingHandler loggingHandler;
    private boolean init;
    private boolean running;

    protected abstract String getImage();

    protected abstract String getExpectedLog();

    protected abstract int[] getPorts();

    @Override
    public String getDisplayName() {
        return getImage();
    }

    @Override
    public void start() {
        if (running) {
            return;
        }

        if (!init) {
            doInit();
            init = true;
        } else {
            doUpdate();
        }

        client.scaleTo(context.getOwner(), 1);
        running = true;

        loggingHandler = new KubernetesLoggingHandler(context);
        loggingHandler.startWatching();
    }

    @Override
    public void stop() {
        if (loggingHandler != null) {
            loggingHandler.stopWatching();
        }

        client.stopService(context.getOwner());
        running = false;
    }

    @Override
    public String getHost() {
        if (useInternalServiceAsUrl()) {
            return context.getName();
        }

        return client.host(context.getOwner());
    }

    @Override
    public int getFirstMappedPort() {
        return getMappedPort(getPorts()[0]);
    }

    @Override
    public int getMappedPort(int port) {
        if (useInternalServiceAsUrl()) {
            return port;
        }

        return client.port(context.getOwner(), port);
    }

    @Override
    public boolean isRunning() {
        return loggingHandler != null && loggingHandler.logsContains(getExpectedLog());
    }

    @Override
    public List<String> logs() {
        return loggingHandler.logs();
    }

    @Override
    protected LoggingHandler getLoggingHandler() {
        return loggingHandler;
    }

    @Override
    protected void init(ServiceContext context) {
        super.init(context);
        context.loadCustomConfiguration(KubernetesServiceConfiguration.class,
                new KubernetesServiceConfigurationBuilder());
    }

    protected String[] getCommand() {
        return null;
    }

    protected void doInit() {
        this.client = context.get(KubernetesExtensionBootstrap.CLIENT);

        applyDeployment();

        client.expose(context.getOwner(), getEffectivePorts());
    }

    protected void doUpdate() {
        applyDeployment();
    }

    protected Optional<Deployment> loadDeploymentFromFolder() {
        return Optional.empty();
    }

    private void applyDeployment() {
        Deployment deployment = loadDeploymentFromConfiguration().or(this::loadDeploymentFromFolder)
                .orElseGet(Deployment::new);

        DeploymentResourceUtils.adaptDeployment(context, client, deployment, getImage(), getCommand(),
                getEffectivePorts(), getServiceAccount());
    }

    private Optional<Deployment> loadDeploymentFromConfiguration() {
        return Optional.ofNullable(context.getConfigurationAs(KubernetesServiceConfiguration.class).getTemplate())
                .filter(StringUtils::isNotEmpty).map(FileUtils::loadFile)
                .map(DeploymentResourceUtils::loadDeploymentFromString);
    }

    private boolean useInternalServiceAsUrl() {
        return context.getConfigurationAs(KubernetesServiceConfiguration.class).isUseInternalService();
    }

    private String getServiceAccount() {
        return context.getConfigurationAs(KubernetesServiceConfiguration.class).getServiceAccount();
    }

    private int[] getEffectivePorts() {
        int[] appPorts = getPorts();
        int[] additionalPorts = context.getConfigurationAs(KubernetesServiceConfiguration.class).getAdditionalPorts();
        if (additionalPorts == null) {
            return appPorts;
        }

        int[] result = new int[appPorts.length + additionalPorts.length];
        System.arraycopy(appPorts, 0, result, 0, appPorts.length);
        System.arraycopy(additionalPorts, 0, result, appPorts.length, additionalPorts.length);
        return result;
    }

}
