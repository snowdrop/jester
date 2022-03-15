package io.jcloud.resources.containers.local;

import static io.jcloud.utils.PropertiesUtils.RESOURCE_PREFIX;
import static io.jcloud.utils.PropertiesUtils.SECRET_PREFIX;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.MountableFile;

import io.jcloud.core.ManagedResource;
import io.jcloud.logging.Log;
import io.jcloud.logging.LoggingHandler;
import io.jcloud.logging.TestContainersLoggingHandler;
import io.jcloud.utils.PropertiesUtils;

public class DockerContainerManagedResource extends ManagedResource {

    private static final String PRIVILEGED_MODE = "container.privileged-mode";
    private static final String TARGET = "target";
    private static final String SCENARIO_NETWORK = "internal.container.network";

    private final String image;
    private final String expectedLog;
    private final String[] command;
    private final Integer[] ports;

    private DockerScenarioNetwork network;
    private GenericContainer<?> innerContainer;
    private LoggingHandler loggingHandler;

    public DockerContainerManagedResource(String image, String expectedLog, String[] command, int[] ports) {
        this.image = PropertiesUtils.resolveProperty(image);
        this.command = command;
        this.expectedLog = PropertiesUtils.resolveProperty(expectedLog);
        this.ports = Arrays.stream(ports).boxed().toArray(Integer[]::new);
    }

    @Override
    public void start() {
        if (isRunning()) {
            return;
        }

        network = context.getScenarioContext().getTestStore().getOrComputeIfAbsent(SCENARIO_NETWORK,
                k -> new DockerScenarioNetwork(context.getScenarioContext()), DockerScenarioNetwork.class);

        innerContainer = initContainer();
        network.attachService(context);
        innerContainer.withNetwork(network);
        innerContainer.withNetworkAliases(context.getName());
        innerContainer.withStartupTimeout(context.getOwner().getConfiguration().getAsDuration(SERVICE_STARTUP_TIMEOUT,
                SERVICE_STARTUP_TIMEOUT_DEFAULT));
        innerContainer.withEnv(resolveProperties());

        loggingHandler = new TestContainersLoggingHandler(context.getOwner(), innerContainer);
        loggingHandler.startWatching();

        doStart();
    }

    @Override
    public void stop() {
        if (loggingHandler != null) {
            loggingHandler.stopWatching();
        }

        if (isRunning()) {
            innerContainer.stop();
            innerContainer = null;
        }
    }

    @Override
    public String getDisplayName() {
        return image;
    }

    @Override
    public String getHost() {
        return innerContainer.getHost();
    }

    @Override
    public int getMappedPort(int port) {
        return innerContainer.getMappedPort(port);
    }

    @Override
    public boolean isRunning() {
        return innerContainer != null && innerContainer.isRunning();
    }

    @Override
    protected LoggingHandler getLoggingHandler() {
        return loggingHandler;
    }

    protected GenericContainer<?> initContainer() {
        GenericContainer<?> container = new GenericContainer<>(image);

        if (StringUtils.isNotBlank(expectedLog)) {
            container.waitingFor(new LogMessageWaitStrategy().withRegEx(".*" + expectedLog + ".*\\s"));
        }

        if (command != null && command.length > 0) {
            container.withCommand(command);
        }

        if (isPrivileged()) {
            Log.info(context.getOwner(), "Running container on Privileged mode");
            container.setPrivilegedMode(true);
        }

        container.withExposedPorts(ports);

        return container;
    }

    private boolean isPrivileged() {
        return context.getOwner().getConfiguration().isTrue(PRIVILEGED_MODE);
    }

    private void doStart() {
        try {
            innerContainer.start();
        } catch (Exception ex) {
            stop();

            throw ex;
        }
    }

    private Map<String, String> resolveProperties() {
        Map<String, String> properties = new HashMap<>();
        for (Entry<String, String> entry : context.getOwner().getProperties().entrySet()) {
            String value = entry.getValue();
            if (isResource(entry.getValue())) {
                value = entry.getValue().replace(RESOURCE_PREFIX, StringUtils.EMPTY);
                addFileToContainer(value);
            } else if (isSecret(entry.getValue())) {
                value = entry.getValue().replace(SECRET_PREFIX, StringUtils.EMPTY);
                addFileToContainer(value);
            }

            properties.put(entry.getKey(), value);
        }
        return properties;
    }

    private void addFileToContainer(String filePath) {
        if (Files.exists(Path.of(TARGET, filePath))) {
            // Mount file if it's a file
            innerContainer.withCopyFileToContainer(MountableFile.forHostPath(Path.of(TARGET, filePath)), filePath);
        } else {
            // then file is in the classpath
            innerContainer.withClasspathResourceMapping(filePath, filePath, BindMode.READ_ONLY);
        }
    }

    private boolean isResource(String key) {
        return key.startsWith(RESOURCE_PREFIX);
    }

    private boolean isSecret(String key) {
        return key.startsWith(SECRET_PREFIX);
    }

}
