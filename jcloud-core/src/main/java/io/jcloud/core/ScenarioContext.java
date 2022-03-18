package io.jcloud.core;

import static io.jcloud.logging.Log.LOG_SUFFIX;

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.jupiter.api.extension.ExtensionContext;

public final class ScenarioContext {

    private static final String LOG_FILE_PATH = System.getProperty("log.file.path", "target/logs");
    private static final int SCENARIO_ID_MAX_SIZE = 60;

    private final ExtensionContext testContext;
    private final String id;
    private final ExtensionContext.Namespace testNamespace;
    private final Map<Class<?>, List<Annotation>> serviceConfigurations = new HashMap<>();

    private ExtensionContext methodTestContext;
    private boolean failed;
    private boolean debug;

    protected ScenarioContext(ExtensionContext testContext) {
        this.testContext = testContext;
        this.id = generateScenarioId(testContext);
        this.testNamespace = ExtensionContext.Namespace.create(ScenarioContext.class);
    }

    public String getId() {
        return id;
    }

    public boolean isFailed() {
        return failed;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getRunningTestClassName() {
        return getTestContext().getRequiredTestClass().getSimpleName();
    }

    public Optional<String> getRunningTestMethodName() {
        if (methodTestContext == null) {
            return Optional.empty();
        }

        return Optional.of(methodTestContext.getRequiredTestMethod().getName());
    }

    public ExtensionContext.Store getTestStore() {
        return getTestContext().getStore(this.testNamespace);
    }

    public ExtensionContext getTestContext() {
        return Optional.ofNullable(methodTestContext).orElse(testContext);
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return getTestContext().getRequiredTestClass().isAnnotationPresent(annotationClass);
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return getTestContext().getRequiredTestClass().getAnnotation(annotationClass);
    }

    public void setMethodTestContext(ExtensionContext methodTestContext) {
        this.methodTestContext = methodTestContext;
    }

    public Path getLogFolder() {
        return Paths.get(LOG_FILE_PATH);
    }

    public Path getLogFile() {
        return getLogFolder().resolve(getRunningTestClassName() + LOG_SUFFIX);
    }

    public <T extends Annotation> Optional<T> getAnnotatedConfigurationForService(Class<T> clazz, Predicate<T> apply) {
        List<Annotation> serviceConfigurationsByClass = serviceConfigurations.get(clazz);
        if (serviceConfigurationsByClass == null) {
            serviceConfigurationsByClass = loadAnnotatedConfiguration(clazz);
            serviceConfigurations.put(clazz, serviceConfigurationsByClass);
        }

        return serviceConfigurationsByClass.stream().filter(clazz::isInstance).map(clazz::cast).filter(apply::test)
                .findFirst();
    }

    protected void markScenarioAsFailed() {
        failed = true;
    }

    private List<Annotation> loadAnnotatedConfiguration(Class<? extends Annotation> clazz) {
        return Arrays.asList(testContext.getRequiredTestClass().getAnnotationsByType(clazz));
    }

    private static String generateScenarioId(ExtensionContext context) {
        String fullId = context.getRequiredTestClass().getSimpleName() + "-" + System.currentTimeMillis();
        return fullId.substring(0, Math.min(SCENARIO_ID_MAX_SIZE, fullId.length()));
    }
}
