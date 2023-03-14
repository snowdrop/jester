package io.github.snowdrop.jester.resources.operators.kubernetes;

import java.nio.file.Path;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.openshift.api.model.operatorhub.v1.OperatorGroup;
import io.fabric8.openshift.api.model.operatorhub.v1.OperatorGroupSpec;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.ClusterServiceVersion;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.Subscription;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.SubscriptionSpec;
import io.github.snowdrop.jester.api.clients.KubernetesClient;
import io.github.snowdrop.jester.api.model.CustomResourceSpec;
import io.github.snowdrop.jester.api.model.CustomResourceStatus;
import io.github.snowdrop.jester.configuration.OperatorServiceConfiguration;
import io.github.snowdrop.jester.core.ServiceContext;
import io.github.snowdrop.jester.logging.Log;
import io.github.snowdrop.jester.utils.AwaitilitySettings;
import io.github.snowdrop.jester.utils.AwaitilityUtils;
import io.github.snowdrop.jester.utils.ManifestsUtils;

public class KubectlOperatorClient {

    private static final String OPERATOR_PHASE_INSTALLED = "Succeeded";
    private static final String CUSTOM_RESOURCE_EXPECTED_TYPE = "Ready";
    private static final String CUSTOM_RESOURCE_EXPECTED_STATUS = "True";

    private final KubernetesClient kubernetesClient;

    public KubectlOperatorClient(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    public void installOperator(ServiceContext service, String subscriptionName, String channel, String source,
            String sourceNamespace) {
        // Install the operator group
        OperatorGroup groupModel = new OperatorGroup();
        groupModel.setMetadata(new ObjectMeta());
        groupModel.getMetadata().setName(service.getName());
        groupModel.setSpec(new OperatorGroupSpec());
        groupModel.getSpec().setTargetNamespaces(List.of(kubernetesClient.namespace()));
        kubernetesClient.underlyingClient().resource(groupModel).createOrReplace();

        // Install the subscription
        Subscription subscriptionModel = new Subscription();
        subscriptionModel.setMetadata(new ObjectMeta());
        subscriptionModel.getMetadata().setName(service.getName());
        subscriptionModel.getMetadata().setNamespace(kubernetesClient.namespace());

        subscriptionModel.setSpec(new SubscriptionSpec());
        subscriptionModel.getSpec().setChannel(channel);
        subscriptionModel.getSpec().setName(subscriptionName);
        subscriptionModel.getSpec().setSource(source);
        subscriptionModel.getSpec().setSourceNamespace(sourceNamespace);

        Log.info(service.getOwner(), "Installing operator... " + subscriptionName);
        kubernetesClient.underlyingClient().resource(subscriptionModel).createOrReplace();

        // Wait for the operator to be installed
        AwaitilityUtils.untilIsTrue(() -> {
            // Get Cluster Service Version
            Subscription subscription = kubernetesClient.underlyingClient().resources(Subscription.class)
                    .withName(service.getName()).get();
            String installedCsv = subscription.getStatus().getInstalledCSV();
            if (StringUtils.isEmpty(installedCsv)) {
                return false;
            }

            // Check Cluster Service Version status
            ClusterServiceVersion operatorService = kubernetesClient.underlyingClient()
                    .resources(ClusterServiceVersion.class).withName(installedCsv).get();
            if (operatorService == null || operatorService.getStatus() == null) {
                return false;
            }

            String csvStatus = operatorService.getStatus().getPhase();
            Log.debug(service.getOwner(), "Operator CSV status: " + csvStatus);
            return OPERATOR_PHASE_INSTALLED.equals(csvStatus);
        }, AwaitilitySettings
                .usingTimeout(service.getConfigurationAs(OperatorServiceConfiguration.class).getInstallTimeout()));
        Log.info(service.getOwner(), "Operator installed");
    }

    public void apply(Path file) {
        kubernetesClient.apply(file);
    }

    public void delete(Path file) {
        kubernetesClient.delete(file);
    }

    public boolean isCustomResourceReady(String name,
            Class<? extends CustomResource<CustomResourceSpec, CustomResourceStatus>> crdType) {
        CustomResource<?, ? extends CustomResourceStatus> customResource = kubernetesClient.underlyingClient()
                .resources(crdType).withName(name).get();
        if (customResource == null || customResource.getStatus() == null
                || customResource.getStatus().getConditions() == null) {
            return false;
        }

        return customResource.getStatus().getConditions().stream()
                .anyMatch(condition -> CUSTOM_RESOURCE_EXPECTED_TYPE.equals(condition.getType())
                        && CUSTOM_RESOURCE_EXPECTED_STATUS.equals(condition.getStatus()));
    }

    public void deleteOperator(ServiceContext service, String name) {
        kubernetesClient.underlyingClient().resources(OperatorGroup.class).withName(service.getName()).delete();
        kubernetesClient.underlyingClient().resources(Subscription.class).withName(name).delete();
        kubernetesClient.deleteResourcesByLabel(ManifestsUtils.LABEL_TO_WATCH_FOR_LOGS, service.getName());
    }
}
