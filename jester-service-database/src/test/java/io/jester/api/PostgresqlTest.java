package io.jester.api;

public class PostgresqlTest extends BaseTest {
    @PostgresqlContainer
    static final DatabaseService database = new DatabaseService();
}
