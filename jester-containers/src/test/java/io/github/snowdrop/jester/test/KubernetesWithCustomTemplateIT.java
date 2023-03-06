package io.github.snowdrop.jester.test;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import io.github.snowdrop.jester.core.extensions.KubernetesExtensionBootstrap;
import io.github.snowdrop.jester.test.samples.ContainerSamples;

@Jester(target = KubernetesExtensionBootstrap.TARGET_KUBERNETES)
@ServiceConfiguration(forService = "templated", deleteFolderOnClose = false)
public class KubernetesWithCustomTemplateIT {

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
        templated.given().log().all().get(ContainerSamples.SAMPLES_DEFAULT_REST_PATH).then().log().all()
                .statusCode(HttpStatus.SC_OK).body(Matchers.is(ContainerSamples.SAMPLES_DEFAULT_REST_PATH_OUTPUT));
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
        assertEquals(6000, containerPort.getContainerPort());
        assertEquals("custom-port", containerPort.getName());
    }

    @Test
    public void testServiceIsInjected() {
        assertNotNull(serviceOfTemplated);

    }
}
