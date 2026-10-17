package com.nff.springiaproject.controllers;

import com.nff.springiaproject.configs.WebConfig;
import com.nff.springiaproject.dtos.BookRecordDto;
import com.nff.springiaproject.models.BookModel;
import com.nff.springiaproject.services.BookService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping(version = "v1")
    public ResponseEntity<CollectionModel<EntityModel<BookModel>>> getAllBooks() {
        List<BookModel> books = bookService.findAll();
        List<EntityModel<BookModel>> bookModels = books.stream()
                .map(this::toBookModel)
                .toList();
        CollectionModel<EntityModel<BookModel>> response = CollectionModel.of(
                bookModels,
                linkTo(methodOn(BookController.class).getAllBooks()).withSelfRel()
        );
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(version = "v2")
    public ResponseEntity<List<BookModel>> getAllBooksV2() {
        List<BookModel> books = bookService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(books);
    }

    @GetMapping(value = "/{id}", version = "v1")
    public ResponseEntity<EntityModel<BookModel>> getOneBook(@PathVariable(value = "id") UUID id) {
        return bookService.findById(id)
                .map(this::toBookModel)
                .map(bookModel -> ResponseEntity.status(HttpStatus.OK).body(bookModel))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping(value = "/{id}", version = "v2")
    public ResponseEntity<BookModel> getOneBookV2(@PathVariable(value = "id") UUID id) {
        return bookService.findById(id)
                .map(bookModel -> ResponseEntity.status(HttpStatus.OK).body(bookModel))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping(version = "v1")
    public ResponseEntity<EntityModel<BookModel>> saveBook(@RequestBody @Valid BookRecordDto bookRecordDto) {
        BookModel bookModel = bookService.save(bookRecordDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toBookModel(bookModel));
    }

    @PostMapping(version = "v2")
    public ResponseEntity<BookModel> saveBookV2(@RequestBody @Valid BookRecordDto bookRecordDto) {
        BookModel bookModel = bookService.save(bookRecordDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookModel);
    }

    @PutMapping(value = "/{id}", version = "v1")
    public ResponseEntity<EntityModel<BookModel>> updateBook(
            @PathVariable(value = "id") UUID id,
            @RequestBody @Valid BookRecordDto bookRecordDto
    ) {
        if (bookService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        BookModel bookModel = bookService.update(id, bookRecordDto);
        return ResponseEntity.status(HttpStatus.OK).body(toBookModel(bookModel));
    }

    @PutMapping(value = "/{id}", version = "v2")
    public ResponseEntity<BookModel> updateBookV2(
            @PathVariable(value = "id") UUID id,
            @RequestBody @Valid BookRecordDto bookRecordDto
    ) {
        if (bookService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        BookModel bookModel = bookService.update(id, bookRecordDto);
        return ResponseEntity.status(HttpStatus.OK).body(bookModel);
    }

    @DeleteMapping(value = "/{id}", version = "v1")
    public ResponseEntity<RepresentationModel<?>> deleteBook(@PathVariable(value = "id") UUID id) {
        if (bookService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        bookService.delete(id);
        RepresentationModel<?> response = new RepresentationModel<>();
        response.add(linkTo(methodOn(BookController.class).getAllBooks()).withRel("books"));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping(value = "/{id}", version = "v2")
    public ResponseEntity<Void> deleteBookV2(@PathVariable(value = "id") UUID id) {
        if (bookService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        bookService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private EntityModel<BookModel> toBookModel(BookModel bookModel) {
        return EntityModel.of(
                bookModel,
                linkTo(methodOn(BookController.class).getOneBook(bookModel.getId())).withSelfRel(),
                linkTo(methodOn(BookController.class).getAllBooks()).withRel("books"),
                linkTo(methodOn(BookController.class).updateBook(bookModel.getId(), null)).withRel("update"),
                linkTo(methodOn(BookController.class).deleteBook(bookModel.getId())).withRel("delete")
        );
    }
}
