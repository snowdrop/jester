package io.jcloud.resources.containers.local;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Network;

import com.github.dockerjava.api.command.CreateNetworkCmd;

import io.jcloud.core.ScenarioContext;

public class DockerScenarioNetwork implements Network, ExtensionContext.Store.CloseableResource {

    private final ScenarioContext scenario;

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

    @Override
    public void close() {
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
}
