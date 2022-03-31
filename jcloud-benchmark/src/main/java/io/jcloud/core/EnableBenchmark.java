package io.jcloud.core;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import io.jcloud.utils.FileUtils;

public interface EnableBenchmark {

    @Test
    default void benchmarkRunner() throws RunnerException {
        Path result = Path.of("target", "benchmarks-results");
        FileUtils.createDirectoryIfDoesNotExist(result);
        Options jmhRunnerOptions = new OptionsBuilder().shouldDoGC(true).shouldFailOnError(true).shouldFailOnError(true)
                .resultFormat(ResultFormatType.JSON)
                // do not use forking or the benchmark methods will not see references stored within its class
                .forks(0)
                // set the class name regex for benchmarks to search for to the current class
                .include("\\." + this.getClass().getSimpleName() + "\\.")
                .result(result.resolve(this.getClass().getSimpleName() + ".json").toString()).build();

        new Runner(jmhRunnerOptions).run();
    }
}
