package com.nff.springiaproject.book.adapter.input.web;

import com.nff.springiaproject.book.domain.BookEnrichmentStatus;
import java.util.UUID;

public record BookResponseDto(
        UUID id,
        String title,
        String author,
        String publisher,
        Integer publicationYear,
        String review,
        String recommendations,
        String genre,
        String targetAudience,
        String language,
        String keywords,
        BookEnrichmentStatus enrichmentStatus
) {
}
