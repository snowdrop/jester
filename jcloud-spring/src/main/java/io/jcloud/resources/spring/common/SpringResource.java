package io.jcloud.resources.spring.common;

import static io.jcloud.utils.FileUtils.findFile;
import static io.jcloud.utils.PropertiesUtils.TARGET;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.bridge.SLF4JBridgeHandler;

import io.jcloud.configuration.PropertyLookup;
import io.jcloud.core.ServiceContext;
import io.jcloud.logging.LoggingHandler;

public class SpringResource {
    private static final String EXPECTED_OUTPUT_DEFAULT = "initialization completed";
    private static final PropertyLookup EXPECTED_OUTPUT = new PropertyLookup("spring.expected.log",
            EXPECTED_OUTPUT_DEFAULT);
    private static final List<String> ERRORS = Arrays.asList("Application run failed");
    private static final String JVM_RUNNER = ".jar";

    private final ServiceContext context;
    private final Path runner;

    public SpringResource(ServiceContext context) {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.uninstall();
        this.context = context;
        this.runner = findRunner().map(Path::of).orElseThrow(() -> new RuntimeException(
                "Could not locate the Spring JAR file. You need to build the application before running the test."));
    }

    public String getDisplayName() {
        return "Spring Boot";
    }

    public Path getRunner() {
        return runner;
    }

    public boolean isRunning(LoggingHandler loggingHandler) {
        return loggingHandler != null && loggingHandler.logsContains(getExpectedLog());
    }

    public boolean isFailed(LoggingHandler loggingHandler) {
        return loggingHandler != null && ERRORS.stream().anyMatch(error -> loggingHandler.logsContains(error));
    }

    public String getExpectedLog() {
        return EXPECTED_OUTPUT.get(context);
    }

    private Optional<String> findRunner() {
        return findFile(TARGET, JVM_RUNNER);
    }
}
