package io.jcloud.core;

import static org.junit.jupiter.api.Assertions.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;

import javax.inject.Inject;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.LifecycleMethodExecutionExceptionHandler;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.api.extension.TestWatcher;

import io.jcloud.api.LookupService;
import io.jcloud.api.Service;
import io.jcloud.api.extensions.AnnotationBinding;
import io.jcloud.api.extensions.ExtensionBootstrap;
import io.jcloud.logging.Log;
import io.jcloud.utils.ReflectionUtils;

public class JCloudExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback,
        ParameterResolver, LifecycleMethodExecutionExceptionHandler, TestWatcher {

    private final ServiceLoader<AnnotationBinding> bindingsRegistry = ServiceLoader.load(AnnotationBinding.class);
    private final ServiceLoader<ExtensionBootstrap> extensionsRegistry = ServiceLoader.load(ExtensionBootstrap.class);

    private List<ServiceContext> services = new ArrayList<>();
    private JCloudContext context;
    private List<ExtensionBootstrap> extensions;

    @Override
    public void beforeAll(ExtensionContext testContext) {
        // Init jcloud context
        context = new JCloudContext(testContext);
        Log.configure();
        Log.debug("JCloud ID: '%s'", context.getId());

        // Init extensions
        extensions = initExtensions();
        extensions.forEach(ext -> ext.beforeAll(context));

        // Init services from class annotations
        ReflectionUtils.findAllAnnotations(testContext.getRequiredTestClass())
                .forEach(annotation -> initServiceFromAnnotation(annotation));

        // Init services from static fields
        ReflectionUtils.findAllFields(testContext.getRequiredTestClass()).stream().filter(ReflectionUtils::isStatic)
                .forEach(field -> initResourceFromField(testContext, field));
    }

    @Override
    public void afterAll(ExtensionContext testContext) {
        try {
            List<ServiceContext> servicesToFinish = new ArrayList<>(services);
            Collections.reverse(servicesToFinish);
            servicesToFinish.forEach(s -> s.getOwner().close());
            deleteLogIfTestSuitePassed();
            services.clear();
        } finally {
            extensions.forEach(ext -> ext.afterAll(context));
        }
    }

    @Override
    public void beforeEach(ExtensionContext testContext) {
        // Init services from instance fields
        ReflectionUtils.findAllFields(testContext.getRequiredTestClass()).stream().filter(ReflectionUtils::isInstance)
                .forEach(field -> initResourceFromField(testContext, field));

        Log.info("## Running test " + testContext.getParent().map(ctx -> ctx.getDisplayName() + ".").orElse("")
                + testContext.getDisplayName());
        context.setMethodTestContext(testContext);
        extensions.forEach(ext -> ext.beforeEach(context));
        services.forEach(service -> {
            if (service.getOwner().isAutoStart() && !service.getOwner().isRunning()) {
                service.getOwner().start();
            }
        });
    }

    @Override
    public void afterEach(ExtensionContext testContext) {
        if (!isClassLifecycle(testContext)) {
            // Stop services from instance fields
            ReflectionUtils.findAllFields(testContext.getRequiredTestClass()).stream()
                    .filter(ReflectionUtils::isInstance).forEach(field -> stopServiceFromField(testContext, field));
        }

        extensions.forEach(ext -> ext.afterEach(context));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return isParameterSupported(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return getParameter(new DependencyContext(parameterContext));
    }

    @Override
    public void handleAfterAllMethodExecutionException(ExtensionContext context, Throwable throwable) {
        testOnError(throwable);
    }

    @Override
    public void handleAfterEachMethodExecutionException(ExtensionContext context, Throwable throwable) {
        testOnError(throwable);
    }

    @Override
    public void handleBeforeAllMethodExecutionException(ExtensionContext context, Throwable throwable) {
        testOnError(throwable);
    }

    @Override
    public void testSuccessful(ExtensionContext testContext) {
        extensions.forEach(ext -> ext.onSuccess(context));
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        testOnError(cause);
    }

    @Override
    public void testDisabled(ExtensionContext testContext, Optional<String> reason) {
        extensions.forEach(ext -> ext.onDisabled(context, reason));
    }

    @Override
    public void handleBeforeEachMethodExecutionException(ExtensionContext context, Throwable throwable) {
        testOnError(throwable);
    }

    private void launchService(Service service) {
        if (!service.isAutoStart()) {
            Log.debug(service, "Service (%s) auto start is off", service.getDisplayName());
            return;
        }

        Log.info(service, "Initialize service (%s)", service.getDisplayName());
        extensions.forEach(ext -> ext.onServiceLaunch(context, service));
        try {
            service.start();
        } catch (Throwable throwable) {
            testOnError(throwable);
            throw throwable;
        }
    }

    private void testOnError(Throwable throwable) {
        // mark test suite as failed
        context.markTestSuiteAsFailed();
        // notify extensions
        extensions.forEach(ext -> ext.onError(context, throwable));
    }

    private void initResourceFromField(ExtensionContext context, Field field) {
        if (field.isAnnotationPresent(LookupService.class)) {
            initLookupService(context, field);
        } else if (Service.class.isAssignableFrom(field.getType())) {
            Service service = ReflectionUtils.getFieldValue(findTestInstance(context, field), field);
            initService(service, field.getName(), field.getAnnotations());
        } else if (field.isAnnotationPresent(Inject.class)) {
            injectDependency(context, field);
        }
    }

    private void initServiceFromAnnotation(Annotation annotation) {
        getAnnotationBinding(annotation).ifPresent(binding -> initService(binding.getDefaultServiceImplementation(),
                binding.getDefaultName(annotation), binding, annotation));
    }

    private void stopServiceFromField(ExtensionContext context, Field field) {
        if (Service.class.isAssignableFrom(field.getType())) {
            Service service = ReflectionUtils.getFieldValue(findTestInstance(context, field), field);
            service.stop();
            services.removeIf(s -> service.getName().equals(s.getName()));
        }
    }

    private void injectDependency(ExtensionContext testContext, Field field) {
        Object fieldValue = null;
        if (JCloudContext.class.equals(field.getType())) {
            fieldValue = context;
        } else if (isParameterSupported(field.getType())) {
            fieldValue = getParameter(new DependencyContext(field.getName(), field.getType(), field.getAnnotations()));
        }

        if (fieldValue != null) {
            ReflectionUtils.setFieldValue(findTestInstance(testContext, field), field, fieldValue);
        }
    }

    private void initService(Service service, String name, Annotation... annotations) {
        AnnotationBinding binding = getAnnotationBinding(annotations)
                .orElseThrow(() -> new RuntimeException("Unknown annotation for service"));
        initService(service, name, binding, annotations);
    }

    private void initService(Service service, String name, AnnotationBinding binding, Annotation... annotations) {
        if (service.isRunning()) {
            return;
        }

        // Validate
        service.validate(binding, annotations);

        // Resolve managed resource
        ManagedResource resource = getManagedResource(name, service, binding, annotations);

        // Initialize it
        ServiceContext serviceContext = service.register(name, context);
        service.init(resource);
        services.add(serviceContext);

        extensions.forEach(ext -> ext.updateServiceContext(serviceContext));
        launchService(service);
    }

    private Optional<AnnotationBinding> getAnnotationBinding(Annotation... annotations) {
        return bindingsRegistry.stream().map(Provider::get).filter(b -> b.isFor(annotations)).findFirst();
    }

    private ManagedResource getManagedResource(String name, Service service, AnnotationBinding binding,
            Annotation... annotations) {
        try {
            return binding.getManagedResource(context, service, annotations);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the Managed Resource for " + name, ex);
        }
    }

    private void initLookupService(ExtensionContext context, Field fieldToInject) {
        Optional<Field> fieldService = ReflectionUtils.findAllFields(context.getRequiredTestClass()).stream()
                .filter(field -> field.getName().equals(fieldToInject.getName())
                        && !field.isAnnotationPresent(LookupService.class))
                .findAny();
        if (!fieldService.isPresent()) {
            fail("Could not lookup service with name " + fieldToInject.getName());
        }

        Field field = fieldService.get();
        Service service = ReflectionUtils.getFieldValue(findTestInstance(context, field), field);
        initService(service, field.getName(), field.getAnnotations());
        ReflectionUtils.setFieldValue(findTestInstance(context, fieldToInject), fieldToInject, service);
    }

    private boolean isParameterSupported(Class<?> paramType) {
        return paramType.isAssignableFrom(JCloudContext.class)
                || extensions.stream().anyMatch(ext -> ext.supportedParameters().contains(paramType));
    }

    private Object getParameter(DependencyContext dependency) {
        if (dependency.getType().isAssignableFrom(JCloudContext.class)) {
            return context;
        }

        Optional<Object> parameter = extensions.stream().map(ext -> ext.getParameter(dependency))
                .filter(Optional::isPresent).map(Optional::get).findFirst();

        if (!parameter.isPresent()) {
            fail("Failed to inject: " + dependency.getName());
        }

        return parameter.get();
    }

    private List<ExtensionBootstrap> initExtensions() {
        List<ExtensionBootstrap> list = new ArrayList<>();
        for (ExtensionBootstrap binding : extensionsRegistry) {
            if (binding.appliesFor(context)) {
                binding.updateContext(context);
                list.add(binding);
            }
        }

        return list;
    }

    private void deleteLogIfTestSuitePassed() {
        if (!context.isFailed()) {
            context.getLogFile().toFile().delete();
        }
    }

    private boolean isClassLifecycle(ExtensionContext context) {
        if (context.getTestInstanceLifecycle().isPresent()) {
            return context.getTestInstanceLifecycle().get() == TestInstance.Lifecycle.PER_CLASS;
        } else if (context.getParent().isPresent()) {
            return isClassLifecycle(context.getParent().get());
        }

        return false;
    }

    private Optional<Object> findTestInstance(ExtensionContext context, Field field) {
        Optional<TestInstances> testInstances = context.getTestInstances();
        if (testInstances.isPresent()) {
            return testInstances.get().findInstance((Class<Object>) field.getDeclaringClass());
        }

        return context.getTestInstance();
    }
}
