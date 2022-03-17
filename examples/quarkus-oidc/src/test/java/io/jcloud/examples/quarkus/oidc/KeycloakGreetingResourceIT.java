package io.jcloud.examples.quarkus.oidc;

import static org.hamcrest.Matchers.equalTo;

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;

import io.jcloud.api.Container;
import io.jcloud.api.DefaultService;
import io.jcloud.api.Quarkus;
import io.jcloud.api.RestService;
import io.jcloud.api.Scenario;

@Tag("containers")
@Scenario
public class KeycloakGreetingResourceIT {
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

    @Quarkus
    static RestService secured = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", KeycloakGreetingResourceIT::getRealmUrl)
            .withProperty("quarkus.oidc.token.issuer", "any").withProperty("quarkus.oidc.client-id", CLIENT_ID)
            .withProperty("quarkus.oidc.credentials.secret", CLIENT_SECRET);

    private static AuthzClient authzClient;

    @BeforeAll
    public static void setup() {
        authzClient = AuthzClient.create(new Configuration(StringUtils.substringBefore(getRealmUrl(), "/realms"), REALM,
                CLIENT_ID, Collections.singletonMap("secret", CLIENT_SECRET), HttpClients.createDefault()));
    }

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
        return authzClient.obtainAccessToken(NORMAL_USER, NORMAL_USER).getToken();
    }

    private static String getRealmUrl() {
        String url = keycloak.getHost();
        // SMELL: Keycloak does not validate Token Issuers when URL contains the port 80.
        int mappedPort = keycloak.getMappedPort(8080);
        if (mappedPort != 80) {
            url += ":" + mappedPort;
        }

        return String.format("http://%s/auth/realms/%s", url, REALM);
    }
}
