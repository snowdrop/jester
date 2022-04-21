package io.jcloud.test;

import java.time.Duration;

import org.apache.http.HttpStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import io.jcloud.api.Container;
import io.jcloud.api.JCloud;
import io.jcloud.api.KafkaOperatorService;
import io.jcloud.api.KafkaResource;
import io.jcloud.api.Operator;
import io.jcloud.api.RestService;
import io.jcloud.api.RunOnKubernetes;

@JCloud
@RunOnKubernetes
@Operator(subscription = "strimzi-kafka-operator")
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
