package io.github.snowdrop.jester.api.clients;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.okhttp.OkHttpClientFactory;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import io.fabric8.openshift.client.OpenShiftClient;
import io.github.snowdrop.jester.api.Service;
import io.github.snowdrop.jester.logging.Log;
import io.github.snowdrop.jester.utils.AwaitilitySettings;
import io.github.snowdrop.jester.utils.AwaitilityUtils;

public final class OpenshiftClient extends BaseKubernetesClient<OpenShiftClient> {

    @Override
    public OpenShiftClient initializeClient(Config config) {
        return new KubernetesClientBuilder().withConfig(config).withHttpClientFactory(new OkHttpClientFactory()).build()
                .adapt(OpenShiftClient.class);
    }

    @Override
    public String host(Service service) {
        Route route = underlyingClient().routes().withName(service.getName()).get();
        if (route == null || route.getSpec() == null) {
            return super.host(service);
        }

        return route.getSpec().getHost();
    }

    @Override
    public int port(Service service, int port) {
        if (PORT_FORWARD_HOST.equalsIgnoreCase(host(service))) {
            return super.port(service, port);
        }

        Route route = underlyingClient().routes().withName(service.getName()).get();
        if (route == null || route.getSpec() == null || route.getSpec().getPort() == null) {
            return super.port(service, port);
        }

        Integer portNumber = route.getSpec().getPort().getTargetPort().getIntVal();
        if (portNumber != null) {
            return portNumber;
        }

        String portName = route.getSpec().getPort().getTargetPort().getStrVal();
        DeploymentConfig deploymentConfig = underlyingClient().deploymentConfigs().withName(service.getName()).get();
        if (deploymentConfig != null) {
            return deploymentConfig.getSpec().getTemplate().getSpec().getContainers().stream()
                    .flatMap(c -> c.getPorts().stream()).filter(p -> p.getName().equals(portName))
                    .map(p -> p.getContainerPort()).findFirst().orElseGet(() -> super.port(service, port));
        }

        Deployment deployment = underlyingClient().apps().deployments().withName(service.getName()).get();
        if (deployment != null) {
            return deployment.getSpec().getTemplate().getSpec().getContainers().stream()
                    .flatMap(c -> c.getPorts().stream()).filter(p -> p.getName().equals(portName))
                    .map(p -> p.getContainerPort()).findFirst().orElseGet(() -> super.port(service, port));
        }

        return super.port(service, port);
    }

    @Override
    public void scaleTo(Service service, int replicas) {
        String serviceName = service.getName();
        DeploymentConfig deployment = underlyingClient().deploymentConfigs().withName(serviceName).get();
        if (deployment != null) {
            if (deployment.getSpec().getReplicas() != replicas) {
                try {
                    Log.info(service, "Scaling deployment '%s' in namespace '%s' to '%s'", serviceName, namespace(),
                            replicas);
                    deployment.getSpec().setReplicas(replicas);
                    underlyingClient().deploymentConfigs().withName(serviceName).patch(deployment);
                    AwaitilityUtils
                            .untilIsTrue(
                                    () -> underlyingClient().deploymentConfigs().withName(serviceName).get().getSpec()
                                            .getReplicas() == replicas,
                                    AwaitilitySettings.defaults().withService(service));
                } catch (Exception e) {
                    throw new RuntimeException("Service failed to be scaled.", e);
                }
            }
        } else {
            super.scaleTo(service, replicas);
        }
    }

    public void exposeRoute(Service service, int[] ports) {
        List portList = IntStream.of(ports).mapToObj(Integer::toString).collect(Collectors.toList());
        portList.forEach(port -> {
            try {
                underlyingClient().routes()
                        .resource(new RouteBuilder().withNewMetadata().withName(service.getName() + "-" + port)
                                .withNamespace(namespace()).endMetadata().withNewSpec().withNewTo().withKind("Service")
                                .withName(service.getName()).endTo().withNewPort().withTargetPort(new IntOrString(port))
                                .endPort().endSpec().build())
                        .createOrReplace();
            } catch (Exception e) {
                throw new RuntimeException("Route failed to be exposed.", e);
            }
        });
    }
}
