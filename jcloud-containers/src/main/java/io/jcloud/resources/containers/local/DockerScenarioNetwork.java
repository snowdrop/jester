package io.jcloud.resources.containers.local;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Network;

import com.github.dockerjava.api.command.CreateNetworkCmd;

import io.jcloud.core.ScenarioContext;
import io.jcloud.core.ServiceContext;

public class DockerScenarioNetwork implements Network, ExtensionContext.Store.CloseableResource {

    private final ScenarioContext scenario;
    private final Set<ServiceContext> services = new HashSet<>();

    public DockerScenarioNetwork(ScenarioContext scenario) {
        this.scenario = scenario;
        CreateNetworkCmd createNetworkCmd = DockerClientFactory.instance().client().createNetworkCmd();
        createNetworkCmd.withName(scenario.getId());
        createNetworkCmd.withCheckDuplicate(true);
        createNetworkCmd.exec();
    }

    @Override
    public String getId() {
        return scenario.getId();
    }

    public void attachService(ServiceContext service) {
        services.add(service);
    }

    @Override
    public void close() {
        for (ServiceContext service : services) {
            try {
                service.getOwner().close();
            } catch (Throwable ignored) {

            }
        }

        try {
            DockerClientFactory.instance().client().removeNetworkCmd(scenario.getId()).exec();
        } catch (Exception ignored) {
        }
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        // SMELL: This is from JUnit5... do nothing then
        return statement;
    }

    private void closeSilently(ExtensionContext.Store.CloseableResource closeable) {

    }
}
