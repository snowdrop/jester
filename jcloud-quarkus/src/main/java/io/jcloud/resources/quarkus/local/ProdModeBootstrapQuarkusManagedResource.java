package io.jcloud.resources.quarkus.local;

import static io.jcloud.utils.PropertiesUtils.RESOURCE_PREFIX;
import static io.jcloud.utils.PropertiesUtils.SECRET_PREFIX;
import static io.jcloud.utils.QuarkusUtils.APPLICATION_PROPERTIES;
import static io.jcloud.utils.QuarkusUtils.QUARKUS_HTTP_PORT_PROPERTY;
import static io.jcloud.utils.QuarkusUtils.RESOURCES_FOLDER;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.jcloud.api.Dependency;
import io.jcloud.core.ManagedResource;
import io.jcloud.core.ServiceContext;
import io.jcloud.logging.FileServiceLoggingHandler;
import io.jcloud.logging.Log;
import io.jcloud.logging.LoggingHandler;
import io.jcloud.resources.quarkus.BootstrapQuarkusProxy;
import io.jcloud.utils.ProcessBuilderProvider;
import io.jcloud.utils.ProcessUtils;
import io.jcloud.utils.PropertiesUtils;
import io.jcloud.utils.SocketUtils;

public class ProdModeBootstrapQuarkusManagedResource extends ManagedResource {

    private static final String LOCALHOST = "localhost";
    private static final List<String> PREFIXES_TO_REPLACE = Arrays.asList(RESOURCE_PREFIX, SECRET_PREFIX);
    private static final String LOG_OUTPUT_FILE = "out.log";
    private static final String JAVA = "java";

    private final String propertiesFile;
    private final Class<?>[] classes;
    private final Dependency[] forcedDependencies;

    private BootstrapQuarkusProxy proxy;
    private File logOutputFile;
    private Process process;
    private LoggingHandler loggingHandler;
    private int assignedHttpPort;

    public ProdModeBootstrapQuarkusManagedResource(String propertiesFile, Class<?>[] classes,
            Dependency[] forcedDependencies) {
        this.propertiesFile = propertiesFile;
        this.classes = classes;
        this.forcedDependencies = forcedDependencies;
    }

    @Override
    public String getDisplayName() {
        return proxy.getDisplayName();
    }

    @Override
    public void start() {
        if (process != null && process.isAlive()) {
            // do nothing
            return;
        }

        try {
            assignPorts();
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
    public int getMappedPort(int port) {
        return assignedHttpPort;
    }

    @Override
    public boolean isRunning() {
        return process != null && process.isAlive() && proxy.isRunning(loggingHandler);
    }

    @Override
    public boolean isFailed() {
        return super.isFailed() || proxy.isFailed(loggingHandler);
    }

    @Override
    public String getProperty(String name) {
        Path applicationProperties = getComputedApplicationProperties();
        if (!Files.exists(applicationProperties)) {
            // computed properties have not been propagated yet, we use the one from src/main/resources
            applicationProperties = RESOURCES_FOLDER.resolve(propertiesFile);
        }

        if (!Files.exists(applicationProperties)) {
            return null;
        }

        Map<String, String> computedProperties = PropertiesUtils.toMap(applicationProperties);
        return Optional.ofNullable(computedProperties.get(name))
                .orElseGet(() -> computedProperties.get(propertyWithProfile(name)));
    }

    @Override
    protected LoggingHandler getLoggingHandler() {
        return loggingHandler;
    }

    @Override
    protected void init(ServiceContext context) {
        super.init(context);
        proxy = new BootstrapQuarkusProxy(context, propertiesFile, classes, forcedDependencies);
        this.logOutputFile = new File(context.getServiceFolder().resolve(LOG_OUTPUT_FILE).toString());
    }

    private List<String> prepareCommand(List<String> systemProperties) {
        List<String> command = new LinkedList<>();
        if (proxy.getArtifact().getFileName().toString().endsWith(".jar")) {
            command.add(JAVA);
            command.addAll(systemProperties);
            command.add("-jar");
            command.add(proxy.getArtifact().toAbsolutePath().toString());
        } else {
            command.add(proxy.getArtifact().toAbsolutePath().toString());
            command.addAll(systemProperties);
        }

        return command;
    }

    private List<String> getPropertiesForCommand() {
        Map<String, String> runtimeProperties = new HashMap<>(context.getOwner().getProperties());
        runtimeProperties.putIfAbsent(QUARKUS_HTTP_PORT_PROPERTY, "" + assignedHttpPort);

        return runtimeProperties.entrySet().stream().map(e -> "-D" + e.getKey() + "=" + getComputedValue(e.getValue()))
                .collect(Collectors.toList());
    }

    private String getComputedValue(String value) {
        for (String prefix : PREFIXES_TO_REPLACE) {
            if (value.startsWith(prefix)) {
                return StringUtils.removeStart(value, prefix);
            }
        }

        return value;
    }

    private Path getComputedApplicationProperties() {
        return context.getServiceFolder().resolve(APPLICATION_PROPERTIES);
    }

    private String propertyWithProfile(String name) {
        return "%" + context.getScenarioContext().getRunningTestClassName() + "." + name;
    }

    private void assignPorts() {
        assignedHttpPort = getOrAssignPortByProperty(QUARKUS_HTTP_PORT_PROPERTY);
    }

    private int getOrAssignPortByProperty(String property) {
        return context.getOwner().getProperty(property).filter(StringUtils::isNotEmpty).map(Integer::parseInt)
                .orElseGet(SocketUtils::findAvailablePort);
    }

}
