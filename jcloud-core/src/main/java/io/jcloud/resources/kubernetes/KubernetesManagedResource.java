package io.jcloud.resources.kubernetes;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.jcloud.api.clients.KubectlClient;
import io.jcloud.configuration.KubernetesServiceConfiguration;
import io.jcloud.configuration.KubernetesServiceConfigurationBuilder;
import io.jcloud.core.ManagedResource;
import io.jcloud.core.ServiceContext;
import io.jcloud.core.extensions.KubernetesExtensionBootstrap;
import io.jcloud.logging.KubernetesLoggingHandler;
import io.jcloud.logging.LoggingHandler;
import io.jcloud.utils.FileUtils;
import io.jcloud.utils.ManifestsUtils;

public abstract class KubernetesManagedResource extends ManagedResource {

    private static final String DEPLOYMENT = "kubernetes.yml";

    private KubectlClient client;
    private LoggingHandler loggingHandler;
    private boolean init;
    private boolean running;

    protected abstract String getImage();

    protected abstract String getExpectedLog();

    protected abstract Integer[] getPorts();

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
        context.loadCustomConfiguration(io.jcloud.configuration.KubernetesServiceConfiguration.class,
                new KubernetesServiceConfigurationBuilder());
    }

    protected String[] getCommand() {
        return null;
    }

    protected void doInit() {
        this.client = context.get(KubernetesExtensionBootstrap.CLIENT);

        applyDeployment();

        for (int port : getPorts()) {
            client.expose(context.getOwner(), port);
        }
    }

    protected void doUpdate() {
        applyDeployment();
    }

    private void applyDeployment() {
        Deployment deployment = Optional
                .ofNullable(context.getConfigurationAs(KubernetesServiceConfiguration.class).getTemplate())
                .filter(StringUtils::isNotEmpty)
                .map(f -> Serialization.unmarshal(FileUtils.loadFile(f), Deployment.class)).orElseGet(Deployment::new);

        // Set service data
        initDeployment(deployment);
        Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        container.setName(context.getName());
        if (StringUtils.isEmpty(container.getImage())) {
            container.setImage(getImage());
        }

        if (container.getCommand() != null && container.getCommand().size() > 0 && getCommand() != null
                && getCommand().length > 0) {
            container.setCommand(Arrays.asList(getCommand()));
        }

        for (int port : getPorts()) {
            if (container.getPorts().stream().noneMatch(p -> p.getContainerPort() == port)) {
                container.getPorts()
                        .add(new ContainerPortBuilder().withName("port-" + port).withContainerPort(port).build());
            }
        }

        // Enrich it
        ManifestsUtils.enrichDeployment(client.underlyingClient(), deployment, context.getOwner());

        // Apply it
        Path target = context.getServiceFolder().resolve(DEPLOYMENT);
        ManifestsUtils.writeFile(target, deployment);
        client.apply(context.getOwner(), target);
    }

    private void initDeployment(Deployment deployment) {
        if (deployment.getSpec() == null) {
            deployment.setSpec(new DeploymentSpec());
        }
        if (deployment.getSpec().getTemplate() == null) {
            deployment.getSpec().setTemplate(new PodTemplateSpec());
        }
        if (deployment.getSpec().getTemplate().getSpec() == null) {
            deployment.getSpec().getTemplate().setSpec(new PodSpec());
        }
        if (deployment.getSpec().getTemplate().getSpec().getContainers() == null) {
            deployment.getSpec().getTemplate().getSpec().setContainers(new ArrayList<>());
        }
        if (deployment.getSpec().getTemplate().getSpec().getContainers().isEmpty()) {
            deployment.getSpec().getTemplate().getSpec().getContainers().add(new Container());
        }
    }

    private boolean useInternalServiceAsUrl() {
        return context.getConfigurationAs(KubernetesServiceConfiguration.class).isUseInternalService();
    }

}
