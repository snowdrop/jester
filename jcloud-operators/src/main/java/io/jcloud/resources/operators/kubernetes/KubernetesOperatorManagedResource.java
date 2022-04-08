package io.jcloud.resources.operators.kubernetes;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import io.jcloud.api.OperatorService;
import io.jcloud.api.model.CustomResourceDefinition;
import io.jcloud.configuration.OperatorServiceConfiguration;
import io.jcloud.configuration.OperatorServiceConfigurationBuilder;
import io.jcloud.core.ManagedResource;
import io.jcloud.core.ServiceContext;
import io.jcloud.core.extensions.KubernetesExtensionBootstrap;
import io.jcloud.logging.KubernetesLoggingHandler;
import io.jcloud.logging.LoggingHandler;
import io.jcloud.utils.FileUtils;
import io.jcloud.utils.PropertiesUtils;

public class KubernetesOperatorManagedResource extends ManagedResource {

    private final String subscriptionName;
    private final String channel;
    private final String source;
    private final String sourceNamespace;

    private LoggingHandler loggingHandler;
    private KubectlOperatorClient client;
    private boolean running;
    private List<CustomResourceDefinition> crdsToWatch = new ArrayList<>();

    public KubernetesOperatorManagedResource(String subscriptionName, String channel, String source,
            String sourceNamespace) {
        this.subscriptionName = PropertiesUtils.resolveProperty(subscriptionName);
        this.channel = PropertiesUtils.resolveProperty(channel);
        this.source = PropertiesUtils.resolveProperty(source);
        this.sourceNamespace = PropertiesUtils.resolveProperty(sourceNamespace);
    }

    @Override
    public String getDisplayName() {
        return "Operator " + subscriptionName;
    }

    @Override
    public void start() {
        if (!running) {
            this.client = new KubectlOperatorClient(context.get(KubernetesExtensionBootstrap.CLIENT));
            installOperator();
            applyCRDs();

            loggingHandler = new KubernetesLoggingHandler(context);
            loggingHandler.startWatching();

            running = true;
        }
    }

    @Override
    public void stop() {
        client.deleteOperator(context, subscriptionName);
        running = false;
    }

    @Override
    public String getHost() {
        throw new UnsupportedOperationException("getHost in operators is not supported yet");
    }

    @Override
    public int getFirstMappedPort() {
        throw new UnsupportedOperationException("getFirstMappedPort in operators is not supported yet");
    }

    @Override
    public int getMappedPort(int port) {
        throw new UnsupportedOperationException("getMappedPort in operators is not supported yet");
    }

    @Override
    public boolean isRunning() {
        return running && customResourcesAreReady();
    }

    @Override
    protected LoggingHandler getLoggingHandler() {
        return loggingHandler;
    }

    @Override
    protected void init(ServiceContext context) {
        super.init(context);
        context.loadCustomConfiguration(OperatorServiceConfiguration.class, new OperatorServiceConfigurationBuilder());
    }

    private boolean customResourcesAreReady() {
        return crdsToWatch.stream().allMatch(crd -> client.isCustomResourceReady(crd.getName(), crd.getType()));
    }

    private void installOperator() {
        client.installOperator(context, subscriptionName, channel, source, sourceNamespace);
    }

    private void applyCRDs() {
        if (context.getOwner() instanceof OperatorService) {
            OperatorService service = (OperatorService) context.getOwner();
            for (Object crd : service.getCrds()) {
                applyCRD((CustomResourceDefinition) crd);
            }
        }
    }

    private void applyCRD(CustomResourceDefinition crd) {
        Path crdFileDefinition = context.getServiceFolder().resolve(crd.getName());
        String content = FileUtils.loadFile(crd.getFile()).replaceAll(Pattern.quote("${SERVICE_NAME}"),
                context.getName());
        FileUtils.copyContentTo(content, crdFileDefinition);

        client.apply(crdFileDefinition);
        crdsToWatch.add(crd);
    }
}
