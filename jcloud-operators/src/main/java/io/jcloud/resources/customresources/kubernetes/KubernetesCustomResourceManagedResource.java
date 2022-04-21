package io.jcloud.resources.customresources.kubernetes;

import java.nio.file.Path;
import java.util.regex.Pattern;

import io.fabric8.kubernetes.client.CustomResource;
import io.jcloud.api.model.CustomResourceDefinition;
import io.jcloud.api.model.CustomResourceSpec;
import io.jcloud.api.model.CustomResourceStatus;
import io.jcloud.core.ManagedResource;
import io.jcloud.core.ServiceContext;
import io.jcloud.core.extensions.KubernetesExtensionBootstrap;
import io.jcloud.logging.KubernetesLoggingHandler;
import io.jcloud.logging.LoggingHandler;
import io.jcloud.resources.operators.kubernetes.KubectlOperatorClient;
import io.jcloud.utils.FileUtils;
import io.jcloud.utils.PropertiesUtils;

public class KubernetesCustomResourceManagedResource extends ManagedResource {

    private final String resource;
    private final Class<? extends CustomResource<CustomResourceSpec, CustomResourceStatus>> type;

    private LoggingHandler loggingHandler;
    private KubectlOperatorClient client;
    private boolean running;
    private CustomResourceDefinition crdToWatch;

    public KubernetesCustomResourceManagedResource(String resource,
            Class<? extends CustomResource<CustomResourceSpec, CustomResourceStatus>> type) {
        this.resource = PropertiesUtils.resolveProperty(resource);
        this.type = type;
    }

    @Override
    public String getDisplayName() {
        return "Resource " + resource;
    }

    @Override
    public void start() {
        if (!running) {
            this.client = new KubectlOperatorClient(context.get(KubernetesExtensionBootstrap.CLIENT));
            applyCRD();

            loggingHandler = new KubernetesLoggingHandler(context);
            loggingHandler.startWatching();

            running = true;
        }
    }

    @Override
    public void stop() {
        deleteCRD();
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
    protected void init(ServiceContext context) {
        super.init(context);
        crdToWatch = new CustomResourceDefinition(context.getName(), resource, type);
    }

    @Override
    protected LoggingHandler getLoggingHandler() {
        return loggingHandler;
    }

    private boolean customResourcesAreReady() {
        return client.isCustomResourceReady(crdToWatch.getName(), crdToWatch.getType());
    }

    private void applyCRD() {
        Path crdFileDefinition = context.getServiceFolder().resolve(crdToWatch.getName());
        String content = FileUtils.loadFile(crdToWatch.getFile()).replaceAll(Pattern.quote("${SERVICE_NAME}"),
                context.getName());
        FileUtils.copyContentTo(content, crdFileDefinition);

        client.apply(crdFileDefinition);
    }

    private void deleteCRD() {
        Path crdFileDefinition = context.getServiceFolder().resolve(crdToWatch.getName());
        client.delete(crdFileDefinition);
    }
}
