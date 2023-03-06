package io.github.snowdrop.jester.test;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.github.snowdrop.jester.api.Container;
import io.github.snowdrop.jester.api.Jester;
import io.github.snowdrop.jester.api.RestService;
import io.github.snowdrop.jester.api.ServiceConfiguration;
import io.github.snowdrop.jester.core.extensions.OpenShiftExtensionBootstrap;
import io.github.snowdrop.jester.test.samples.ContainerSamples;

@Jester(target = OpenShiftExtensionBootstrap.TARGET_OPENSHIFT)
@ServiceConfiguration(forService = "templated", deleteFolderOnClose = false)
public class OpenShiftWithCustomTemplateIT {

    @Container(image = ContainerSamples.QUARKUS_REST_IMAGE, ports = ContainerSamples.SAMPLES_DEFAULT_PORT, expectedLog = ContainerSamples.QUARKUS_STARTUP_EXPECTED_LOG)
    static RestService templated = new RestService();

    @Inject
    @Named("templated")
    static Deployment deploymentOfTemplated;

    @Inject
    @Named("templated")
    static Service serviceOfTemplated;

    @Test
    public void testServiceIsUpAndRunning() {
        templated.given().get(ContainerSamples.SAMPLES_DEFAULT_REST_PATH).then().statusCode(HttpStatus.SC_OK)
                .body(Matchers.is(ContainerSamples.SAMPLES_DEFAULT_REST_PATH_OUTPUT));
    }

    @Test
    public void testDeploymentIsInjectedWithExpectedValues() {
        assertNotNull(deploymentOfTemplated);
        assertEquals("label-from-template", deploymentOfTemplated.getMetadata().getLabels().get("my-label"));
        // it should keep the one from the template and add the new one from the @Container annotation.
        List<ContainerPort> containerPorts = deploymentOfTemplated.getSpec().getTemplate().getSpec().getContainers()
                .get(0).getPorts();
        assertEquals(2, containerPorts.size());
        assertTrue(containerPorts.stream().anyMatch(containerPort -> containerPort.getContainerPort() == 8080));
        assertTrue(containerPorts.stream().anyMatch(containerPort -> containerPort.getContainerPort() == 6000
                && "custom-port".equals(containerPort.getName())));
    }

    @Test
    public void testServiceIsInjected() {
        assertNotNull(serviceOfTemplated);
    }
}
