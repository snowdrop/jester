package io.jcloud.examples.quarkus.jdbc.mysql;

import io.jcloud.api.DatabaseService;
import io.jcloud.api.JCloud;
import io.jcloud.api.MySqlContainer;
import io.jcloud.api.Quarkus;
import io.jcloud.api.RestService;

@JCloud
public class MySqlDatabaseIT extends AbstractSqlDatabaseIT {

    @MySqlContainer
    static DatabaseService database = new DatabaseService();

    @Quarkus
    static RestService app = new RestService().withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);
}
