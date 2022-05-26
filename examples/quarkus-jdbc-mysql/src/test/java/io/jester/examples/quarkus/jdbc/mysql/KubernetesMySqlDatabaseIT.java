package io.jester.examples.quarkus.jdbc.mysql;

import io.jester.api.KubernetesServiceConfiguration;
import io.jester.api.RunOnKubernetes;

@RunOnKubernetes
@KubernetesServiceConfiguration(forService = "database", useInternalService = true)
public class KubernetesMySqlDatabaseIT extends MySqlDatabaseIT {
}
