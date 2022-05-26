package io.jester.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Repeatable(ServiceConfigurations.class)
public @interface ServiceConfiguration {
    String forService();

    /**
     * Default startup timeout for services is 5 minutes. Fallback service property: "ts.services.<SERVICE
     * NAME>.startup.timeout".
     */
    String startupTimeout() default "";

    /**
     * Default startup check poll interval is every 2 seconds. Fallback service property: "ts.services.<SERVICE
     * NAME>.startup.check-poll-interval".
     */
    String startupCheckPollInterval() default "";

    /**
     * Default timeout factor for all checks. Fallback service property: "ts.services.<SERVICE NAME>.factor.timeout".
     */
    double factorTimeout() default 1.0;

    /**
     * Delete /target/{service name} folder on service close. Fallback service property: "ts.services.<SERVICE
     * NAME>.delete.folder.on.close".
     */
    boolean deleteFolderOnClose() default true;

    /**
     * Enable/Disable the logs for the current service. Fallback service property: "ts.services.<SERVICE
     * NAME>.log.enabled".
     */
    boolean logEnabled() default true;

    /**
     * Tune the log level for the current service. Possible values in {@link java.util.logging.Level}.
     */
    String logLevel() default "INFO";

    /**
     * Port resolution with range min. Fallback service property: "ts.services.<SERVICE NAME>.port.range.min".
     */
    int portRangeMin() default 1101;

    /**
     * Port resolution with range max. Fallback service property: "ts.services.<SERVICE NAME>.port.range.max".
     */
    int portRangeMax() default 49151;

    /**
     * "incremental" (default) or "random". Fallback service property: "ts.services.<SERVICE
     * NAME>.port.resolution.strategy".
     */
    String portResolutionStrategy() default "incremental";

    /**
     * Configure the image registry to use for services.
     * <p>
     * Fallback service property: "ts.services.<SERVICE NAME>.image.registry".
     * <p>
     * The default registry is based on the default setup in Kind. It's convenient to set it as default to run tests
     * directly from the IDE.
     */
    String imageRegistry() default "localhost:5000";
}
