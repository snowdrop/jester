package io.jester.test;

import java.time.Duration;

import org.apache.http.HttpStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import io.jester.api.Container;
import io.jester.api.Jester;
import io.jester.api.KafkaOperatorService;
import io.jester.api.KafkaResource;
import io.jester.api.Operator;
import io.jester.api.RestService;
import io.jester.api.RunOnKubernetes;

@Jester
@RunOnKubernetes
@Operator(subscription = "strimzi-kafka-operator", channel = "strimzi-0.31.x")
public class KubernetesKafkaOperatorIT {

    @KafkaResource
    static final KafkaOperatorService kafka = new KafkaOperatorService();

    @Container(image = "${sample.messaging-kafka.image:server.io/test/quarkus-messaging-kafka:latest}", ports = 8080, expectedLog = "Installed features")
    static final RestService app = new RestService().withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl);

    @Test
    public void checkUserResourceByNormalUser() {
        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            app.given().get("/prices/poll").then().statusCode(HttpStatus.SC_OK);
        });
    }
}
