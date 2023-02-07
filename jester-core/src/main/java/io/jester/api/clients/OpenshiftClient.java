package io.jester.api.clients;

import static io.jester.utils.ManifestsUtils.LABEL_CONTEXT_ID;
import static io.jester.utils.ManifestsUtils.LABEL_TO_WATCH_FOR_LOGS;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.jester.api.Service;
import io.jester.logging.Log;
import io.jester.utils.AwaitilitySettings;
import io.jester.utils.AwaitilityUtils;
import io.jester.utils.Command;
import io.jester.utils.KeyValueEntry;
import io.jester.utils.SocketUtils;

public final class OpenshiftClient {

    private static final int PROJECT_NAME_SIZE = 10;
    private static final int PROJECT_CREATION_RETRIES = 5;

    private static final String OC = "oc";

    private static final String PORT_FORWARD_HOST = "localhost";

    private final String currentProject;
    private final DefaultOpenShiftClient masterClient;

    private final NamespacedOpenShiftClient client;
    private final Map<String, KeyValueEntry<Service, LocalPortForwardWrapper>> portForwardsByService = new HashMap<>();

    private OpenshiftClient(String namespace) {
        currentProject = namespace;
        Config config = new ConfigBuilder().withTrustCerts(true).withNamespace(currentProject).build();
        masterClient = new DefaultOpenShiftClient(config);
        client = masterClient.inNamespace(currentProject);
    }

    /**
     * @return the fabric8 kubernetes client that is used internally.
     */
    public NamespacedOpenShiftClient underlyingClient() {
        return client;
    }

    /**
     * @return the current namespace
     */
    public String namespace() {
        return currentProject;
    }

    /**
     * Apply the file into OpenShift.
     *
     * @param file
     */
    public void apply(Path file) {
        try {
            new Command(OC, "apply", "-f", file.toAbsolutePath().toString(), "-n", currentProject).runAndWait();
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply resource " + file.toAbsolutePath(), e);
        }
    }

    /**
     * Delete the file into OpenShift.
     *
     * @param file
     */
    public void delete(Path file) {
        try {
            new Command(OC, "delete", "-f", file.toAbsolutePath().toString(), "-n", currentProject).runAndWait();
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
        List portList = IntStream.of(ports).mapToObj(Integer::toString).collect(Collectors.toList());
        portList.forEach(port -> {
            try {
                new Command(OC, "expose", "deployment", service.getName(), "--port=" + port,
                        String.format("--target-port=%s", port), "--name=" + service.getName(), "-n", currentProject)
                                .runAndWait();
                new Command(OC, "expose", "svc", service.getName(), "--port=" + port, "--name=" + service.getName(),
                        "-n", currentProject).runAndWait();
            } catch (Exception e) {
                throw new RuntimeException("Service failed to be exposed.", e);
            }
        });
    }

    public void rollout(DeploymentConfig deploymentConfig) {
        try {
            new Command(OC, "rollout", "latest",
                    deploymentConfig.getFullResourceName() + "/" + deploymentConfig.getMetadata().getName(), "-n",
                    currentProject).runAndWait();
        } catch (Exception e) {
            throw new RuntimeException("Service failed to be rolled out.", e);
        }
    }

    public void rollout(Deployment deployment) {
        try {
            new Command(OC, "rollout", "latest",
                    deployment.getFullResourceName() + "/" + deployment.getMetadata().getName(), "-n", currentProject)
                            .runAndWait();
        } catch (Exception e) {
            throw new RuntimeException("Service failed to be rolled out.", e);
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
            new Command(OC, "scale", "deployment/" + service.getName(), "--replicas=" + replicas, "-n", currentProject)
                    .runAndWait();
            final String rcName = service.getName() + "-1";
            AwaitilityUtils.untilIsTrue(() -> client.apps().deployments().withName(service.getName()).get().getSpec()
                    .getReplicas() == replicas, AwaitilitySettings.defaults().withService(service));
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
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
        Route route = client.routes().withName(serviceName).get();
        if (route == null || route.getSpec() == null) {
            return PORT_FORWARD_HOST;
        }

        return route.getSpec().getHost();
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
        Route route = client.routes().withName(serviceName).get();
        if (route == null || route.getSpec() == null || route.getSpec().getPort() == null) {
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

        return route.getSpec().getPort().getTargetPort().getIntVal();
    }

    /**
     * @return events of the namespace.
     */
    public String getEvents() {
        List<String> output = new ArrayList<>();
        try {
            new Command(OC, "get", "events", "-n", currentProject).outputToLines(output).runAndWait();
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
            new Command(OC, "delete", "namespace", currentProject).runAndWait();
        } catch (Exception e) {
            throw new RuntimeException("Project failed to be deleted.", e);
        } finally {
            masterClient.close();
        }
    }

    /**
     * Delete all the resources within the test.
     */
    public void deleteResourcesInJesterContext(String contextId) {
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
            new Command(OC, "delete", "-n", currentProject, "all", "-l", label).runAndWait();
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
            new Command(OC, "get", "svc", service.getName(), "-n", currentProject).outputToConsole().runAndWait();
        } catch (Exception ignored) {
        }
    }

    public static OpenshiftClient createClientUsingCurrentNamespace() {
        return new OpenshiftClient(new DefaultOpenShiftClient().getNamespace());
    }

    public static OpenshiftClient createClientUsingANewNamespace() {
        boolean namespaceCreated = false;

        String namespace = generateRandomNamespaceName();
        int index = 0;
        while (index < PROJECT_CREATION_RETRIES) {
            if (doCreateNamespace(namespace)) {
                namespaceCreated = true;
                break;
            }

            namespace = generateRandomNamespaceName();
            index++;
        }

        if (!namespaceCreated) {
            throw new RuntimeException("Namespace cannot be created. Review your OpenShift installation.");
        }

        return new OpenshiftClient(namespace);
    }

    private static boolean doCreateNamespace(String namespaceName) {
        boolean created = false;
        try {
            new Command(OC, "create", "namespace", namespaceName).runAndWait();
            created = true;
        } catch (Exception e) {
            Log.warn("Namespace " + namespaceName + " failed to be created. Caused by: " + e.getMessage()
                    + ". Trying again.");
        }

        return created;
    }

    private static String generateRandomNamespaceName() {
        return ThreadLocalRandom.current().ints(PROJECT_NAME_SIZE, 'a', 'z' + 1)
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
