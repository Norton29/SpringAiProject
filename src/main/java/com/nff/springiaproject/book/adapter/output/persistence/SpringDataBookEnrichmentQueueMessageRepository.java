package com.nff.springiaproject.book.adapter.output.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataBookEnrichmentQueueMessageRepository
        extends JpaRepository<BookEnrichmentQueueMessageJpaEntity, UUID> {

    List<BookEnrichmentQueueMessageJpaEntity> findByPublishedAtIsNullOrderByCreatedAtAsc(Pageable pageable);
}
