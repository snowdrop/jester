package io.jcloud.resources.operators.kubernetes;

import static io.jcloud.utils.AwaitilityUtils.untilIsTrue;

import java.nio.file.Path;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.openshift.api.model.operatorhub.v1.OperatorGroup;
import io.fabric8.openshift.api.model.operatorhub.v1.OperatorGroupSpec;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.ClusterServiceVersion;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.Subscription;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.SubscriptionSpec;
import io.jcloud.api.clients.KubectlClient;
import io.jcloud.api.model.CustomResourceSpec;
import io.jcloud.api.model.CustomResourceStatus;
import io.jcloud.configuration.OperatorServiceConfiguration;
import io.jcloud.core.ServiceContext;
import io.jcloud.logging.Log;
import io.jcloud.utils.AwaitilityUtils;
import io.jcloud.utils.ManifestsUtils;

public class KubectlOperatorClient {

    private static final String OPERATOR_PHASE_INSTALLED = "Succeeded";
    private static final String CUSTOM_RESOURCE_EXPECTED_TYPE = "Ready";
    private static final String CUSTOM_RESOURCE_EXPECTED_STATUS = "True";

    private final KubectlClient kubectlClient;

    public KubectlOperatorClient(KubectlClient kubectlClient) {
        this.kubectlClient = kubectlClient;
    }

    public void installOperator(ServiceContext service, String name, String channel, String source,
            String sourceNamespace) {
        // Install the operator group
        OperatorGroup groupModel = new OperatorGroup();
        groupModel.setMetadata(new ObjectMeta());
        groupModel.getMetadata().setName(service.getName());
        groupModel.setSpec(new OperatorGroupSpec());
        groupModel.getSpec().setTargetNamespaces(Arrays.asList(kubectlClient.namespace()));
        kubectlClient.underlyingClient().resource(groupModel).createOrReplace();

        // Install the subscription
        Subscription subscriptionModel = new Subscription();
        subscriptionModel.setMetadata(new ObjectMeta());
        subscriptionModel.getMetadata().setName(name);
        subscriptionModel.getMetadata().setNamespace(kubectlClient.namespace());

        subscriptionModel.setSpec(new SubscriptionSpec());
        subscriptionModel.getSpec().setChannel(channel);
        subscriptionModel.getSpec().setName(name);
        subscriptionModel.getSpec().setSource(source);
        subscriptionModel.getSpec().setSourceNamespace(sourceNamespace);

        Log.info(service.getOwner(), "Installing operator... " + name);
        kubectlClient.underlyingClient().resource(subscriptionModel).createOrReplace();

        // Wait for the operator to be installed
        untilIsTrue(() -> {
            // Get Cluster Service Version
            Subscription subscription = kubectlClient.underlyingClient().resources(Subscription.class).withName(name)
                    .get();
            String installedCsv = subscription.getStatus().getInstalledCSV();
            if (StringUtils.isEmpty(installedCsv)) {
                return false;
            }

            // Check Cluster Service Version status
            ClusterServiceVersion operatorService = kubectlClient.underlyingClient()
                    .resources(ClusterServiceVersion.class).withName(installedCsv).get();
            Log.debug(service.getOwner(), "Operator CSV status: " + operatorService.getStatus().getPhase());
            return OPERATOR_PHASE_INSTALLED.equals(operatorService.getStatus().getPhase());
        }, AwaitilityUtils.AwaitilitySettings.defaults().withService(service.getOwner())
                .usingTimeout(service.getConfigurationAs(OperatorServiceConfiguration.class).getInstallTimeout()));
        Log.info(service.getOwner(), "Operator installed");
    }

    public void apply(Path file) {
        kubectlClient.apply(file);
    }

    public boolean isCustomResourceReady(String name,
            Class<? extends CustomResource<CustomResourceSpec, CustomResourceStatus>> crdType) {
        CustomResource<?, ? extends CustomResourceStatus> customResource = kubectlClient.underlyingClient()
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
        kubectlClient.underlyingClient().resources(OperatorGroup.class).withName(service.getName()).delete();
        kubectlClient.underlyingClient().resources(Subscription.class).withName(name).delete();
        kubectlClient.deleteResourcesByLabel(ManifestsUtils.LABEL_TO_WATCH_FOR_LOGS, service.getName());
    }
}
