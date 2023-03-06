package io.github.jester.examples.quarkus.jdbc.mysql;

import io.github.jester.api.KubernetesServiceConfiguration;
import io.github.jester.api.RunOnKubernetes;

@RunOnKubernetes
@KubernetesServiceConfiguration(forService = "database", useInternalService = true)
public class KubernetesMySqlDatabaseIT extends MySqlDatabaseIT {
}
