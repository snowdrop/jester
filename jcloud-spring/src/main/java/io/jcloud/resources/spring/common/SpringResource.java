package io.jcloud.resources.spring.common;

import static io.jcloud.utils.FileUtils.findFile;
import static io.jcloud.utils.PropertiesUtils.TARGET;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.jcloud.configuration.SpringServiceConfiguration;
import io.jcloud.configuration.SpringServiceConfigurationBuilder;
import io.jcloud.core.ServiceContext;
import io.jcloud.logging.LoggingHandler;

public class SpringResource {
    private static final List<String> ERRORS = Arrays.asList("Application run failed");
    private static final String JVM_RUNNER = ".jar";

    private final ServiceContext context;
    private final Path runner;

    public SpringResource(ServiceContext context) {
        this.context = context;
        this.context.loadCustomConfiguration(SpringServiceConfiguration.class, new SpringServiceConfigurationBuilder());
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
        return context.getConfigurationAs(SpringServiceConfiguration.class).getExpectedLog();
    }

    private Optional<String> findRunner() {
        return findFile(TARGET, JVM_RUNNER);
    }
}
