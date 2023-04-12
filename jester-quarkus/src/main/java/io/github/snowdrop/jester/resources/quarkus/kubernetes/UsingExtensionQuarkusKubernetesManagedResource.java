package io.github.snowdrop.jester.resources.quarkus.kubernetes;

import static io.github.snowdrop.jester.utils.DockerUtils.getUniqueName;
import static io.github.snowdrop.jester.utils.ManifestsUtils.LABEL_CONTEXT_ID;
import static io.github.snowdrop.jester.utils.ManifestsUtils.LABEL_TO_WATCH_FOR_LOGS;
import static io.github.snowdrop.jester.utils.MavenUtils.BATCH_MODE;
import static io.github.snowdrop.jester.utils.MavenUtils.DISPLAY_VERSION;
import static io.github.snowdrop.jester.utils.MavenUtils.PACKAGE_GOAL;
import static io.github.snowdrop.jester.utils.MavenUtils.SKIP_CHECKSTYLE;
import static io.github.snowdrop.jester.utils.MavenUtils.SKIP_ITS;
import static io.github.snowdrop.jester.utils.MavenUtils.SKIP_TESTS;
import static io.github.snowdrop.jester.utils.MavenUtils.installParentPomsIfNeeded;
import static io.github.snowdrop.jester.utils.MavenUtils.mvnCommand;
import static io.github.snowdrop.jester.utils.MavenUtils.withProperty;
import static io.github.snowdrop.jester.utils.QuarkusUtils.copyResourcesToServiceFolder;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.github.snowdrop.jester.configuration.QuarkusServiceConfiguration;
import io.github.snowdrop.jester.configuration.QuarkusServiceConfigurationBuilder;
import io.github.snowdrop.jester.core.ServiceContext;
import io.github.snowdrop.jester.resources.kubernetes.KubernetesManagedResource;
import io.github.snowdrop.jester.utils.Command;
import io.github.snowdrop.jester.utils.FileUtils;
import io.github.snowdrop.jester.utils.PropertiesUtils;
import io.github.snowdrop.jester.utils.QuarkusUtils;

public class UsingExtensionQuarkusKubernetesManagedResource extends KubernetesManagedResource {

    private static final String USING_EXTENSION_PROFILE = "-Pdeploy-to-kubernetes-using-extension";
    private static final String QUARKUS_PLUGIN_DEPLOY = "-Dquarkus.kubernetes.deploy=true";
    private static final String QUARKUS_CONTAINER_NAME = "quarkus.application.name";
    private static final String QUARKUS_CONTAINER_IMAGE = "quarkus.container-image.image";
    private static final String QUARKUS_KUBERNETES_CLIENT_NAMESPACE = "quarkus.kubernetes-client.namespace";
    private static final String QUARKUS_KUBERNETES_CLIENT_TRUST_CERTS = "quarkus.kubernetes-client.trust-certs";
    private static final String QUARKUS_KUBERNETES_ENV_VARS = "quarkus.kubernetes.env.vars.";
    private static final String QUARKUS_KUBERNETES_LABELS = "quarkus.kubernetes.labels.";
    private static final Path RESOURCES_FOLDER = Paths.get("src", "main", "resources", "application.properties");
    private static final Path TEST_RESOURCES_FOLDER = Paths.get("src", "test", "resources", "application.properties");

    private final Path location;

    private String image;

    public UsingExtensionQuarkusKubernetesManagedResource(String location) {
        this.location = Path.of(location);
    }

    @Override
    public String getDisplayName() {
        return context.getName();
    }

    @Override
    protected String getImage() {
        return image;
    }

    @Override
    protected String getExpectedLog() {
        return context.getConfigurationAs(QuarkusServiceConfiguration.class).getExpectedLog();
    }

    @Override
    protected int[] getPorts() {
        List<Integer> ports = new ArrayList<>();
        ports.add(Optional.ofNullable(getProperty(QuarkusUtils.QUARKUS_HTTP_PORT_PROPERTY)).map(Integer::parseInt)
                .orElse(QuarkusUtils.HTTP_PORT_DEFAULT));

        Optional.ofNullable(getProperty(QuarkusUtils.QUARKUS_GRPC_SERVER_PORT)).map(Integer::parseInt)
                .ifPresent(grpcPort -> ports.add(grpcPort));

        return ports.stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    protected void init(ServiceContext context) {
        super.init(context);

        context.loadCustomConfiguration(QuarkusServiceConfiguration.class, new QuarkusServiceConfigurationBuilder());
        this.image = context.getConfiguration().getImageRegistry() + "/" + getUniqueName(context);
    }

    @Override
    protected void doInit() {
        cloneProjectToServiceAppFolder();
        copyResourcesToServiceFolder(location, context);
        mergePropertiesIntoAppFolder();
        deployProjectUsingMavenCommand();
    }

    protected void withAdditionalArguments(List<String> args) {

    }

    private void mergePropertiesIntoAppFolder() {
        Map<String, String> properties = context.getOwner().getProperties().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Path applicationPropertiesPath = context.getServiceFolder().resolve(RESOURCES_FOLDER);
        if (Files.exists(applicationPropertiesPath)) {
            properties.putAll(PropertiesUtils.toMap(applicationPropertiesPath));
        }

        Path testPropertiesPath = context.getServiceFolder().resolve(TEST_RESOURCES_FOLDER);
        if (Files.exists(testPropertiesPath)) {
            properties.putAll(PropertiesUtils.toMap(testPropertiesPath));
        }

        PropertiesUtils.fromMap(properties, context.getServiceFolder().resolve(RESOURCES_FOLDER));
    }

    private void deployProjectUsingMavenCommand() {
        installParentPomsIfNeeded();

        String namespace = client.namespace();

        List<String> args = mvnCommand(context);
        args.addAll(Arrays.asList(USING_EXTENSION_PROFILE, BATCH_MODE, DISPLAY_VERSION, PACKAGE_GOAL,
                QUARKUS_PLUGIN_DEPLOY, SKIP_TESTS, SKIP_ITS, SKIP_CHECKSTYLE));
        args.add(withProperty(QUARKUS_CONTAINER_IMAGE, image));
        args.add(withContainerName());
        args.add(withKubernetesClientNamespace(namespace));
        args.add(withKubernetesClientTrustCerts());
        args.add(withLabelsForWatching());
        args.add(withLabelsForScenarioId());
        withEnvVars(args, context.getOwner().getProperties());
        withAdditionalArguments(args);

        try {
            new Command(args).onDirectory(context.getServiceFolder()).runAndWait();
        } catch (Exception e) {
            fail("Failed to run maven command. Caused by " + e.getMessage());
        }
    }

    private String withLabelsForWatching() {
        return withLabels(LABEL_TO_WATCH_FOR_LOGS, context.getOwner().getName());
    }

    private String withLabelsForScenarioId() {
        return withLabels(LABEL_CONTEXT_ID, context.getJesterContext().getId());
    }

    private String withLabels(String label, String value) {
        return withProperty(QUARKUS_KUBERNETES_LABELS + label, value);
    }

    private String withContainerName() {
        return withProperty(QUARKUS_CONTAINER_NAME, context.getName());
    }

    private String withKubernetesClientNamespace(String namespace) {
        return withProperty(QUARKUS_KUBERNETES_CLIENT_NAMESPACE, namespace);
    }

    private String withKubernetesClientTrustCerts() {
        return withProperty(QUARKUS_KUBERNETES_CLIENT_TRUST_CERTS, Boolean.TRUE.toString());
    }

    private void withEnvVars(List<String> args, Map<String, String> envVars) {
        for (Entry<String, String> envVar : envVars.entrySet()) {
            String envVarKey = envVar.getKey().replaceAll(Pattern.quote("."), "-");
            args.add(withProperty(QUARKUS_KUBERNETES_ENV_VARS + envVarKey, envVar.getValue()));
        }
    }

    private void cloneProjectToServiceAppFolder() {
        FileUtils.copyDirectoryTo(location, context.getServiceFolder());
    }

}
