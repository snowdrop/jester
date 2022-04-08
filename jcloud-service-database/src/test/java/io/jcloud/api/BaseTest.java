package io.jcloud.api;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

@JCloud
public abstract class BaseTest {

    @LookupService
    DatabaseService database;

    @Test
    public void shouldBeUpAndRunning() {
        assertTrue(database.isRunning());
    }
}
