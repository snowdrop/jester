package io.github.snowdrop.jester.utils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.github.snowdrop.jester.api.clients.BaseKubernetesClient;
import io.github.snowdrop.jester.core.ServiceContext;

public final class DeploymentResourceUtils {

    private static final String DEPLOYMENT = "deployment.yml";

    private DeploymentResourceUtils() {

    }

    public static Deployment loadDeploymentFromString(String content) {
        try {
            return Serialization.unmarshal(content, Deployment.class);
        } catch (Exception ignored) {
            // Maybe it's a kubernetes list
            KubernetesList list = null;
            try {
                list = Serialization.unmarshal(content, KubernetesList.class);
            } catch (Exception ex) {
                list = SerializationUtils.unmarshalAsList(content);
            }

            Deployment found = null;
            for (HasMetadata resource : list.getItems()) {
                if (resource instanceof Deployment) {
                    found = (Deployment) resource;
                }
            }

            return found;
        }
    }

    public static void adaptDeployment(ServiceContext context, BaseKubernetesClient client, Deployment deployment,
            String image, String[] command, int[] ports) {
        // Set service data
        initDeployment(deployment);
        Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        container.setName(context.getName());
        container.setImage(image);
        if (command != null) {
            container.setCommand(Arrays.asList(command));
        }

        for (int port : ports) {
            if (container.getPorts().stream().noneMatch(p -> p.getContainerPort() == port)) {
                container.getPorts()
                        .add(new ContainerPortBuilder().withName("port-" + port).withContainerPort(port).build());
            }
        }

        // Enrich it
        ManifestsUtils.enrichDeployment(client, deployment, context.getOwner());

        // Apply it
        Path target = context.getServiceFolder().resolve(DEPLOYMENT);
        ManifestsUtils.writeFile(target, deployment);
        client.apply(target);
    }

    private static void initDeployment(Deployment deployment) {
        if (deployment.getSpec() == null) {
            deployment.setSpec(new DeploymentSpec());
        }
        if (deployment.getSpec().getTemplate() == null) {
            deployment.getSpec().setTemplate(new PodTemplateSpec());
        }
        if (deployment.getSpec().getTemplate().getSpec() == null) {
            deployment.getSpec().getTemplate().setSpec(new PodSpec());
        }
        if (deployment.getSpec().getTemplate().getSpec().getContainers() == null) {
            deployment.getSpec().getTemplate().getSpec().setContainers(new ArrayList<>());
        }
        if (deployment.getSpec().getTemplate().getSpec().getContainers().isEmpty()) {
            deployment.getSpec().getTemplate().getSpec().getContainers().add(new Container());
        }
    }
}
