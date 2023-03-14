package io.github.snowdrop.jester.api.clients;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.okhttp.OkHttpClientFactory;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import io.fabric8.openshift.client.OpenShiftClient;
import io.github.snowdrop.jester.api.Service;

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

        return route.getSpec().getPort().getTargetPort().getIntVal();
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
