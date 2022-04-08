package io.jcloud.examples.quarkus.jdbc.mysql;

import io.jcloud.api.KubernetesServiceConfiguration;
import io.jcloud.api.RunOnKubernetes;

@RunOnKubernetes
@KubernetesServiceConfiguration(forService = "database", useInternalService = true)
public class KubernetesMySqlDatabaseIT extends MySqlDatabaseIT {
}
