package io.jester.resources.spring.common;

import static io.jester.utils.FileUtils.findFile;
import static io.jester.utils.PropertiesUtils.TARGET;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.jester.configuration.SpringServiceConfiguration;
import io.jester.configuration.SpringServiceConfigurationBuilder;
import io.jester.core.ServiceContext;
import io.jester.logging.LoggingHandler;
import io.jester.utils.Command;
import io.jester.utils.FileUtils;
import io.jester.utils.PropertiesUtils;

public class SpringResource {
    private static final List<String> ERRORS = Arrays.asList("Application run failed");
    private static final String JVM_RUNNER = ".jar";

    private final ServiceContext context;
    private final Path location;
    private final Path runner;
    private final String[] buildCommands;

    public SpringResource(ServiceContext context, String location, boolean forceBuild, String[] buildCommands) {
        this.context = context;
        this.location = Path.of(location);
        if (!Files.exists(this.location)) {
            throw new RuntimeException("Spring location does not exist.");
        }

        this.buildCommands = PropertiesUtils.resolveProperties(buildCommands);
        this.context.loadCustomConfiguration(SpringServiceConfiguration.class, new SpringServiceConfigurationBuilder());
        if (forceBuild) {
            this.runner = tryToBuildRunner();
        } else {
            this.runner = findRunner().map(Path::of).orElseGet(this::tryToBuildRunner);
        }

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
        return findFile(location.resolve(TARGET), JVM_RUNNER);
    }

    private Path tryToBuildRunner() {
        FileUtils.copyDirectoryTo(location, context.getServiceFolder());
        FileUtils.deletePath(context.getServiceFolder().resolve("target"));
        FileUtils.deletePath(context.getServiceFolder().resolve("src").resolve("test").resolve("java"));
        if (buildCommands.length > 0) {
            List<String> effectiveCommands = new ArrayList<>();
            effectiveCommands.addAll(Arrays.asList(buildCommands));
            effectiveCommands.addAll(Arrays.asList("-DskipTests", "-Dformatter.skip", "-Dcheckstyle.skip"));
            try {
                new Command(effectiveCommands).onDirectory(context.getServiceFolder().toString()).runAndWait();
            } catch (Exception ex) {
                throw new RuntimeException("Error running build commands for service " + context.getName(), ex);
            }
        }

        return findFile(context.getServiceFolder().resolve(TARGET), JVM_RUNNER).map(Path::of)
                .orElseThrow(() -> new RuntimeException(
                        "Could not locate the Spring JAR file. You need to build the application before running the test."));
    }
}
