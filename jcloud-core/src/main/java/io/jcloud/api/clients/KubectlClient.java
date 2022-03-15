package io.jcloud.api.clients;

import static io.jcloud.utils.ManifestsUtils.LABEL_SCENARIO_ID;
import static io.jcloud.utils.ManifestsUtils.LABEL_TO_WATCH_FOR_LOGS;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.jcloud.api.Service;
import io.jcloud.configuration.PropertyLookup;
import io.jcloud.logging.Log;
import io.jcloud.utils.Command;
import io.jcloud.utils.SocketUtils;

public final class KubectlClient {

    public static final PropertyLookup ENABLED_EPHEMERAL_NAMESPACES = new PropertyLookup(
            "ts.kubernetes.ephemeral.namespaces.enabled", Boolean.TRUE.toString());

    private static final int NAMESPACE_NAME_SIZE = 10;
    private static final int NAMESPACE_CREATION_RETRIES = 5;

    private static final String KUBECTL = "kubectl";
    private static final int HTTP_PORT_DEFAULT = 80;
    private static final String PORT_FORWARD_HOST = "localhost";

    private final String currentNamespace;
    private final DefaultKubernetesClient masterClient;
    private final NamespacedKubernetesClient client;
    private final Map<String, LocalPortForward> portForwardsByService = new HashMap<>();

    private KubectlClient() {
        if (ENABLED_EPHEMERAL_NAMESPACES.getAsBoolean()) {
            currentNamespace = createNamespace();
        } else {
            currentNamespace = new DefaultKubernetesClient().getNamespace();
        }

        Config config = new ConfigBuilder().withTrustCerts(true).withNamespace(currentNamespace).build();
        masterClient = new DefaultKubernetesClient(config);
        client = masterClient.inNamespace(currentNamespace);
    }

    public static KubectlClient create() {
        return new KubectlClient();
    }

    /**
     * @return the fabric8 kubernetes client that is used internally.
     */
    public NamespacedKubernetesClient underlyingClient() {
        return client;
    }

    /**
     * @return the current namespace
     */
    public String namespace() {
        return currentNamespace;
    }

    /**
     * Apply the file into Kubernetes.
     *
     * @param file
     */
    public void apply(Service service, Path file) {
        try {
            new Command(KUBECTL, "apply", "-f", file.toAbsolutePath().toString(), "-n", currentNamespace)
                    .runAndWait();
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply resource " + file.toAbsolutePath() + " for " + service.getName(), e);
        }
    }

    /**
     * Expose the service and port defined.
     *
     * @param service
     * @param port
     */
    public void expose(Service service, Integer port) {
        try {
            new Command(KUBECTL, "expose", "deployment", service.getName(), "--port=" + port, "--name=" + service.getName(),
                    "-n", currentNamespace).runAndWait();
        } catch (Exception e) {
            throw new RuntimeException("Service failed to be exposed.", e);
        }
    }

    /**
     * Scale the service to the replicas.
     *
     * @param service
     * @param replicas
     */
    public void scaleTo(Service service, int replicas) {
        try {
            new Command(KUBECTL, "scale", "deployment/" + service.getName(), "--replicas=" + replicas, "-n", currentNamespace)
                    .runAndWait();
        } catch (Exception e) {
            throw new RuntimeException("Service failed to be scaled.", e);
        }
    }

    /**
     * Get the running pods in the current service.
     */
    public List<Pod> podsInService(Service service) {
        return client.pods().withLabel(LABEL_TO_WATCH_FOR_LOGS, service.getName()).list().getItems();
    }

    /**
     * Get all the logs for all the pods within the current namespace.
     *
     * @return
     */
    public Map<String, String> logs() {
        Map<String, String> logs = new HashMap<>();
        for (Pod pod : client.pods().list().getItems()) {
            String podName = pod.getMetadata().getName();
            logs.put(podName, client.pods().withName(podName).getLog());
        }

        return logs;
    }

    /**
     * Get all the logs for all the pods within one service.
     *
     * @param service
     * @return
     */
    public Map<String, String> logs(Service service) {
        Map<String, String> logs = new HashMap<>();
        for (Pod pod : podsInService(service)) {
            if (isPodRunning(pod)) {
                String podName = pod.getMetadata().getName();
                logs.put(podName, client.pods().withName(podName).getLog());
            }
        }

        return logs;
    }

    /**
     * Resolve the url by the service.
     *
     * @param service
     * @return
     */
    public String host(Service service) {
        String serviceName = service.getName();
        io.fabric8.kubernetes.api.model.Service serviceModel = client.services().withName(serviceName).get();
        if (serviceModel == null
                || serviceModel.getStatus() == null
                || serviceModel.getStatus().getLoadBalancer() == null
                || serviceModel.getStatus().getLoadBalancer().getIngress() == null) {
            return PORT_FORWARD_HOST;
        }

        // IP detection rules:
        // 1.- Try Ingress IP
        // 2.- Try Ingress Hostname
        Optional<String> ip = serviceModel.getStatus().getLoadBalancer().getIngress().stream()
                .map(ingress -> StringUtils.defaultIfBlank(ingress.getIp(), ingress.getHostname()))
                .filter(StringUtils::isNotEmpty)
                .findFirst();

        if (ip.isEmpty()) {
            return PORT_FORWARD_HOST;
        }

        return ip.get();
    }

    /**
     * Resolve the port by the service.
     *
     * @param service
     * @return
     */
    public int port(Service service, int port) {
        String serviceName = service.getName();
        io.fabric8.kubernetes.api.model.Service serviceModel = client.services().withName(serviceName).get();
        if (serviceModel == null || serviceModel.getSpec() == null || serviceModel.getSpec().getPorts() == null) {
            throw new RuntimeException("Service " + serviceName + " not found");
        }

        if (PORT_FORWARD_HOST.equalsIgnoreCase(host(service))) {
            LocalPortForward portForward = portForwardsByService.get(serviceName);
            if (portForward == null || !portForward.isAlive()) {
                portForward = client.services().withName(serviceName).portForward(port, SocketUtils.findAvailablePort());
                portForwardsByService.put(serviceName, portForward);
            }

            return portForward.getLocalPort();
        }

        return serviceModel.getSpec().getPorts().stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getTargetPort().getIntVal() == port)
                .map(ServicePort::getPort)
                .findFirst()
                .orElse(HTTP_PORT_DEFAULT);
    }

    /**
     * Delete the namespace and all the resources.
     */
    public void deleteNamespace(String scenarioId) {
        portForwardsByService.values().forEach(this::forceClose);

        if (ENABLED_EPHEMERAL_NAMESPACES.getAsBoolean()) {
            try {
                new Command(KUBECTL, "delete", "namespace", currentNamespace).runAndWait();
            } catch (Exception e) {
                throw new RuntimeException("Project failed to be deleted.", e);
            } finally {
                masterClient.close();
            }
        } else {
            deleteResourcesByLabel(LABEL_SCENARIO_ID, scenarioId);
        }
    }

    /**
     * Stop service and free its resources.
     *
     * @param service
     */
    public void stopService(Service service) {
        Optional.ofNullable(portForwardsByService.remove(service.getName())).ifPresent(this::forceClose);
        scaleTo(service, 0);
    }

    /**
     * Delete test resources.
     */
    private void deleteResourcesByLabel(String labelName, String labelValue) {
        try {
            String label = String.format("%s=%s", labelName, labelValue);
            new Command(KUBECTL, "delete", "-n", currentNamespace, "all", "-l", label).runAndWait();
        } catch (Exception e) {
            throw new RuntimeException("Resources failed to be deleted.", e);
        } finally {
            masterClient.close();
        }
    }

    private boolean isPodRunning(Pod pod) {
        return pod.getStatus().getPhase().equals("Running");
    }

    private String createNamespace() {
        boolean namespaceCreated = false;

        String namespace = generateRandomNamespaceName();
        int index = 0;
        while (index < NAMESPACE_CREATION_RETRIES) {
            if (doCreateNamespace(namespace)) {
                namespaceCreated = true;
                break;
            }

            namespace = generateRandomNamespaceName();
            index++;
        }

        if (!namespaceCreated) {
            throw new RuntimeException("Namespace cannot be created. Review your Kubernetes installation.");
        }

        return namespace;
    }

    private boolean doCreateNamespace(String namespaceName) {
        boolean created = false;
        try {
            new Command(KUBECTL, "create", "namespace", namespaceName).runAndWait();
            created = true;
        } catch (Exception e) {
            Log.warn("Namespace " + namespaceName + " failed to be created. Caused by: " + e.getMessage() + ". Trying again.");
        }

        return created;
    }

    private String generateRandomNamespaceName() {
        return ThreadLocalRandom.current().ints(NAMESPACE_NAME_SIZE, 'a', 'z' + 1)
                .collect(() -> new StringBuilder("ts-"), StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private void forceClose(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException ignored) {
            // ignored
        }
    }

    private void printServiceInfo(Service service) {
        try {
            new Command(KUBECTL, "get", "svc", service.getName(), "-n", currentNamespace)
                    .outputToConsole()
                    .runAndWait();
        } catch (Exception ignored) {
        }
    }

}
