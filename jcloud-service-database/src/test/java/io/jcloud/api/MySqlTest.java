package io.jcloud.api;

public class MySqlTest extends BaseTest {
    @MySqlContainer
    static final DatabaseService database = new DatabaseService();
}
