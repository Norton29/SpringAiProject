package com.nff.springiaproject.book.domain.model;

import java.util.UUID;

import com.nff.springiaproject.book.domain.BookEnrichmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    private UUID id;
    private String title;
    private String author;
    private String publisher;
    private Integer publicationYear;
    private String review;
    private String recommendations;
    private String genre;
    private String targetAudience;
    private String language;
    private String keywords;
    private BookEnrichmentStatus enrichmentStatus;
}
