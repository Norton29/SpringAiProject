package com.nff.springiaproject.book.adapter.input.web;

import com.nff.springiaproject.book.application.BookService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final BookWebMapper bookWebMapper;

    @GetMapping(version = "v1")
    public ResponseEntity<List<BookResponseDto>> getAllBooks() {
        List<BookResponseDto> books = bookService.findAll().stream()
                .map(bookWebMapper::toResponse)
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(books);
    }

    @GetMapping(version = "v2")
    public ResponseEntity<List<BookResponseDto>> getAllBooksV2() {
        List<BookResponseDto> books = bookService.findAll().stream()
                .map(bookWebMapper::toResponse)
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(books);
    }

    @GetMapping(value = "/{id}", version = "v1")
    public ResponseEntity<BookResponseDto> getOneBook(@PathVariable(value = "id") UUID id) {
        return bookService.findById(id)
                .map(bookWebMapper::toResponse)
                .map(book -> ResponseEntity.status(HttpStatus.OK).body(book))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping(value = "/{id}", version = "v2")
    public ResponseEntity<BookResponseDto> getOneBookV2(@PathVariable(value = "id") UUID id) {
        return bookService.findById(id)
                .map(bookWebMapper::toResponse)
                .map(book -> ResponseEntity.status(HttpStatus.OK).body(book))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping(version = "v1")
    public ResponseEntity<BookResponseDto> saveBook(@RequestBody @Valid BookRecordDto bookRecordDto) {
        BookResponseDto book = bookWebMapper.toResponse(bookService.save(bookWebMapper.toInput(bookRecordDto)));
        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }

    @PostMapping(version = "v2")
    public ResponseEntity<BookResponseDto> saveBookV2(@RequestBody @Valid BookRecordDto bookRecordDto) {
        BookResponseDto book = bookWebMapper.toResponse(bookService.save(bookWebMapper.toInput(bookRecordDto)));
        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }

    @PutMapping(value = "/{id}", version = "v1")
    public ResponseEntity<BookResponseDto> updateBook(
            @PathVariable(value = "id") UUID id,
            @RequestBody @Valid BookRecordDto bookRecordDto
    ) {
        if (bookService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        BookResponseDto book = bookWebMapper.toResponse(
                bookService.update(id, bookWebMapper.toInput(bookRecordDto))
        );
        return ResponseEntity.status(HttpStatus.OK).body(book);
    }

    @PutMapping(value = "/{id}", version = "v2")
    public ResponseEntity<BookResponseDto> updateBookV2(
            @PathVariable(value = "id") UUID id,
            @RequestBody @Valid BookRecordDto bookRecordDto
    ) {
        if (bookService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        BookResponseDto book = bookWebMapper.toResponse(
                bookService.update(id, bookWebMapper.toInput(bookRecordDto))
        );
        return ResponseEntity.status(HttpStatus.OK).body(book);
    }

    @DeleteMapping(value = "/{id}", version = "v1")
    public ResponseEntity<Void> deleteBook(@PathVariable(value = "id") UUID id) {
        if (bookService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        bookService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping(value = "/{id}", version = "v2")
    public ResponseEntity<Void> deleteBookV2(@PathVariable(value = "id") UUID id) {
        if (bookService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        bookService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
