package io.github.snowdrop.jester.api;

public class MongoDbTest extends BaseTest {
    @MongoDbContainer
    static final DatabaseService database = new DatabaseService();
}
