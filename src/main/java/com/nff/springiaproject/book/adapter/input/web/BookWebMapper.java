package com.nff.springiaproject.book.adapter.input.web;

import com.nff.springiaproject.book.application.BookInput;
import com.nff.springiaproject.book.domain.Book;
import org.springframework.stereotype.Component;

@Component
public class BookWebMapper {

    public BookInput toInput(BookRecordDto bookRecordDto) {
        return new BookInput(
                bookRecordDto.title(),
                bookRecordDto.author(),
                bookRecordDto.publisher(),
                bookRecordDto.publicationYear()
        );
    }

    public BookResponseDto toResponse(Book book) {
        return new BookResponseDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getPublicationYear(),
                book.getReview(),
                book.getRecommendations(),
                book.getGenre(),
                book.getTargetAudience(),
                book.getLanguage(),
                book.getKeywords(),
                book.getEnrichmentStatus()
        );
    }
}
