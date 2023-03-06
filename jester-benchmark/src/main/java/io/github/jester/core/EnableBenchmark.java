package io.github.jester.core;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import io.github.jester.configuration.BenchmarkConfiguration;
import io.github.jester.utils.FileUtils;

public interface EnableBenchmark {

    @Test
    default void benchmarkRunner(JesterContext context) throws RunnerException {
        // enable profiling (for Java processes only)
        context.getConfiguration().setProfilingEnabled(true);

        BenchmarkConfiguration configuration = context.getConfigurationAs(BenchmarkConfiguration.class);

        // run benchmarks
        Path result = Path.of(configuration.getOutputLocation());
        FileUtils.createDirectoryIfDoesNotExist(result);
        ChainedOptionsBuilder jmhRunnerOptions = new OptionsBuilder().shouldDoGC(true).shouldFailOnError(true)
                .shouldFailOnError(true).resultFormat(benchmarkResultFormat()).addProfiler(StackProfiler.class)
                // do not use forking or the benchmark methods will not see references stored within its class
                .forks(0)
                // set the class name regex for benchmarks to search for to the current class
                .include("\\." + this.getClass().getSimpleName() + "\\.")
                .result(result.resolve(this.getClass().getSimpleName() + ".json").toString());

        new Runner(jmhRunnerOptions.build()).run();
    }

    default ResultFormatType benchmarkResultFormat() {
        return ResultFormatType.JSON;
    }
}
