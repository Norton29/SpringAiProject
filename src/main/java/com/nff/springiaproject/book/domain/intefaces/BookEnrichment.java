package com.nff.springiaproject.book.domain.intefaces;

public interface BookEnrichment {

    BookDetails enrich(String title, String author);

    record BookDetails(
            String review,
            String recommendations,
            String genre,
            String targetAudience,
            String language,
            String keywords
    ) {
    }
}
