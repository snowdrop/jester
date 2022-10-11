package io.jester.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import io.fabric8.openshift.client.OpenShiftClient;
import io.jester.api.RunOnOpenShift;
import io.jester.api.clients.OCClient;

@RunOnOpenShift
public class OpenShiftServiceLifecycleIT extends ServiceLifecycleIT {

    @Inject
    static OCClient clientAsStaticInstance;

    @Inject
    static OpenShiftClient fabric8OpenShiftClient;

    @Test
    public void shouldInjectOpenShiftClientsAsStaticInstance() {
        assertNotNull(clientAsStaticInstance);
        assertNotNull(fabric8OpenShiftClient);
    }

    @Test
    public void shouldInjectOpenShiftClientAsField(OCClient clientAsField) {
        assertNotNull(clientAsField);
    }
}
