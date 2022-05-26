package io.jester.resources.containers.local;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Network;

import com.github.dockerjava.api.command.CreateNetworkCmd;

import io.jester.core.JesterContext;
import io.jester.core.ServiceContext;

public class DockerJesterNetwork implements Network, ExtensionContext.Store.CloseableResource {

    private static final String NETWORK = "internal.container.network";

    private final JesterContext context;
    private final Set<ServiceContext> services = new HashSet<>();

    public DockerJesterNetwork(JesterContext context) {
        this.context = context;
        CreateNetworkCmd createNetworkCmd = DockerClientFactory.instance().client().createNetworkCmd();
        createNetworkCmd.withName(context.getId());
        createNetworkCmd.withCheckDuplicate(true);
        createNetworkCmd.exec();
    }

    @Override
    public String getId() {
        return context.getId();
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
            DockerClientFactory.instance().client().removeNetworkCmd(context.getId()).exec();
        } catch (Exception ignored) {
        }

        context.getTestStore().remove(NETWORK);
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        // SMELL: This is from JUnit5... do nothing then
        return statement;
    }

    public static final DockerJesterNetwork getOrCreate(JesterContext context) {
        return context.getTestStore().getOrComputeIfAbsent(NETWORK, k -> new DockerJesterNetwork(context),
                DockerJesterNetwork.class);
    }
}
