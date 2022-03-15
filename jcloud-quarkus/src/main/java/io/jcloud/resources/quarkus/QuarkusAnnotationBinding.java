package io.jcloud.resources.quarkus;

import java.lang.reflect.Field;
import java.util.ServiceLoader;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.jcloud.api.Quarkus;
import io.jcloud.api.extensions.AnnotationBinding;
import io.jcloud.api.extensions.QuarkusManagedResourceBinding;
import io.jcloud.core.ManagedResource;
import io.jcloud.resources.quarkus.local.ProdModeBootstrapQuarkusManagedResource;

public class QuarkusAnnotationBinding implements AnnotationBinding {

    private final ServiceLoader<QuarkusManagedResourceBinding> customBindings = ServiceLoader
            .load(QuarkusManagedResourceBinding.class);

    @Override
    public boolean isFor(Field field) {
        return field.isAnnotationPresent(Quarkus.class);
    }

    @Override
    public ManagedResource getManagedResource(ExtensionContext context, Field field) {
        Quarkus metadata = field.getAnnotation(Quarkus.class);

        for (QuarkusManagedResourceBinding binding : customBindings) {
            if (binding.appliesFor(context)) {
                return binding.init(field);
            }
        }

        // If none handler found, then the container will be running on localhost by default
        return new ProdModeBootstrapQuarkusManagedResource(metadata.properties(), metadata.classes(),
                metadata.dependencies());
    }

}
