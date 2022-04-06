package io.jcloud.api;

import io.jcloud.api.model.KafkaInstanceCustomResource;
import io.jcloud.core.JCloudContext;
import io.jcloud.core.ServiceContext;

public class KafkaOperatorService extends OperatorService<KafkaOperatorService> {

    private static final String HOST = "%s-kafka-bootstrap";
    private static final int PORT = 9092;
    private static final String KAFKA_INSTANCE_TEMPLATE_DEFAULT = "/strimzi-operator-kafka-instance.yaml";

    private String kafkaInstanceTemplate;

    public KafkaOperatorService() {
        this(KAFKA_INSTANCE_TEMPLATE_DEFAULT);
    }

    public KafkaOperatorService(String kafkaInstanceTemplate) {
        this.kafkaInstanceTemplate = kafkaInstanceTemplate;
    }

    @Override
    public String getHost() {
        return String.format(HOST, getName());
    }

    @Override
    public int getMappedPort(int port) {
        return PORT;
    }

    public String getBootstrapUrl() {
        return getHost() + ":" + PORT;
    }

    @Override
    public ServiceContext register(String serviceName, JCloudContext context) {
        withCrd(serviceName, kafkaInstanceTemplate, KafkaInstanceCustomResource.class);
        return super.register(serviceName, context);
    }
}
