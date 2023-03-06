package io.github.snowdrop.jester.api;

public class MySqlTest extends BaseTest {
    @MySqlContainer
    static final DatabaseService database = new DatabaseService();
}
