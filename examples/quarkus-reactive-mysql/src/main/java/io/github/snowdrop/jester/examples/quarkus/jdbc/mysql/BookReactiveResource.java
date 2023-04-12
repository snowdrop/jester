package io.github.snowdrop.jester.examples.quarkus.jdbc.mysql;

import java.util.List;

import org.jboss.resteasy.reactive.RestResponse;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/book")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookReactiveResource {

    @GET
    @WithSession
    public Uni<List<PanacheEntityBase>> getAll() {
        return BookReactive.listAll(Sort.by("title"));
    }

    @GET
    @Path("/{id}")
    @WithSession
    public Uni<BookReactive> get(@PathParam("id") Long id) {
        return BookReactive.findById(id).onItem().ifNull()
                .failWith(() -> new NotFoundException("book '" + id + "' not found")).map(book -> (BookReactive) book);
    }

    @POST
    @WithTransaction
    public Uni<RestResponse<BookReactive>> create(@Valid BookReactive bookReactive) {
        return BookReactive.getSession().flatMap(s -> s.merge(bookReactive))
                .map(dbBook -> RestResponse.status(RestResponse.Status.CREATED, dbBook));
    }

    @PUT
    @Path("/{id}")
    @WithTransaction
    public Uni<BookReactive> update(@PathParam("id") Long id, @Valid BookReactive newBookReactive) {
        return BookReactive.findById(id).onItem().ifNull()
                .failWith(() -> new NotFoundException("book '" + id + "' not found")).map(dbBook -> {
                    ((BookReactive) dbBook).title = newBookReactive.title;
                    ((BookReactive) dbBook).author = newBookReactive.author;
                    return (BookReactive) dbBook;
                });
    }

    @DELETE
    @Path("/{id}")
    @WithTransaction
    public Uni<RestResponse<Void>> delete(@PathParam("id") Long id) {
        return BookReactive.findById(id).onItem().ifNull()
                .failWith(() -> new NotFoundException("book '" + id + "' not found")).flatMap(PanacheEntityBase::delete)
                .map(x -> RestResponse.status(RestResponse.Status.NO_CONTENT));
    }
}
