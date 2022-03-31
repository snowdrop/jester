package io.jcloud.examples.benchmark.apps;

import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import io.jcloud.api.Dependency;
import io.jcloud.api.Quarkus;
import io.jcloud.api.RestService;
import io.jcloud.api.Scenario;
import io.jcloud.api.Spring;
import io.jcloud.core.EnableBenchmark;
import io.jcloud.core.ServiceState;
import io.restassured.response.ValidatableResponse;

@DisabledOnOs(value = OS.WINDOWS, disabledReason = "Not supported on Windows")
@Scenario
@Fork(3)
@Warmup(iterations = 1)
@Measurement(iterations = 3)
@BenchmarkMode(Mode.Throughput)
@Threads(50)
public class ThroughputBenchmarkForRestAppsIT implements EnableBenchmark {

    @Quarkus(dependencies = @Dependency(artifactId = "quarkus-resteasy-reactive", version = "${quarkus.platform.version:2.7.5.Final}"))
    public static RestService quarkusReactive = new RestService().setAutoStart(false);

    @Quarkus(dependencies = @Dependency(artifactId = "quarkus-resteasy", version = "${quarkus.platform.version:2.7.5.Final}"))
    public static RestService quarkusClassic = new RestService().setAutoStart(false);

    @Spring(forceBuild = true, buildCommands = { "mvn", "package", "-Pspring",
            "-Duse.test-source.folder=spring-jersey" })
    public static RestService springJersey = new RestService().setAutoStart(false);

    @Spring(forceBuild = true, buildCommands = { "mvn", "package", "-Pspring", "-Duse.test-source.folder=spring-web" })
    public static RestService springWeb = new RestService().setAutoStart(false);

    public static class QuarkusResteasyReactiveState extends ServiceState<RestService> {

        public QuarkusResteasyReactiveState() {
            super(quarkusReactive);
        }
    }

    public static class QuarkusResteasyClassicState extends ServiceState<RestService> {

        public QuarkusResteasyClassicState() {
            super(quarkusClassic);
        }
    }

    public static class SpringJerseyState extends ServiceState<RestService> {

        public SpringJerseyState() {
            super(springJersey);
        }
    }

    public static class SpringWebState extends ServiceState<RestService> {

        public SpringWebState() {
            super(springWeb);
        }
    }

    @Benchmark
    public ValidatableResponse resteasyReactive(QuarkusResteasyReactiveState state) {
        return runBenchmark(state);
    }

    @Benchmark
    public ValidatableResponse resteasyClassic(QuarkusResteasyClassicState state) {
        return runBenchmark(state);
    }

    @Benchmark
    public ValidatableResponse springJersey(SpringJerseyState state) {
        return runBenchmark(state);
    }

    @Benchmark
    public ValidatableResponse springWeb(SpringWebState state) {
        return runBenchmark(state);
    }

    private ValidatableResponse runBenchmark(ServiceState<RestService> state) {
        return state.getService().given().get("/greeting").then().statusCode(HttpStatus.SC_OK).body(is("Hello!"));
    }
}
