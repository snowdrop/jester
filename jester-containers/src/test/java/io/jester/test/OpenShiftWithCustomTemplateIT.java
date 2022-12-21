package io.jester.test;

import static io.jester.test.samples.ContainerSamples.QUARKUS_REST_IMAGE;
import static io.jester.test.samples.ContainerSamples.SAMPLES_DEFAULT_PORT;
import static io.jester.test.samples.ContainerSamples.SAMPLES_DEFAULT_REST_PATH;
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
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.jester.api.Container;
import io.jester.api.Jester;
import io.jester.api.RestService;
import io.jester.api.ServiceConfiguration;
import io.jester.core.extensions.OpenShiftExtensionBootstrap;

@Jester(target = OpenShiftExtensionBootstrap.TARGET_OPENSHIFT)
@ServiceConfiguration(forService = "templated", deleteFolderOnClose = false)
public class OpenShiftWithCustomTemplateIT {

    @Container(image = QUARKUS_REST_IMAGE, ports = SAMPLES_DEFAULT_PORT, expectedLog = "serving on")
    static RestService templated = new RestService();

    @Inject
    @Named("templated")
    static DeploymentConfig deploymentOfTemplated;

    @Inject
    @Named("templated")
    static Service serviceOfTemplated;

    @Test
    public void testServiceIsUpAndRunning() {
        templated.given().log().all().get(SAMPLES_DEFAULT_REST_PATH).then().log().all().statusCode(HttpStatus.SC_OK)
                .body(Matchers.containsString("Hello OpenShift!"));
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
