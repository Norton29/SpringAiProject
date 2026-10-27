package com.nff.springiaproject.book.adapter.output.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_book_enrichment_outboxes")
@Getter
@Setter
@NoArgsConstructor
public class BookEnrichmentQueueMessageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID bookId;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = true)
    private Instant publishedAt;

    @PrePersist
    public void initializeCreatedAt() {
        createdAt = Instant.now();
    }
}
