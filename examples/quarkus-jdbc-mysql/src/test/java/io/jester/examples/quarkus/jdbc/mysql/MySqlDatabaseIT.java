package io.jester.examples.quarkus.jdbc.mysql;

import io.jester.api.DatabaseService;
import io.jester.api.Jester;
import io.jester.api.MySqlContainer;
import io.jester.api.Quarkus;
import io.jester.api.RestService;

@Jester
public class MySqlDatabaseIT extends AbstractSqlDatabaseIT {

    @MySqlContainer
    static DatabaseService database = new DatabaseService();

    @Quarkus
    static RestService app = new RestService().withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);
}
