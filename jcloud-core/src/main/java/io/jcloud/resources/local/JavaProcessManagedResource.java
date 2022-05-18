package io.jcloud.resources.local;

import static io.jcloud.utils.PropertiesUtils.RESOURCE_PREFIX;
import static io.jcloud.utils.PropertiesUtils.SECRET_PREFIX;

import java.io.File;
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
import io.jcloud.utils.SocketUtils;

public abstract class JavaProcessManagedResource extends ManagedResource {

    private static final String LOCALHOST = "localhost";
    private static final List<String> PREFIXES_TO_REPLACE = Arrays.asList(RESOURCE_PREFIX, SECRET_PREFIX);
    private static final String LOG_OUTPUT_FILE = "out.log";
    private static final String JAVA = "java";

    protected Map<String, String> propertiesToOverwrite = new HashMap<>();

    private File logOutputFile;
    private Process process;
    private LoggingHandler loggingHandler;
    private int assignedHttpPort;
    private Map<Integer, Integer> assignedCustomPorts;

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
        return Optional.ofNullable(assignedCustomPorts.get(port)).orElse(assignedHttpPort);
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

    protected List<String> getPropertiesForCommand() {
        Map<String, String> runtimeProperties = new HashMap<>(context.getOwner().getProperties());
        runtimeProperties.putAll(propertiesToOverwrite);

        return runtimeProperties.entrySet().stream().map(e -> "-D" + e.getKey() + "=" + getComputedValue(e.getValue()))
                .collect(Collectors.toList());
    }

    protected boolean enableProfiling() {
        return context.getJCloudContext().getConfiguration().isProfilingEnabled();
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

    protected Map<Integer, Integer> assignCustomPorts() {
        return Collections.emptyMap();
    }

    protected int getOrAssignPortByProperty(String property) {
        return context.getOwner().getProperty(property).filter(StringUtils::isNotEmpty).map(Integer::parseInt)
                .orElseGet(() -> SocketUtils.findAvailablePort(context.getOwner()));
    }

    private void assignPorts() {
        assignedHttpPort = getOrAssignPortByProperty(getHttpPortProperty());
        propertiesToOverwrite.put(getHttpPortProperty(), "" + assignedHttpPort);

        this.assignedCustomPorts = assignCustomPorts();
    }

    private String getComputedValue(String value) {
        for (String prefix : PREFIXES_TO_REPLACE) {
            if (value.startsWith(prefix)) {
                return StringUtils.removeStart(value, prefix);
            }
        }

        return value;
    }
}
