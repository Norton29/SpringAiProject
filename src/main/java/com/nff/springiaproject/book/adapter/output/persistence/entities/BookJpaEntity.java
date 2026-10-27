package com.nff.springiaproject.book.adapter.output.persistence.entities;

import com.nff.springiaproject.book.domain.BookEnrichmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_books")
@Getter
@Setter
@NoArgsConstructor
public class BookJpaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(nullable = false, length = 100)
    private String publisher;

    @Column(nullable = false)
    private Integer publicationYear;

    @Column(nullable = true, length = 10000, columnDefinition = "TEXT")
    private String review;

    @Column(nullable = true, length = 10000, columnDefinition = "TEXT")
    private String recommendations;

    @Column(nullable = true, length = 100)
    private String genre;

    @Column(nullable = true, length = 150)
    private String targetAudience;

    @Column(nullable = true, length = 100)
    private String language;

    @Column(nullable = true, length = 1000)
    private String keywords;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 20)
    private BookEnrichmentStatus enrichmentStatus;
}
