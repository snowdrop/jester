package io.jcloud.test;

import static io.jcloud.test.samples.ContainerSamples.QUARKUS_SECURED_IMAGE;
import static io.jcloud.test.samples.ContainerSamples.QUARKUS_STARTUP_EXPECTED_LOG;
import static io.jcloud.test.samples.ContainerSamples.SAMPLES_DEFAULT_PORT;
import static io.jcloud.test.samples.ContainerSamples.SAMPLES_DEFAULT_REST_PATH;
import static io.jcloud.test.samples.ContainerSamples.SAMPLES_DEFAULT_REST_PATH_OUTPUT;
import static org.hamcrest.Matchers.equalTo;

import java.util.Collections;

import org.apache.http.HttpStatus;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;

import io.jcloud.api.Container;
import io.jcloud.api.DefaultService;
import io.jcloud.api.RestService;
import io.jcloud.api.Scenario;

/**
 * We'll use the quarkus-secured example and integrate it with a Keycloak service.
 */
@Scenario
public class ExternalResourceIT {

    private static final String USER = "admin";
    private static final String PASSWORD = "admin";
    private static final String REALM = "test-realm";
    private static final String CLIENT_ID = "test-application-client";
    private static final String CLIENT_SECRET = "test-application-client-secret";
    private static final String NORMAL_USER = "test-normal-user";

    @Container(image = "quay.io/keycloak/keycloak:14.0.0", expectedLog = "Admin console listening", ports = 8080)
    static final DefaultService keycloak = new DefaultService().withProperty("KEYCLOAK_USER", USER)
            .withProperty("KEYCLOAK_PASSWORD", PASSWORD)
            .withProperty("KEYCLOAK_IMPORT", "resource::/keycloak-realm.json");

    @Container(image = QUARKUS_SECURED_IMAGE, ports = SAMPLES_DEFAULT_PORT, expectedLog = QUARKUS_STARTUP_EXPECTED_LOG)
    static RestService secured = new RestService()
            .withProperty("quarkus.oidc.auth-server-url",
                    () -> String.format("http://keycloak:8080/auth/realms/%s", REALM))
            .withProperty("quarkus.oidc.token.issuer", "any").withProperty("quarkus.oidc.client-id", CLIENT_ID)
            .withProperty("quarkus.oidc.credentials.secret", CLIENT_SECRET);

    private static AuthzClient authzClient;

    @BeforeAll
    public static void setup() {
        authzClient = AuthzClient.create(new Configuration(getKeycloakUrl(), REALM, CLIENT_ID,
                Collections.singletonMap("secret", CLIENT_SECRET), HttpClients.createDefault()));
    }

    @Test
    public void testSecuredEndpointWithInvalidToken() {
        secured.given().get(SAMPLES_DEFAULT_REST_PATH).then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void testSecuredEndpointWithValidToken() {
        secured.given().auth().oauth2(getTokenByTestNormalUser()).get(SAMPLES_DEFAULT_REST_PATH).then()
                .statusCode(HttpStatus.SC_OK).body(equalTo(SAMPLES_DEFAULT_REST_PATH_OUTPUT));
    }

    private String getTokenByTestNormalUser() {
        return authzClient.obtainAccessToken(NORMAL_USER, NORMAL_USER).getToken();
    }

    private static String getKeycloakUrl() {
        String url = keycloak.getHost();
        // SMELL: Keycloak does not validate Token Issuers when URL contains the port 80.
        int mappedPort = keycloak.getMappedPort(8080);
        if (mappedPort != 80) {
            url += ":" + mappedPort;
        }

        return String.format("http://%s/auth", url);
    }
}
