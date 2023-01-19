package io.jester.test;

import io.fabric8.openshift.client.OpenShiftClient;
import io.jester.api.RunOnOpenShift;
import io.jester.api.clients.OpenshiftClient;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunOnOpenShift
public class OpenShiftServiceLifecycleIT extends ServiceLifecycleIT {

    @Inject
    static OpenshiftClient clientAsStaticInstance;

    @Inject
    static OpenShiftClient fabric8OpenShiftClient;

    @Test
    public void shouldInjectOpenShiftClientsAsStaticInstance() {
        assertNotNull(clientAsStaticInstance);
        assertNotNull(fabric8OpenShiftClient);
    }

    @Test
    public void shouldInjectOpenShiftClientAsField(OpenshiftClient clientAsField) {
        assertNotNull(clientAsField);
    }
}
