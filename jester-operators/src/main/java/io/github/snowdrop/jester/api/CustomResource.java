package io.github.snowdrop.jester.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.github.snowdrop.jester.api.model.CustomResourceSpec;
import io.github.snowdrop.jester.api.model.CustomResourceStatus;

@Target({ ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomResource {

    String resource();

    Class<? extends io.fabric8.kubernetes.client.CustomResource<CustomResourceSpec, CustomResourceStatus>> type();
}
