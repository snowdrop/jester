package io.github.jester.examples.quarkus.greetings;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class CustomNoopHostnameVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }

    @Override
    public final String toString() {
        return "NO_OP";
    }
}
