package io.jcloud.resources.local;

import static io.jcloud.utils.Ports.isSsl;
import static io.jcloud.utils.PropertiesUtils.RESOURCE_PREFIX;
import static io.jcloud.utils.PropertiesUtils.SECRET_PREFIX;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.jcloud.core.ManagedResource;
import io.jcloud.core.ServiceContext;
import io.jcloud.logging.FileServiceLoggingHandler;
import io.jcloud.logging.Log;
import io.jcloud.logging.LoggingHandler;
import io.jcloud.utils.FileUtils;
import io.jcloud.utils.ProcessBuilderProvider;
import io.jcloud.utils.ProcessUtils;
import io.jcloud.utils.PropertiesUtils;
import io.jcloud.utils.SocketUtils;

public abstract class JavaProcessManagedResource extends ManagedResource {

    private static final String LOCALHOST = "localhost";
    private static final String APPLICATION_PROPERTIES = "application.properties";
    private static final Path SOURCE_RESOURCES = Path.of("src", "main", "resources");
    private static final Path SOURCE_TEST_RESOURCES = Path.of("src", "test", "resources");
    private static final List<String> PREFIXES_TO_REPLACE = Arrays.asList(RESOURCE_PREFIX, SECRET_PREFIX);
    private static final String LOG_OUTPUT_FILE = "out.log";
    private static final String JAVA = "java";

    private File logOutputFile;
    private Process process;
    private LoggingHandler loggingHandler;
    private int assignedHttpPort;
    private int assignedSslPort;

    protected abstract String getHttpPortProperty();

    protected abstract Path getRunner();

    @Override
    public void start() {
        if (process != null && process.isAlive()) {
            // do nothing
            return;
        }

        try {
            List<String> command = prepareCommand(getPropertiesForCommand());
            Log.info(context.getOwner(), "Running command: %s", String.join(" ", command));

            ProcessBuilder pb = ProcessBuilderProvider.command(command).redirectErrorStream(true)
                    .redirectOutput(logOutputFile).directory(context.getServiceFolder().toFile());

            process = pb.start();

            loggingHandler = new FileServiceLoggingHandler(context.getOwner(), logOutputFile);
            loggingHandler.startWatching();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        if (loggingHandler != null) {
            loggingHandler.stopWatching();
        }

        ProcessUtils.destroy(process);
    }

    @Override
    public String getHost() {
        return LOCALHOST;
    }

    @Override
    public int getFirstMappedPort() {
        return assignedHttpPort;
    }

    @Override
    public int getMappedPort(int port) {
        if (isSsl(port)) {
            return assignedSslPort;
        }

        return assignedHttpPort;
    }

    @Override
    public String getProperty(String name) {
        Map<String, String> computedProperties = getPropertiesFromFile();
        return Optional.ofNullable(computedProperties.get(name))
                .orElseGet(() -> computedProperties.get(propertyWithProfile(name)));
    }

    @Override
    public boolean isRunning() {
        return process != null && process.isAlive();
    }

    @Override
    protected LoggingHandler getLoggingHandler() {
        return loggingHandler;
    }

    @Override
    protected void init(ServiceContext context) {
        super.init(context);
        this.logOutputFile = new File(context.getServiceFolder().resolve(LOG_OUTPUT_FILE).toString());
        assignPorts();
        copyResourcesToServiceFolder();
    }

    protected void copyResourcesToServiceFolder() {
        FileUtils.copyDirectoryTo(SOURCE_TEST_RESOURCES, context.getServiceFolder());
        FileUtils.copyDirectoryTo(SOURCE_RESOURCES, context.getServiceFolder());
    }

    protected Map<String, Object> getAllComputedProperties() {
        Map<String, Object> allProperties = new HashMap<>();
        // from properties file
        allProperties.putAll(getPropertiesFromFile());
        // from context
        allProperties.putAll(context.getAllProperties());
        return allProperties;
    }

    protected Path getComputedApplicationProperties() {
        return context.getServiceFolder().resolve(APPLICATION_PROPERTIES);
    }

    protected List<String> getPropertiesForCommand() {
        Map<String, String> runtimeProperties = new HashMap<>(context.getOwner().getProperties());
        if (enableSsl()) {
            runtimeProperties.putIfAbsent(getSslPortProperty(), "" + assignedSslPort);
        }

        runtimeProperties.putIfAbsent(getHttpPortProperty(), "" + assignedHttpPort);

        return runtimeProperties.entrySet().stream().map(e -> "-D" + e.getKey() + "=" + getComputedValue(e.getValue()))
                .collect(Collectors.toList());
    }

    protected String getSslPortProperty() {
        // if ssl is enable, let's use the same http port property by default.
        return getHttpPortProperty();
    }

    protected boolean enableSsl() {
        return false;
    }

    protected boolean enableProfiling() {
        return context.getJCloudContext().getConfiguration().isProfilingEnabled();
    }

    private String propertyWithProfile(String name) {
        return "%" + context.getJCloudContext().getRunningTestClassName() + "." + name;
    }

    private Map<String, String> getPropertiesFromFile() {
        List<Path> applicationPropertiesCandidates = Arrays.asList(getComputedApplicationProperties(),
                SOURCE_TEST_RESOURCES.resolve(APPLICATION_PROPERTIES),
                SOURCE_RESOURCES.resolve(APPLICATION_PROPERTIES));

        return applicationPropertiesCandidates.stream().filter(Files::exists).map(PropertiesUtils::toMap).findFirst()
                .orElseGet(Collections::emptyMap);
    }

    private List<String> prepareCommand(List<String> systemProperties) {
        List<String> command = new LinkedList<>();
        if (getRunner().getFileName().toString().endsWith(".jar")) {
            command.add(JAVA);
            if (enableProfiling()) {
                command.addAll(getProfilingProperties());
            }

            command.addAll(systemProperties);
            command.add("-jar");
            command.add(getRunner().toAbsolutePath().toString());
        } else {
            command.add(getRunner().toAbsolutePath().toString());
            command.addAll(systemProperties);
        }

        return command;
    }

    protected Collection<String> getProfilingProperties() {
        return Arrays.asList("-XX:+FlightRecorder", "-XX:StartFlightRecording=filename="
                + context.getServiceFolder().toAbsolutePath().resolve("profile.jfr"));
    }

    private void assignPorts() {
        assignedHttpPort = getOrAssignPortByProperty(getHttpPortProperty());

        if (enableSsl()) {
            assignedSslPort = getOrAssignPortByProperty(getSslPortProperty());
        }
    }

    private String getComputedValue(String value) {
        for (String prefix : PREFIXES_TO_REPLACE) {
            if (value.startsWith(prefix)) {
                return StringUtils.removeStart(value, prefix);
            }
        }

        return value;
    }

    private int getOrAssignPortByProperty(String property) {
        return context.getOwner().getProperty(property).filter(StringUtils::isNotEmpty).map(Integer::parseInt)
                .orElseGet(() -> SocketUtils.findAvailablePort(context.getOwner()));
    }
}
