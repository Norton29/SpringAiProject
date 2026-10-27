package com.nff.springiaproject.book.domain.intefaces;

import java.util.List;
import java.util.UUID;

public interface BookEnrichmentQueues {

    void enqueue(UUID bookId);

    List<PendingMessage> findPending(int limit);

    void markPublished(UUID messageId);

    record PendingMessage(UUID id, UUID bookId) {
    }
}
