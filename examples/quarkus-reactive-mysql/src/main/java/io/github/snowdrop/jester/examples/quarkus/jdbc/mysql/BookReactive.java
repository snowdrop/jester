package io.github.snowdrop.jester.examples.quarkus.jdbc.mysql;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "book")
public class BookReactive extends PanacheEntity {
    @NotBlank(message = "book title must be set")
    public String title;

    @NotBlank(message = "book author must be set")
    public String author;
}
