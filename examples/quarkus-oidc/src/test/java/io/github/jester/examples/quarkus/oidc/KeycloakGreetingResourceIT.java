package io.github.jester.examples.quarkus.oidc;

import static org.hamcrest.Matchers.equalTo;

import java.util.Collections;

import org.apache.http.HttpStatus;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;

import io.github.jester.api.Container;
import io.github.jester.api.DefaultService;
import io.github.jester.api.Jester;
import io.github.jester.api.Quarkus;
import io.github.jester.api.RestService;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("containers")
@Jester
public class KeycloakGreetingResourceIT {
    protected static final String REALM = "test-realm";
    private static final String CLIENT_ID = "test-application-client";
    private static final String CLIENT_SECRET = "test-application-client-secret";
    private static final String NORMAL_USER = "test-normal-user";

    @Container(image = "quay.io/keycloak/keycloak:14.0.0", expectedLog = "Admin console listening", ports = 8080)
    DefaultService keycloak = new DefaultService().withProperty("KEYCLOAK_IMPORT", "resource::/keycloak-realm.json");

    @Quarkus
    RestService secured = new RestService().withProperty("quarkus.oidc.auth-server-url", this::getRealmUrl)
            .withProperty("quarkus.oidc.token.issuer", "any").withProperty("quarkus.oidc.client-id", CLIENT_ID)
            .withProperty("quarkus.oidc.credentials.secret", CLIENT_SECRET);

    @Test
    public void testSecuredEndpointWithInvalidToken() {
        secured.given().get("/hello").then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void testSecuredEndpointWithValidToken() {
        secured.given().auth().oauth2(getTokenByTestNormalUser()).get("/hello").then().statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello Samples"));
    }

    private String getTokenByTestNormalUser() {
        AuthzClient authzClient = AuthzClient.create(new Configuration(getKeycloakUrl(), REALM, CLIENT_ID,
                Collections.singletonMap("secret", CLIENT_SECRET), HttpClients.createDefault()));

        return authzClient.obtainAccessToken(NORMAL_USER, NORMAL_USER).getToken();
    }

    protected String getKeycloakUrl() {
        String url = keycloak.getHost();
        // SMELL: Keycloak does not validate Token Issuers when URL contains the port 80.
        int mappedPort = keycloak.getMappedPort(8080);
        if (mappedPort != 80) {
            url += ":" + mappedPort;
        }

        return String.format("http://%s/auth", url);
    }

    protected String getRealmUrl() {
        String url = keycloak.getHost();
        // SMELL: Keycloak does not validate Token Issuers when URL contains the port 80.
        int mappedPort = keycloak.getMappedPort(8080);
        if (mappedPort != 80) {
            url += ":" + mappedPort;
        }

        return String.format("http://%s/auth/realms/%s", url, REALM);
    }
}
