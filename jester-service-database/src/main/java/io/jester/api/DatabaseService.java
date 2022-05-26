package io.jester.api;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import io.jester.core.BaseService;

public class DatabaseService extends BaseService<DatabaseService> {

    private static final String USER_DEFAULT_VALUE = "user";
    private static final String PASSWORD_DEFAULT_VALUE = "user";
    private static final String DATABASE_DEFAULT_VALUE = "mydb";
    private static final String JDBC_NAME = Pattern.quote("${JDBC_NAME}");
    private static final String HOST = Pattern.quote("${HOST}");
    private static final String PORT = Pattern.quote("${PORT}");
    private static final String DATABASE = Pattern.quote("${DATABASE}");
    private static final String JDBC_URL_PATTERN = "jdbc:${JDBC_NAME}://${HOST}:${PORT}/${DATABASE}";
    private static final String REACTIVE_URL_PATTERN = "${JDBC_NAME}://${HOST}:${PORT}/${DATABASE}";

    private String user = USER_DEFAULT_VALUE;
    private String password = PASSWORD_DEFAULT_VALUE;
    private String database = DATABASE_DEFAULT_VALUE;
    private String jdbcUrlPattern = JDBC_URL_PATTERN;
    private String reactiveUrlPattern = REACTIVE_URL_PATTERN;

    private String jdbcName;
    private String databaseNameProperty;
    private String userProperty;
    private String passwordProperty;

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }

    public String getJdbcUrl() {
        return jdbcUrlPattern.replaceAll(JDBC_NAME, jdbcName).replaceAll(HOST, getHost())
                .replaceAll(PORT, "" + getFirstMappedPort()).replaceAll(DATABASE, getDatabase());
    }

    public String getReactiveUrl() {
        return reactiveUrlPattern.replaceAll(JDBC_NAME, jdbcName).replaceAll(HOST, getHost())
                .replaceAll(PORT, "" + getFirstMappedPort()).replaceAll(DATABASE, getDatabase());
    }

    public DatabaseService overrideDefaults(String user, String password, String database) {
        this.user = user;
        this.password = password;
        this.database = database;
        return this;
    }

    public DatabaseService with(String user, String password, String database) {
        withUser(user);
        withPassword(password);
        withDatabase(database);
        return this;
    }

    public DatabaseService withUser(String user) {
        if (StringUtils.isEmpty(userProperty)) {
            throw new UnsupportedOperationException(
                    "You cannot configure the user in the current database because the user property has not been provided");
        }

        this.user = user;
        return this;
    }

    public DatabaseService withPassword(String password) {
        if (StringUtils.isEmpty(passwordProperty)) {
            throw new UnsupportedOperationException("You cannot configure the password in the current database because "
                    + "the password property has not been provided");
        }

        this.password = password;
        return this;
    }

    public DatabaseService withDatabase(String database) {
        if (StringUtils.isEmpty(databaseNameProperty)) {
            throw new UnsupportedOperationException(
                    "You cannot configure the database name in the current database because "
                            + "the database name property has not been provided");
        }

        this.database = database;
        return this;
    }

    public DatabaseService withJdbcName(String jdbcName) {
        this.jdbcName = jdbcName;
        return this;
    }

    public DatabaseService withDatabaseNameProperty(String databaseNameProperty) {
        this.databaseNameProperty = databaseNameProperty;
        return this;
    }

    public DatabaseService withUserProperty(String userProperty) {
        this.userProperty = userProperty;
        return this;
    }

    public DatabaseService withPasswordProperty(String passwordProperty) {
        this.passwordProperty = passwordProperty;
        return this;
    }

    public DatabaseService withJdbcUrlPattern(String jdbcUrlPattern) {
        this.jdbcUrlPattern = jdbcUrlPattern;
        return this;
    }

    public DatabaseService withReactiveUrlPattern(String reactiveUrlPattern) {
        this.reactiveUrlPattern = reactiveUrlPattern;
        return this;
    }

    @Override
    public DatabaseService onPreStart(HookAction action) {
        if (StringUtils.isNotEmpty(userProperty)) {
            withProperty(userProperty, getUser());
        }

        if (StringUtils.isNotEmpty(passwordProperty)) {
            withProperty(passwordProperty, getPassword());
        }

        if (StringUtils.isNotEmpty(databaseNameProperty)) {
            withProperty(databaseNameProperty, getDatabase());
        }

        return super.onPreStart(action);
    }
}
