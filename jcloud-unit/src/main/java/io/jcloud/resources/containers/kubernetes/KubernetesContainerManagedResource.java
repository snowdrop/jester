package io.jcloud.resources.containers.kubernetes;

import static java.util.regex.Pattern.quote;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.jcloud.api.clients.KubectlClient;
import io.jcloud.core.ManagedResource;
import io.jcloud.core.ServiceContext;
import io.jcloud.core.extensions.KubernetesExtensionBootstrap;
import io.jcloud.logging.KubernetesLoggingHandler;
import io.jcloud.logging.LoggingHandler;
import io.jcloud.utils.PropertiesUtils;

public class KubernetesContainerManagedResource extends ManagedResource {

    private static final String DEPLOYMENT_SERVICE_PROPERTY = "kubernetes.service";
    private static final String DEPLOYMENT_TEMPLATE_PROPERTY = "kubernetes.template";
    private static final String USE_INTERNAL_SERVICE_AS_URL_PROPERTY = "kubernetes.use-internal-service-as-url";
    private static final String DEPLOYMENT_TEMPLATE_PROPERTY_DEFAULT = "/kubernetes-deployment-template.yml";

    private static final String DEPLOYMENT = "kubernetes.yml";

    private final String image;
    private final String expectedLog;
    private final String[] command;
    private final Integer[] ports;

    private KubectlClient client;
    private LoggingHandler loggingHandler;
    private boolean running;

    public KubernetesContainerManagedResource(String image, String expectedLog, String[] command, int[] ports) {
        this.image = PropertiesUtils.resolveProperty(image);
        this.command = command;
        this.expectedLog = PropertiesUtils.resolveProperty(expectedLog);
        this.ports = Arrays.stream(ports).boxed().toArray(Integer[]::new);
    }

    @Override
    protected void init(ServiceContext context) {
        super.init(context);
        this.client = context.get(KubernetesExtensionBootstrap.CLIENT);
    }

    @Override
    public String getDisplayName() {
        return image;
    }

    @Override
    public void start() {
        if (running) {
            return;
        }

        applyDeployment();

        client.expose(context.getOwner(), ports[0]);

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

        client.scaleTo(context.getOwner(), 0);
        running = false;
    }

    @Override
    public String getHost() {
        if (useInternalServiceAsUrl()) {
            return context.getName();
        }

        return client.url(context.getOwner());
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
        return loggingHandler != null && loggingHandler.logsContains(expectedLog);
    }

    @Override
    public List<String> logs() {
        return loggingHandler.logs();
    }

    @Override
    protected LoggingHandler getLoggingHandler() {
        return loggingHandler;
    }

    private void applyDeployment() {
        String deploymentFile = context.getOwner().getConfiguration().getOrDefault(DEPLOYMENT_TEMPLATE_PROPERTY,
                DEPLOYMENT_TEMPLATE_PROPERTY_DEFAULT);
        client.applyServiceProperties(context.getOwner(), deploymentFile, this::replaceDeploymentContent,
                context.getServiceFolder().resolve(DEPLOYMENT));
    }

    private String replaceDeploymentContent(String content) {
        String customServiceName = context.getOwner().getConfiguration().get(DEPLOYMENT_SERVICE_PROPERTY);
        if (StringUtils.isNotEmpty(customServiceName)) {
            // replace it by the service owner name
            content = content.replaceAll(quote(customServiceName), context.getName());
        }

        return content.replaceAll(quote("${IMAGE}"), image)
                .replaceAll(quote("${SERVICE_NAME}"), context.getName())
                .replaceAll(quote("${INTERNAL_PORT}"), "" + ports[0]);
    }

    private boolean useInternalServiceAsUrl() {
        return Boolean.TRUE.toString()
                .equals(context.getOwner().getConfiguration().get(USE_INTERNAL_SERVICE_AS_URL_PROPERTY));
    }

}
