package io.jcloud.api.clients;

import static io.jcloud.utils.ManifestsUtils.LABEL_CONTEXT_ID;
import static io.jcloud.utils.ManifestsUtils.LABEL_TO_WATCH_FOR_LOGS;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.jcloud.api.Service;
import io.jcloud.logging.Log;
import io.jcloud.utils.AwaitilityUtils;
import io.jcloud.utils.Command;
import io.jcloud.utils.KeyValueEntry;
import io.jcloud.utils.SocketUtils;

public final class KubectlClient {

    private static final int NAMESPACE_NAME_SIZE = 10;
    private static final int NAMESPACE_CREATION_RETRIES = 5;

    private static final String KUBECTL = "kubectl";
    private static final int HTTP_PORT_DEFAULT = 80;
    private static final String PORT_FORWARD_HOST = "localhost";

    private final String currentNamespace;
    private final DefaultKubernetesClient masterClient;
    private final NamespacedKubernetesClient client;
    private final Map<String, KeyValueEntry<Service, LocalPortForwardWrapper>> portForwardsByService = new HashMap<>();

    private KubectlClient(String namespace) {
        currentNamespace = namespace;
        Config config = new ConfigBuilder().withTrustCerts(true).withNamespace(currentNamespace).build();
        masterClient = new DefaultKubernetesClient(config);
        client = masterClient.inNamespace(currentNamespace);
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
    public void apply(Path file) {
        try {
            new Command(KUBECTL, "apply", "-f", file.toAbsolutePath().toString(), "-n", currentNamespace).runAndWait();
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply resource " + file.toAbsolutePath(), e);
        }
    }

    /**
     * Delete the file into Kubernetes.
     *
     * @param file
     */
    public void delete(Path file) {
        try {
            new Command(KUBECTL, "delete", "-f", file.toAbsolutePath().toString(), "-n", currentNamespace).runAndWait();
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply resource " + file.toAbsolutePath(), e);
        }
    }

    /**
     * Expose the service and port defined.
     *
     * @param service
     * @param ports
     */
    public void expose(Service service, int... ports) {
        try {
            new Command(KUBECTL, "expose", "deployment", service.getName(),
                    "--port=" + IntStream.of(ports).mapToObj(Integer::toString).collect(Collectors.joining(",")),
                    "--name=" + service.getName(), "-n", currentNamespace).runAndWait();
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
            new Command(KUBECTL, "scale", "deployment/" + service.getName(), "--replicas=" + replicas, "-n",
                    currentNamespace).runAndWait();

            AwaitilityUtils.untilIsTrue(
                    () -> client.apps().deployments().withName(service.getName()).get().getSpec()
                            .getReplicas() == replicas,
                    AwaitilityUtils.AwaitilitySettings.defaults().withService(service));
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
            try {
                logs.put(podName, client.pods().withName(podName).getLog());
            } catch (Exception ignored) {
                // the pod contains multiple container, and we don't support this use case yet, ignoring exception.
            }

        }

        return logs;
    }

    /**
     * Get all the logs for all the pods within one service.
     *
     * @param service
     *
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
     *
     * @return
     */
    public String host(Service service) {
        String serviceName = service.getName();
        io.fabric8.kubernetes.api.model.Service serviceModel = client.services().withName(serviceName).get();
        if (serviceModel == null || serviceModel.getStatus() == null
                || serviceModel.getStatus().getLoadBalancer() == null
                || serviceModel.getStatus().getLoadBalancer().getIngress() == null) {
            return PORT_FORWARD_HOST;
        }

        // IP detection rules:
        // 1.- Try Ingress IP
        // 2.- Try Ingress Hostname
        Optional<String> ip = serviceModel.getStatus().getLoadBalancer().getIngress().stream()
                .map(ingress -> StringUtils.defaultIfBlank(ingress.getIp(), ingress.getHostname()))
                .filter(StringUtils::isNotEmpty).findFirst();

        if (ip.isEmpty()) {
            return PORT_FORWARD_HOST;
        }

        return ip.get();
    }

    /**
     * Resolve the port by the service.
     *
     * @param service
     *
     * @return
     */
    public int port(Service service, int port) {
        String serviceName = service.getName();
        io.fabric8.kubernetes.api.model.Service serviceModel = client.services().withName(serviceName).get();
        if (serviceModel == null || serviceModel.getSpec() == null || serviceModel.getSpec().getPorts() == null) {
            throw new RuntimeException("Service " + serviceName + " not found");
        }

        if (PORT_FORWARD_HOST.equalsIgnoreCase(host(service))) {
            String svcPortForwardKey = serviceName + "-" + port;
            KeyValueEntry<Service, LocalPortForwardWrapper> portForwardByService = portForwardsByService
                    .get(svcPortForwardKey);
            if (portForwardByService == null || portForwardByService.getValue().needsToRecreate()) {
                closePortForward(portForwardByService);
                LocalPortForward process = client.services().withName(serviceName).portForward(port,
                        SocketUtils.findAvailablePort(service));
                Log.trace(service, "Opening port forward from local port " + process.getLocalPort());

                portForwardByService = new KeyValueEntry<>(service, new LocalPortForwardWrapper(process, service));
                portForwardsByService.put(svcPortForwardKey, portForwardByService);
            }

            return portForwardByService.getValue().localPort;
        }

        return serviceModel.getSpec().getPorts().stream().filter(Objects::nonNull)
                .filter(s -> s.getTargetPort().getIntVal() == port).map(ServicePort::getPort).findFirst()
                .orElse(HTTP_PORT_DEFAULT);
    }

    /**
     * @return events of the namespace.
     */
    public String getEvents() {
        List<String> output = new ArrayList<>();
        try {
            new Command(KUBECTL, "get", "events", "-n", currentNamespace).outputToLines(output).runAndWait();
        } catch (Exception ex) {
            Log.warn("Failed to get namespace events", ex);
        }

        return output.stream().collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * Delete the namespace and all the resources.
     */
    public void deleteNamespace() {
        portForwardsByService.values().forEach(this::closePortForward);
        try {
            new Command(KUBECTL, "delete", "namespace", currentNamespace).runAndWait();
        } catch (Exception e) {
            throw new RuntimeException("Project failed to be deleted.", e);
        } finally {
            masterClient.close();
        }
    }

    /**
     * Delete all the resources within the test.
     */
    public void deleteResourcesInJCloudContext(String contextId) {
        portForwardsByService.values().forEach(this::closePortForward);
        deleteResourcesByLabel(LABEL_CONTEXT_ID, contextId);
    }

    /**
     * Stop service and free its resources.
     *
     * @param service
     */
    public void stopService(Service service) {
        scaleTo(service, 0);
    }

    /**
     * Delete test resources.
     */
    public void deleteResourcesByLabel(String labelName, String labelValue) {
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

    private void closePortForward(KeyValueEntry<Service, LocalPortForwardWrapper> portForward) {
        if (portForward != null) {
            int localPort = portForward.getValue().localPort;
            Log.trace(portForward.getKey(), "Closing port forward using local port " + localPort);
            try {
                portForward.getValue().process.close();
                AwaitilityUtils.untilIsFalse(portForward.getValue().process::isAlive);
            } catch (IOException ex) {
                Log.warn("Failed to close port forward " + localPort, ex);
            }
        }
    }

    private void printServiceInfo(Service service) {
        try {
            new Command(KUBECTL, "get", "svc", service.getName(), "-n", currentNamespace).outputToConsole()
                    .runAndWait();
        } catch (Exception ignored) {
        }
    }

    public static KubectlClient createClientUsingCurrentNamespace() {
        return new KubectlClient(new DefaultKubernetesClient().getNamespace());
    }

    public static KubectlClient createClientUsingANewNamespace() {
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

        return new KubectlClient(namespace);
    }

    private static boolean doCreateNamespace(String namespaceName) {
        boolean created = false;
        try {
            new Command(KUBECTL, "create", "namespace", namespaceName).runAndWait();
            created = true;
        } catch (Exception e) {
            Log.warn("Namespace " + namespaceName + " failed to be created. Caused by: " + e.getMessage()
                    + ". Trying again.");
        }

        return created;
    }

    private static String generateRandomNamespaceName() {
        return ThreadLocalRandom.current().ints(NAMESPACE_NAME_SIZE, 'a', 'z' + 1)
                .collect(() -> new StringBuilder("ts-"), StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    class LocalPortForwardWrapper {
        int localPort;
        LocalPortForward process;
        Service service;
        Set<String> podIds;

        LocalPortForwardWrapper(LocalPortForward process, Service service) {
            this.localPort = process.getLocalPort();
            this.process = process;
            this.service = service;
            this.podIds = resolvePodIds();
        }

        /**
         * Needs to recreate the port forward if the pods have changed or the process was stopped.
         */
        boolean needsToRecreate() {
            if (!process.isAlive()) {
                return true;
            }

            Set<String> newPodIds = resolvePodIds();
            return !podIds.containsAll(newPodIds);
        }

        private Set<String> resolvePodIds() {
            return podsInService(service).stream().map(p -> p.getMetadata().getName()).collect(Collectors.toSet());
        }
    }

}
