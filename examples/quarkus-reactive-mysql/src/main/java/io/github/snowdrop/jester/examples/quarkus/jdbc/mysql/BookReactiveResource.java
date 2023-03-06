package io.github.snowdrop.jester.examples.quarkus.jdbc.mysql;

import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.RestResponse;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;

@Path("/book")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookReactiveResource {

    @GET
    public Uni<List<PanacheEntityBase>> getAll() {
        return BookReactive.listAll(Sort.by("title"));
    }

    @GET
    @Path("/{id}")
    public Uni<BookReactive> get(@PathParam("id") Long id) {
        return BookReactive.findById(id).onItem().ifNull()
                .failWith(() -> new NotFoundException("book '" + id + "' not found")).map(book -> (BookReactive) book);
    }

    @POST
    @ReactiveTransactional
    public Uni<RestResponse<BookReactive>> create(@Valid BookReactive bookReactive) {
        if (bookReactive.id != null) {
            throw new ClientErrorException("unexpected ID in request", ValidationExceptionMapper.UNPROCESSABLE_ENTITY);
        }
        return bookReactive.persist()
                .map(dbBook -> RestResponse.status(RestResponse.Status.CREATED, (BookReactive) dbBook));
    }

    @PUT
    @Path("/{id}")
    @ReactiveTransactional
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
    @ReactiveTransactional
    public Uni<RestResponse<Void>> delete(@PathParam("id") Long id) {
        return BookReactive.findById(id).onItem().ifNull()
                .failWith(() -> new NotFoundException("book '" + id + "' not found")).flatMap(PanacheEntityBase::delete)
                .map(x -> RestResponse.status(RestResponse.Status.NO_CONTENT));
    }
}
