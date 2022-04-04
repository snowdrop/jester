package io.jcloud.test;

import static io.jcloud.test.samples.ContainerSamples.QUARKUS_REST_IMAGE;
import static io.jcloud.test.samples.ContainerSamples.QUARKUS_STARTUP_EXPECTED_LOG;
import static io.jcloud.test.samples.ContainerSamples.SAMPLES_DEFAULT_PORT;
import static io.jcloud.test.samples.ContainerSamples.SAMPLES_DEFAULT_REST_PATH;
import static io.jcloud.test.samples.ContainerSamples.SAMPLES_DEFAULT_REST_PATH_OUTPUT;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.jcloud.api.Container;
import io.jcloud.api.JCloud;
import io.jcloud.api.RestService;
import io.jcloud.api.RunOnKubernetes;

@JCloud
@RunOnKubernetes
public class KubernetesWithCustomTemplateIT {

    @Container(image = QUARKUS_REST_IMAGE, ports = SAMPLES_DEFAULT_PORT, expectedLog = QUARKUS_STARTUP_EXPECTED_LOG)
    static RestService templated = new RestService();

    @Inject
    @Named("templated")
    static Deployment deploymentOfTemplated;

    @Inject
    @Named("templated")
    static Service serviceOfTemplated;

    @Test
    public void testServiceIsUpAndRunning() {
        templated.given().get(SAMPLES_DEFAULT_REST_PATH).then().statusCode(HttpStatus.SC_OK)
                .body(is(SAMPLES_DEFAULT_REST_PATH_OUTPUT));
    }

    @Test
    public void testDeploymentIsInjectedWithExpectedValues() {
        assertNotNull(deploymentOfTemplated);
        assertEquals("label-from-template", deploymentOfTemplated.getMetadata().getLabels().get("my-label"));
        // it should keep the one from the template and add the new one from the @Container annotation.
        assertEquals(2,
                deploymentOfTemplated.getSpec().getTemplate().getSpec().getContainers().get(0).getPorts().size());
        ContainerPort containerPort = deploymentOfTemplated.getSpec().getTemplate().getSpec().getContainers().get(0)
                .getPorts().get(0);
        assertNotNull(containerPort);
        assertEquals(8088, containerPort.getContainerPort());
        assertEquals("custom-port", containerPort.getName());
    }

    @Test
    public void testServiceIsInjected() {
        assertNotNull(serviceOfTemplated);

    }
}
