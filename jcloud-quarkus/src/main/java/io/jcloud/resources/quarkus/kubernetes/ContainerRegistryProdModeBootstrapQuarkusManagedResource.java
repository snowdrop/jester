package io.jcloud.resources.quarkus.kubernetes;

import static io.jcloud.utils.QuarkusUtils.HTTP_PORT_DEFAULT;
import static io.jcloud.utils.QuarkusUtils.QUARKUS_HTTP_PORT_PROPERTY;

import io.jcloud.api.Dependency;
import io.jcloud.core.ServiceContext;
import io.jcloud.resources.kubernetes.KubernetesManagedResource;
import io.jcloud.resources.quarkus.BootstrapQuarkusProxy;
import io.jcloud.utils.QuarkusUtils;

public class ContainerRegistryProdModeBootstrapQuarkusManagedResource extends KubernetesManagedResource {

    private final String propertiesFile;
    private final Class<?>[] classes;
    private final Dependency[] forcedDependencies;

    private BootstrapQuarkusProxy proxy;
    private String image;

    public ContainerRegistryProdModeBootstrapQuarkusManagedResource(String propertiesFile, Class<?>[] classes,
            Dependency[] forcedDependencies) {
        this.propertiesFile = propertiesFile;
        this.classes = classes;
        this.forcedDependencies = forcedDependencies;
    }

    @Override
    public String getDisplayName() {
        return proxy.getDisplayName();
    }

    @Override
    public String getImage() {
        return image;
    }

    @Override
    protected String getExpectedLog() {
        return proxy.getExpectedLog();
    }

    @Override
    protected Integer[] getPorts() {
        return new Integer[] { context.getOwner().getProperty(QUARKUS_HTTP_PORT_PROPERTY).map(Integer::parseInt)
                .orElse(HTTP_PORT_DEFAULT) };
    }

    @Override
    protected void init(ServiceContext context) {
        super.init(context);

        proxy = new BootstrapQuarkusProxy(context, propertiesFile, classes, forcedDependencies);
        image = createImageAndPush();
    }

    private String createImageAndPush() {
        return QuarkusUtils.createImageAndPush(context, proxy.getLaunchMode(), proxy.getArtifact());
    }

}
