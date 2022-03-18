package io.jcloud.examples.quarkus.oidc;

import io.jcloud.api.RunOnKubernetes;

@RunOnKubernetes
public class KubernetesKeycloakGreetingResourceIT extends KeycloakGreetingResourceIT {

    @Override
    protected String getRealmUrl() {
        return String.format("http://keycloak:8080/auth/realms/%s", REALM);
    }
}
