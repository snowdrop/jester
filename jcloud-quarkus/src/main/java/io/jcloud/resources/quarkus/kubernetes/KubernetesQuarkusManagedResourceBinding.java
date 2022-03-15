package io.jcloud.resources.quarkus.kubernetes;

import java.lang.reflect.Field;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.jcloud.api.KubernetesScenario;
import io.jcloud.api.Quarkus;
import io.jcloud.api.extensions.QuarkusManagedResourceBinding;
import io.jcloud.core.ManagedResource;

public class KubernetesQuarkusManagedResourceBinding implements QuarkusManagedResourceBinding {
    @Override
    public boolean appliesFor(ExtensionContext context) {
        return context.getRequiredTestClass().isAnnotationPresent(KubernetesScenario.class);
    }

    @Override
    public ManagedResource init(Field field) {
        Quarkus metadata = field.getAnnotation(Quarkus.class);
        return new ContainerRegistryProdModeBootstrapQuarkusManagedResource(metadata.properties(), metadata.classes(),
                metadata.dependencies());
    }
}
