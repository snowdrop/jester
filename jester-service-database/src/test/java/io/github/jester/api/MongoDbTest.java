package io.github.jester.api;

public class MongoDbTest extends BaseTest {
    @MongoDbContainer
    static final DatabaseService database = new DatabaseService();
}
