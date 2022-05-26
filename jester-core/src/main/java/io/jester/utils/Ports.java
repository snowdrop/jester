package io.jester.utils;

import java.util.Arrays;
import java.util.List;

public final class Ports {

    public static final int DEFAULT_HTTP_PORT = 8080;
    public static final int DEFAULT_SSL_PORT = 8443;
    public static final List<Integer> SSL_PORTS = Arrays.asList(DEFAULT_SSL_PORT, 443);

    private Ports() {

    }

    public static boolean isSsl(int port) {
        return SSL_PORTS.contains(port);
    }
}
