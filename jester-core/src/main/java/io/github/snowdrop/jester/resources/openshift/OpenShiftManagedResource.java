package io.github.snowdrop.jester.resources.openshift;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import io.fabric8.kubernetes.api.model.Capabilities;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.PodSecurityContext;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.SecurityContext;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.github.snowdrop.jester.api.clients.OpenshiftClient;
import io.github.snowdrop.jester.configuration.OpenShiftServiceConfiguration;
import io.github.snowdrop.jester.configuration.OpenShiftServiceConfigurationBuilder;
import io.github.snowdrop.jester.core.ManagedResource;
import io.github.snowdrop.jester.core.ServiceContext;
import io.github.snowdrop.jester.core.extensions.OpenShiftExtensionBootstrap;
import io.github.snowdrop.jester.logging.LoggingHandler;
import io.github.snowdrop.jester.logging.OpenShiftLoggingHandler;
import io.github.snowdrop.jester.utils.AwaitilitySettings;
import io.github.snowdrop.jester.utils.AwaitilityUtils;
import io.github.snowdrop.jester.utils.FileUtils;
import io.github.snowdrop.jester.utils.ManifestsUtils;

public abstract class OpenShiftManagedResource extends ManagedResource {

    private static final String DEPLOYMENT = "openshift.yml";

    private OpenshiftClient client;
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
        } else if (context.getConfigurationAs(OpenShiftServiceConfiguration.class).isUseRoute()) {
            return 80;
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
        context.loadCustomConfiguration(OpenShiftServiceConfiguration.class,
                new OpenShiftServiceConfigurationBuilder());
    }

    protected String[] getCommand() {
        return null;
    }

    protected void doInit() {
        this.client = context.get(OpenShiftExtensionBootstrap.CLIENT);

        applyDeployment();

        client.expose(context.getOwner(), getEffectivePorts());

        if (context.getConfigurationAs(OpenShiftServiceConfiguration.class).isUseRoute()) {
            client.exposeRoute(context.getOwner(), getEffectivePorts());
            // wait until the route is reachable
            waitForRoute();
        }
    }

    protected void doUpdate() {
        applyDeployment();
    }

    private void waitForRoute() {
        HttpClient client = HttpClient.newHttpClient();
        AwaitilityUtils.untilIsTrue(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(String.format("http://%s:%s", getHost(), getFirstMappedPort()))).GET().build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return response.statusCode() != HttpStatus.SC_SERVICE_UNAVAILABLE;
            } catch (Exception ignored) {
                return false;
            }
        }, AwaitilitySettings.defaults().withService(context.getOwner()));
    }

    private void applyDeployment() {
        Deployment deployment = Optional
                .ofNullable(context.getConfigurationAs(OpenShiftServiceConfiguration.class).getTemplate())
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

        for (int port : getEffectivePorts()) {
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
        client.apply(target);
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
        if (deployment.getSpec().getTemplate().getSpec().getSecurityContext() == null) {
            deployment.getSpec().getTemplate().getSpec().setSecurityContext(new PodSecurityContext());
            deployment.getSpec().getTemplate().getSpec().getSecurityContext().setRunAsNonRoot(true);
        }
        if (deployment.getSpec().getTemplate().getSpec().getContainers() == null) {
            deployment.getSpec().getTemplate().getSpec().setContainers(new ArrayList<>());
        }
        if (deployment.getSpec().getTemplate().getSpec().getContainers().isEmpty()) {
            deployment.getSpec().getTemplate().getSpec().getContainers().add(new Container());
            deployment.getSpec().getTemplate().getSpec().getContainers().get(0)
                    .setSecurityContext(new SecurityContext());
            deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getSecurityContext()
                    .setAllowPrivilegeEscalation(false);
            Capabilities capabilities = new Capabilities();
            capabilities.setDrop(Arrays.asList("ALL"));
            deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getSecurityContext()
                    .setCapabilities(capabilities);
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
