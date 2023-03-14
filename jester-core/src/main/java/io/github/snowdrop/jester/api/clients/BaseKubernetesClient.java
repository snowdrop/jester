package io.github.snowdrop.jester.api.clients;

import java.io.FileInputStream;
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

import org.apache.commons.lang3.StringUtils;

import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.events.v1.Event;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.fabric8.kubernetes.client.okhttp.OkHttpClientFactory;
import io.github.snowdrop.jester.api.Service;
import io.github.snowdrop.jester.logging.Log;
import io.github.snowdrop.jester.utils.AwaitilitySettings;
import io.github.snowdrop.jester.utils.AwaitilityUtils;
import io.github.snowdrop.jester.utils.KeyValueEntry;
import io.github.snowdrop.jester.utils.ManifestsUtils;
import io.github.snowdrop.jester.utils.SocketUtils;

public abstract class BaseKubernetesClient<T extends KubernetesClient> {

    protected static final String PORT_FORWARD_HOST = "localhost";

    private static final int NAMESPACE_NAME_SIZE = 10;
    private static final int NAMESPACE_CREATION_RETRIES = 5;

    private static final int HTTP_PORT_DEFAULT = 80;

    private final Map<String, KeyValueEntry<Service, LocalPortForwardWrapper>> portForwardsByService = new HashMap<>();

    private String currentNamespace;
    private T client;

    public abstract T initializeClient(Config config);

    /**
     * @return the fabric8 kubernetes client that is used internally.
     */
    public T underlyingClient() {
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
        try (FileInputStream is = new FileInputStream(file.toFile())) {
            Log.info("Applying file at '%s' in namespace '%s'", file.toAbsolutePath().toString(), currentNamespace);
            client.load(is).createOrReplace();
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
        try (FileInputStream is = new FileInputStream(file.toFile())) {
            Log.info("Deleting file at '%s' in namespace '%s'", file.toAbsolutePath().toString(), currentNamespace);
            client.load(is).delete();
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
            Log.info(service, "Exposing deployment '%s' in namespace '%s'", service.getName(), currentNamespace);
            Deployment deployment = client.apps().deployments().withName(service.getName()).get();

            List<ServicePort> servicePorts = new ArrayList<>();
            for (int port : ports) {
                servicePorts.add(new ServicePortBuilder().withName("" + port).withPort(port).build());
            }
            client.services()
                    .resource(new ServiceBuilder().withNewMetadata().withNamespace(currentNamespace)
                            .withName(service.getName()).withLabels(deployment.getMetadata().getLabels()).endMetadata()
                            .withNewSpec().withSelector(deployment.getSpec().getTemplate().getMetadata().getLabels())
                            .withPorts(servicePorts).endSpec().build())
                    .create();
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
            String serviceName = service.getName();

            Deployment deployment = client.apps().deployments().withName(serviceName).get();
            if (deployment.getSpec().getReplicas() != replicas) {
                Log.info(service, "Scaling deployment '%s' in namespace '%s' to '%s'", serviceName, currentNamespace,
                        replicas);
                deployment.getSpec().setReplicas(replicas);
                client.apps().deployments().withName(serviceName).patch(deployment);
                AwaitilityUtils.untilIsTrue(() -> client.apps().deployments().withName(serviceName).get().getSpec()
                        .getReplicas() == replicas, AwaitilitySettings.defaults().withService(service));
            }

        } catch (Exception e) {
            throw new RuntimeException("Service failed to be scaled.", e);
        }
    }

    /**
     * Get the running pods in the current service.
     */
    public List<Pod> podsInService(Service service) {
        return client.pods().withLabel(ManifestsUtils.LABEL_TO_WATCH_FOR_LOGS, service.getName()).list().getItems();
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

        io.fabric8.kubernetes.api.model.Service serviceModel = client.services().withName(serviceName).get();
        if (serviceModel == null || serviceModel.getSpec() == null || serviceModel.getSpec().getPorts() == null) {
            throw new RuntimeException("Service " + serviceName + " not found");
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
            for (Event event : client.events().v1().events().inNamespace(currentNamespace).list().getItems()) {
                output.add(event.toString());
            }

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
        if (client != null) {
            Log.info("Deleting namespace '%s'", currentNamespace);
            try {
                client.namespaces().withName(currentNamespace).delete();
            } catch (Exception e) {
                throw new RuntimeException("Project failed to be deleted.", e);
            } finally {
                client.close();
            }
        }
    }

    /**
     * Delete all the resources within the test.
     */
    public void deleteResourcesInJesterContext(String contextId) {
        portForwardsByService.values().forEach(this::closePortForward);
        deleteResourcesByLabel(ManifestsUtils.LABEL_CONTEXT_ID, contextId);
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
     * Create secret from file.
     */
    public void createSecretFromFile(String secretName, String filePath) {
        if (client.secrets().withName(secretName).get() == null) {
            try {
                client.secrets().load(new FileInputStream(filePath));
            } catch (Exception e) {
                throw new RuntimeException("Could not create secret.", e);
            }
        }
    }

    /**
     * Create or update a config map from file.
     */
    public void createOrUpdateConfigMap(String configMapName, String key, String value) {
        if (client.configMaps().withName(configMapName).get() != null) {
            // update existing config map by adding new file
            client.configMaps().withName(configMapName).edit(configMap -> {
                configMap.getData().put(key, value);
                return configMap;
            });
        } else {
            // create new one
            client.configMaps().createOrReplace(new ConfigMapBuilder().withNewMetadata().withName(configMapName)
                    .endMetadata().addToData(key, value).build());
        }
    }

    /**
     * Delete test resources.
     */
    public void deleteResourcesByLabel(String labelName, String labelValue) {
        try {
            // Deployments
            client.apps().deployments().inNamespace(currentNamespace).withLabel(labelName, labelValue).delete();
            // Revisions
            client.apps().controllerRevisions().inNamespace(currentNamespace).withLabel(labelName, labelValue).delete();
            // Daemons
            client.apps().daemonSets().inNamespace(currentNamespace).withLabel(labelName, labelValue).delete();
            // Replicas
            client.apps().replicaSets().inNamespace(currentNamespace).withLabel(labelName, labelValue).delete();
            // Stateful Sets
            client.apps().statefulSets().inNamespace(currentNamespace).withLabel(labelName, labelValue).delete();
            // CRDs
            client.resourceList().inNamespace(currentNamespace).resources().forEach(r -> {
                if (labelValue.equals(r.get().getMetadata().getLabels().get(labelName))) {
                    r.delete();
                }
            });
            // Services
            client.services().inNamespace(currentNamespace).withLabel(labelName, labelValue).delete();
            // Pods
            client.pods().inNamespace(currentNamespace).withLabel(labelName, labelValue).delete();
            // Secrets
            client.secrets().inNamespace(currentNamespace).withLabel(labelName, labelValue).delete();
            // ConfigMap
            client.configMaps().inNamespace(currentNamespace).withLabel(labelName, labelValue).delete();
            // Claims
            client.persistentVolumeClaims().inNamespace(currentNamespace).withLabel(labelName, labelValue).delete();
            // Service Accounts
            client.serviceAccounts().inNamespace(currentNamespace).withLabel(labelName, labelValue).delete();
            // Jobs
            client.batch().v1().jobs().inNamespace(currentNamespace).withLabel(labelName, labelValue).delete();
            // CronJobs
            client.batch().v1().cronjobs().inNamespace(currentNamespace).withLabel(labelName, labelValue).delete();
            client.batch().v1beta1().cronjobs().inNamespace(currentNamespace).withLabel(labelName, labelValue).delete();
        } catch (Exception e) {
            throw new RuntimeException("Resources failed to be deleted.", e);
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

    public void initializeClientUsingANewNamespace() {
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

        initializeClientUsingNamespace(namespace);
    }

    public void initializeClientUsingNamespace(String namespace) {
        Log.info("Using namespace '%s'", namespace);
        currentNamespace = namespace;
        Config config = new ConfigBuilder().withTrustCerts(true).withNamespace(currentNamespace).build();
        client = (T) initializeClient(config);
    }

    private static boolean doCreateNamespace(String namespaceName) {
        boolean created = false;
        Config config = new ConfigBuilder().withTrustCerts(true).build();
        try (KubernetesClient client = new KubernetesClientBuilder().withHttpClientFactory(new OkHttpClientFactory())
                .withConfig(config).build()) {
            client.namespaces()
                    .resource(new NamespaceBuilder().withNewMetadata().withName(namespaceName).endMetadata().build())
                    .create();
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
