package io.github.snowdrop.jester.api.clients;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.okhttp.OkHttpClientFactory;

public final class KubectlClient extends BaseKubernetesClient<KubernetesClient> {

    private static final String KUBECTL = "kubectl";

    @Override
    public String command() {
        return KUBECTL;
    }

    @Override
    public KubernetesClient initializeClient(Config config) {
        return new KubernetesClientBuilder().withConfig(config).withHttpClientFactory(new OkHttpClientFactory())
                .build();
    }
}
