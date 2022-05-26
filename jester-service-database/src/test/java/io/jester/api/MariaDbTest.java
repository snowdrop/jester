package io.jester.api;

public class MariaDbTest extends BaseTest {
    @MariaDbContainer
    static final DatabaseService database = new DatabaseService();
}
