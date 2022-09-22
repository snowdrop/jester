package io.jester.resources.quarkus.common;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import io.quarkus.builder.BuildChainBuilder;

// needs to be in a class of its own in order to avoid java.lang.IncompatibleClassChangeError
public class JesterBuildChainCustomizerProducer
        implements Function<Map<String, Object>, List<Consumer<BuildChainBuilder>>> {

    @SuppressWarnings("unchecked")
    @Override
    public List<Consumer<BuildChainBuilder>> apply(Map<String, Object> testContext) {
        return Arrays.asList(new JesterBuildChainCustomizerConsumer());
    }
}
