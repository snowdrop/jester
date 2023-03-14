package io.github.snowdrop.jester.api.clients;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

public final class KubernetesClient extends BaseKubernetesClient<io.fabric8.kubernetes.client.KubernetesClient> {

    @Override
    public io.fabric8.kubernetes.client.KubernetesClient initializeClient(Config config) {
        return new KubernetesClientBuilder().withConfig(config).build();
    }
}
