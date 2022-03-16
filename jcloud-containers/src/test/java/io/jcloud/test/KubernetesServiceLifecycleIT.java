package io.jcloud.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import io.jcloud.api.RunOnKubernetes;
import io.jcloud.api.clients.KubectlClient;

@RunOnKubernetes
public class KubernetesServiceLifecycleIT extends ServiceLifecycleIT {

    @Inject
    static KubectlClient clientAsStaticInstance;

    @Test
    public void shouldInjectKubernetesClientAsStaticInstance() {
        assertNotNull(clientAsStaticInstance);
    }

    @Test
    public void shouldInjectKubernetesClientAsField(KubectlClient clientAsField) {
        assertNotNull(clientAsField);
    }
}
