package io.github.snowdrop.jester.examples.quarkus.jdbc.mysql;

import io.github.snowdrop.jester.api.DatabaseService;
import io.github.snowdrop.jester.api.Jester;
import io.github.snowdrop.jester.api.MySqlContainer;
import io.github.snowdrop.jester.api.Quarkus;
import io.github.snowdrop.jester.api.RestService;

@Jester
public class MySqlDatabaseIT extends AbstractSqlDatabaseIT {

    @MySqlContainer
    static DatabaseService database = new DatabaseService();

    @Quarkus
    static RestService app = new RestService().withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);
}
