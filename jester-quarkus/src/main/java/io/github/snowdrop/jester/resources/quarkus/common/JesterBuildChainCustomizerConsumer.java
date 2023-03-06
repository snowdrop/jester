package io.github.snowdrop.jester.resources.quarkus.common;

import java.util.function.Consumer;

import io.github.snowdrop.jester.utils.QuarkusUtils;
import io.quarkus.builder.BuildChainBuilder;
import io.quarkus.builder.item.BuildItem;

// needs to be in a class of it's own in order to avoid java.lang.IncompatibleClassChangeError
public class JesterBuildChainCustomizerConsumer implements Consumer<BuildChainBuilder> {

    @Override
    public void accept(BuildChainBuilder builder) {
        ClassLoader cl = this.getClass().getClassLoader();
        try {
            Class<?> customProjectForKubernetes = cl.loadClass(QuarkusUtils.QUARKUS_KUBERNETES_SPI_CUSTOM_PROJECT);
            builder.addBuildStep(new KubernetesCustomProjectBuildStep(customProjectForKubernetes))
                    .produces((Class<? extends BuildItem>) customProjectForKubernetes).build();
        } catch (ClassNotFoundException ignored) {

        }
    }

}
