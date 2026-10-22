package com.nff.springiaproject.book.application;

public record BookInput(
        String title,
        String author,
        String publisher,
        Integer publicationYear
) {
}
