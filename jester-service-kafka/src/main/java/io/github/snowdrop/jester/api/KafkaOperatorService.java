package io.github.snowdrop.jester.api;

public class KafkaOperatorService extends OperatorService {

    private static final String HOST = "%s-kafka-bootstrap";
    private static final int PORT = 9092;

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
}
