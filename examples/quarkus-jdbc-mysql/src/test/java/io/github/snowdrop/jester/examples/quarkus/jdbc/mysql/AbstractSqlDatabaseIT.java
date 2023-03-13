package io.github.snowdrop.jester.examples.quarkus.jdbc.mysql;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.restassured.http.ContentType;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractSqlDatabaseIT {

    private static final int INVALID_ID = 999;

    @Test
    @Order(1)
    public void getAll() {
        given().get("/book").then().statusCode(HttpStatus.SC_OK).body("", hasSize(7));
    }

    @Test
    @Order(2)
    public void get() {
        given().get("/book/7").then().statusCode(HttpStatus.SC_OK).body("title", equalTo("Perdido Street Station"))
                .body("author", equalTo("China Miéville"));
    }

    @Test
    @Order(3)
    public void create() {
        Book book = new Book();
        book.id = 8;
        book.title = "Neuromancer";
        book.author = "William Gibson";

        given().contentType(ContentType.JSON).body(book).post("/book").then().statusCode(HttpStatus.SC_CREATED)
                .body("id", equalTo(8)).body("title", equalTo("Neuromancer")).body("author", equalTo("William Gibson"));

        given().get("/book/8").then().statusCode(HttpStatus.SC_OK).body("title", equalTo("Neuromancer")).body("author",
                equalTo("William Gibson"));
    }

    @Test
    @Order(4)
    public void createInvalidPayload() {
        given().contentType(ContentType.TEXT).body("").post("/book").then()
                .statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE)
                .body("code", equalTo(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE));
    }

    @Test
    @Order(5)
    public void update() {
        Book book = new Book();
        book.id = 8;
        book.title = "Schismatrix";
        book.author = "Bruce Sterling";

        given().contentType(ContentType.JSON).body(book).put("/book/" + 8).then().statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(8)).body("title", equalTo("Schismatrix")).body("author", equalTo("Bruce Sterling"));

        given().get("/book/" + 8).then().statusCode(HttpStatus.SC_OK).body("title", equalTo("Schismatrix"))
                .body("author", equalTo("Bruce Sterling"));
    }

    @Test
    @Order(6)
    public void updateWithUnknownId() {
        Book book = new Book();
        book.id = INVALID_ID;
        book.title = "foo";
        book.author = "bar";

        given().contentType(ContentType.JSON).body(book).put("/book/999").then().statusCode(HttpStatus.SC_NOT_FOUND)
                .body("code", equalTo(HttpStatus.SC_NOT_FOUND)).body("error", equalTo("book '999' not found"));
    }

    @Test
    @Order(7)
    public void updateInvalidPayload() {
        given().contentType(ContentType.TEXT).body("").put("/book/8").then()
                .statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE)
                .body("code", equalTo(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE));
    }

    @Test
    @Order(8)
    public void updateBadPayload() {
        Book book = new Book();

        given().contentType(ContentType.JSON).body(book).put("/book/8").then()
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .body("code", equalTo(HttpStatus.SC_UNPROCESSABLE_ENTITY))
                .body("error.message", containsInAnyOrder("book title must be set", "book author must be set"));
    }

    @Test
    @Order(9)
    public void delete() {
        given().delete("/book/8").then().statusCode(HttpStatus.SC_NO_CONTENT);

        given().get("/book/8").then().statusCode(HttpStatus.SC_NOT_FOUND).body("code", equalTo(HttpStatus.SC_NOT_FOUND))
                .body("error", equalTo("book '8' not found"));
    }

    @Test
    @Order(10)
    public void deleteWithUnknownId() {
        given().delete("/book/999").then().statusCode(HttpStatus.SC_NOT_FOUND)
                .body("code", equalTo(HttpStatus.SC_NOT_FOUND)).body("error", equalTo("book '999' not found"));
    }
}
