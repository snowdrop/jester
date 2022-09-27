package io.jester.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.jester.api.RunOnKubernetes;
import io.jester.api.clients.KubectlClient;

@DisabledIfSystemProperty(named = "environment.ci", matches = "true", disabledReason = "In the GitHub runner, this test is flaky")
@RunOnKubernetes
public class KubernetesServiceLifecycleIT extends ServiceLifecycleIT {

    @Inject
    static KubectlClient clientAsStaticInstance;

    @Inject
    static KubernetesClient fabric8KubernetesClient;

    @Test
    public void shouldInjectKubernetesClientsAsStaticInstance() {
        assertNotNull(clientAsStaticInstance);
        assertNotNull(fabric8KubernetesClient);
    }

    @Test
    public void shouldInjectKubernetesClientAsField(KubectlClient clientAsField) {
        assertNotNull(clientAsField);
    }
}
