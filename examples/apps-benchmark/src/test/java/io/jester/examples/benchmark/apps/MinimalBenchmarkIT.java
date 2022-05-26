package io.jester.examples.benchmark.apps;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import io.jester.api.Dependency;
import io.jester.api.HttpService;
import io.jester.api.Jester;
import io.jester.api.Quarkus;
import io.jester.core.EnableBenchmark;
import io.jester.core.ServiceState;

@DisabledOnOs(value = OS.WINDOWS, disabledReason = "Not supported on Windows")
@Jester
@Fork(1)
@Warmup(iterations = 1)
@Measurement(iterations = 1)
@BenchmarkMode(Mode.Throughput)
@Threads(5)
public class MinimalBenchmarkIT implements EnableBenchmark {

    @Quarkus(dependencies = @Dependency(artifactId = "quarkus-resteasy-reactive", version = "${quarkus.platform.version:2.8.0.Final}"))
    public static HttpService quarkusReactive = new HttpService().setAutoStart(false);

    public static class QuarkusResteasyReactiveState extends ServiceState<HttpService> {

        public QuarkusResteasyReactiveState() {
            super(quarkusReactive);
        }
    }

    @Benchmark
    public String quarkusResteasyReactive(QuarkusResteasyReactiveState state) {
        return runBenchmark(state);
    }

    private String runBenchmark(ServiceState<HttpService> state) {
        HttpResponse<String> response = state.getService().getString("/greeting");
        assertEquals("Hello!", response.body());
        return response.body();
    }
}
