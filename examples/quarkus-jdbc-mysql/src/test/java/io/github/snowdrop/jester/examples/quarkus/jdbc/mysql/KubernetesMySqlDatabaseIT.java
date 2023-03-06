package io.github.snowdrop.jester.examples.quarkus.jdbc.mysql;

import io.github.snowdrop.jester.api.KubernetesServiceConfiguration;
import io.github.snowdrop.jester.api.RunOnKubernetes;

@RunOnKubernetes
@KubernetesServiceConfiguration(forService = "database", useInternalService = true)
public class KubernetesMySqlDatabaseIT extends MySqlDatabaseIT {
}
