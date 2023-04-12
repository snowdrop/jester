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

    @Test
    @Order(1)
    public void create() {
        BookReactive bookReactive = new BookReactive();
        bookReactive.title = "Neuromancer";
        bookReactive.author = "William Gibson";
        bookReactive.id = 1L;

        given().contentType(ContentType.JSON).body(bookReactive).post("/book").then().statusCode(HttpStatus.SC_CREATED)
                .body("id", equalTo(1)).body("title", equalTo("Neuromancer")).body("author", equalTo("William Gibson"));
    }

    @Test
    @Order(2)
    public void getAll() {
        given().get("/book").then().statusCode(HttpStatus.SC_OK).body("", hasSize(1));
    }

    @Test
    @Order(3)
    public void get() {
        given().get("/book/1").then().statusCode(HttpStatus.SC_OK).body("title", equalTo("Neuromancer")).body("author",
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
        BookReactive bookReactive = new BookReactive();
        bookReactive.id = 1L;
        bookReactive.title = "Schismatrix";
        bookReactive.author = "Bruce Sterling";

        given().contentType(ContentType.JSON).body(bookReactive).put("/book/1").then().statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(1)).body("title", equalTo("Schismatrix")).body("author", equalTo("Bruce Sterling"));

        given().get("/book/1").then().statusCode(HttpStatus.SC_OK).body("title", equalTo("Schismatrix")).body("author",
                equalTo("Bruce Sterling"));
    }

    @Test
    @Order(6)
    public void updateWithUnknownId() {
        BookReactive bookReactive = new BookReactive();
        bookReactive.id = 2L;
        bookReactive.title = "foo";
        bookReactive.author = "bar";

        given().contentType(ContentType.JSON).body(bookReactive).put("/book/" + 2L).then()
                .statusCode(HttpStatus.SC_NOT_FOUND).body("code", equalTo(HttpStatus.SC_NOT_FOUND))
                .body("error", equalTo("book '2' not found"));
    }

    @Test
    @Order(7)
    public void updateInvalidPayload() {
        given().contentType(ContentType.TEXT).body("").put("/book/1").then()
                .statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE)
                .body("code", equalTo(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE));
    }

    @Test
    @Order(8)
    public void updateBadPayload() {
        BookReactive bookReactive = new BookReactive();

        given().contentType(ContentType.JSON).body(bookReactive).put("/book/1").then()
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .body("code", equalTo(HttpStatus.SC_UNPROCESSABLE_ENTITY))
                .body("error.message", containsInAnyOrder("book title must be set", "book author must be set"));
    }

    @Test
    @Order(9)
    public void delete() {
        given().delete("/book/1").then().statusCode(HttpStatus.SC_NO_CONTENT);

        given().get("/book/1").then().statusCode(HttpStatus.SC_NOT_FOUND).body("code", equalTo(HttpStatus.SC_NOT_FOUND))
                .body("error", equalTo("book '1' not found"));
    }

    @Test
    @Order(10)
    public void deleteWithUnknownId() {
        given().delete("/book/999").then().statusCode(HttpStatus.SC_NOT_FOUND)
                .body("code", equalTo(HttpStatus.SC_NOT_FOUND)).body("error", equalTo("book '999' not found"));
    }
}
