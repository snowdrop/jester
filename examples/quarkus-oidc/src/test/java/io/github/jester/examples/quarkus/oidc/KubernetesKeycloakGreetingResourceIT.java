package io.github.jester.examples.quarkus.oidc;

import io.github.jester.api.RunOnKubernetes;

@RunOnKubernetes
public class KubernetesKeycloakGreetingResourceIT extends KeycloakGreetingResourceIT {

    @Override
    protected String getRealmUrl() {
        return String.format("http://keycloak:8080/auth/realms/%s", REALM);
    }
}
