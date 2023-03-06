package io.github.jester.resources.common.local;

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

import io.github.jester.configuration.DockerServiceConfiguration;
import io.github.jester.configuration.DockerServiceConfigurationBuilder;
import io.github.jester.core.ManagedResource;
import io.github.jester.core.ServiceContext;
import io.github.jester.logging.Log;
import io.github.jester.logging.LoggingHandler;
import io.github.jester.logging.TestContainersLoggingHandler;
import io.github.jester.resources.containers.local.DockerJesterNetwork;
import io.github.jester.utils.PropertiesUtils;

public abstract class GenericContainerManagedResource extends ManagedResource {

    private static final String TARGET = "target";

    private final String expectedLog;
    private final String[] command;
    private final Integer[] ports;

    private DockerJesterNetwork network;
    private GenericContainer<?> innerContainer;
    private LoggingHandler loggingHandler;

    public GenericContainerManagedResource(String expectedLog, String[] command, int[] ports) {
        this.command = PropertiesUtils.resolveProperties(command);
        this.expectedLog = PropertiesUtils.resolveProperty(expectedLog);
        this.ports = Arrays.stream(ports).boxed().toArray(Integer[]::new);
    }

    protected abstract String getImage();

    @Override
    public void start() {
        if (isRunning()) {
            return;
        }

        network = DockerJesterNetwork.getOrCreate(context.getJesterContext());
        network.attachService(context);

        innerContainer = new GenericContainer<>(getImage());

        if (StringUtils.isNotBlank(expectedLog)) {
            innerContainer.waitingFor(new LogMessageWaitStrategy().withRegEx(".*" + expectedLog + ".*\\s"));
        }

        if (command != null && command.length > 0) {
            innerContainer.withCommand(command);
        }

        if (isPrivileged()) {
            Log.info(context.getOwner(), "Running container on Privileged mode");
            innerContainer.setPrivilegedMode(true);
        }

        innerContainer.withExposedPorts(ports);
        innerContainer.withNetwork(network);
        innerContainer.withNetworkAliases(context.getName());
        innerContainer.withStartupTimeout(context.getConfiguration().getStartupTimeout());
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
    public String getHost() {
        return innerContainer.getHost();
    }

    @Override
    public int getFirstMappedPort() {
        return getMappedPort(ports[0]);
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

    @Override
    protected void init(ServiceContext context) {
        super.init(context);
        context.loadCustomConfiguration(DockerServiceConfiguration.class, new DockerServiceConfigurationBuilder());
    }

    private boolean isPrivileged() {
        return context.getConfigurationAs(DockerServiceConfiguration.class).isPrivileged();
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
                value = entry.getValue().replace(PropertiesUtils.RESOURCE_PREFIX, StringUtils.EMPTY);
                addFileToContainer(value);
            } else if (isSecret(entry.getValue())) {
                value = entry.getValue().replace(PropertiesUtils.SECRET_PREFIX, StringUtils.EMPTY);
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
        return key.startsWith(PropertiesUtils.RESOURCE_PREFIX);
    }

    private boolean isSecret(String key) {
        return key.startsWith(PropertiesUtils.SECRET_PREFIX);
    }

}
