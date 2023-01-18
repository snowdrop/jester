package io.jester.resources.openshift;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.ReplicationControllerBuilder;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
import io.jester.api.clients.OCClient;
import io.jester.configuration.OpenShiftServiceConfiguration;
import io.jester.configuration.OpenShiftServiceConfigurationBuilder;
import io.jester.core.ManagedResource;
import io.jester.core.ServiceContext;
import io.jester.core.extensions.OpenShiftExtensionBootstrap;
import io.jester.logging.LoggingHandler;
import io.jester.logging.OpenShiftLoggingHandler;
import io.jester.utils.FileUtils;
import io.jester.utils.ManifestsUtils;

public abstract class OpenShiftManagedResource extends ManagedResource {

    private static final String DEPLOYMENT = "openshift.yml";

    private OCClient client;
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

        loggingHandler = new OpenShiftLoggingHandler(context);
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
        return 80;
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
        context.loadCustomConfiguration(OpenShiftServiceConfiguration.class,
                new OpenShiftServiceConfigurationBuilder());
    }

    protected String[] getCommand() {
        return null;
    }

    protected void doInit() {
        this.client = context.get(OpenShiftExtensionBootstrap.CLIENT);

        // applyReplicationController();
        applyDeploymentConfig();

        client.expose(context.getOwner(), getEffectivePorts());
    }

    protected void doUpdate() {
        // applyReplicationController();
        applyDeploymentConfig();
    }

    private static ReplicationControllerBuilder initReplicationControllerBuilder() {
        return new ReplicationControllerBuilder().withNewSpec().withReplicas(1).withNewTemplate().withNewSpec()
                .addNewContainer().endContainer().endSpec().endTemplate().endSpec();
    }

    private void applyDeploymentConfig() {
        DeploymentConfigBuilder deploymentConfigBuilder = Optional
                .ofNullable(context.getConfigurationAs(OpenShiftServiceConfiguration.class).getTemplate())
                .filter(StringUtils::isNotEmpty)
                .map(f -> Serialization.unmarshal(FileUtils.loadFile(f), DeploymentConfigBuilder.class))
                .orElseGet(DeploymentConfigBuilder::new);
        // Set service data
        initDeploymentConfig(deploymentConfigBuilder);
        DeploymentConfig deploymentConfig = deploymentConfigBuilder.build();
        deploymentConfig.getSpec().getTemplate().getSpec().getSecurityContext().setRunAsNonRoot(true);
        Container container = deploymentConfig.getSpec().getTemplate().getSpec().getContainers().get(0);
        container.setName(context.getName());
        if (StringUtils.isEmpty(container.getImage())) {
            container.setImage(getImage());
        }

        if (container.getCommand() != null && container.getCommand().size() > 0 && getCommand() != null
                && getCommand().length > 0) {
            container.setCommand(Arrays.asList(getCommand()));
        }
        for (int port : getEffectivePorts()) {
            if (container.getPorts().stream().noneMatch(p -> p.getContainerPort() == port)) {
                container.getPorts().add(new ContainerPortBuilder().withContainerPort(port).build());
            }
        }

        // Enrich it
        ManifestsUtils.enrichDeployment(client.underlyingClient(), deploymentConfig, context.getOwner());

        // Apply it
        Path target = context.getServiceFolder().resolve(DEPLOYMENT);
        ManifestsUtils.writeFile(target, deploymentConfig);
        client.apply(target);
        client.rollout(deploymentConfig);
    }

    private void initDeploymentConfig(DeploymentConfigBuilder deployment) {
        if (deployment.editOrNewSpec().withReplicas(1).editOrNewTemplate().editOrNewSpec().hasContainers()) {
            deployment.editOrNewSpec().withReplicas(1).editOrNewTemplate().editOrNewSpec().editFirstContainer()
                    .editOrNewSecurityContext().withAllowPrivilegeEscalation(false).editOrNewCapabilities()
                    .addToDrop("ALL").endCapabilities().endSecurityContext().endContainer().editOrNewSecurityContext()
                    .editOrNewSeccompProfile().withType("RuntimeDefault").endSeccompProfile().withRunAsNonRoot(true)
                    .endSecurityContext().endSpec().endTemplate().endSpec();
        } else {
            deployment.editOrNewSpec().withReplicas(1).editOrNewTemplate().editOrNewSpec().addNewContainer()
                    .editOrNewSecurityContext().withAllowPrivilegeEscalation(false).editOrNewCapabilities()
                    .addToDrop("ALL").endCapabilities().endSecurityContext().endContainer().editOrNewSecurityContext()
                    .editOrNewSeccompProfile().withType("RuntimeDefault").endSeccompProfile().withRunAsNonRoot(true)
                    .endSecurityContext().endSpec().endTemplate().endSpec();
        }
    }

    private boolean useInternalServiceAsUrl() {
        return context.getConfigurationAs(OpenShiftServiceConfiguration.class).isUseInternalService();
    }

    private int[] getEffectivePorts() {
        int[] appPorts = getPorts();
        int[] additionalPorts = context.getConfigurationAs(OpenShiftServiceConfiguration.class).getAdditionalPorts();
        if (additionalPorts == null) {
            return appPorts;
        }

        int[] result = new int[appPorts.length + additionalPorts.length];
        System.arraycopy(appPorts, 0, result, 0, appPorts.length);
        System.arraycopy(additionalPorts, 0, result, appPorts.length, additionalPorts.length);
        return result;
    }

}
