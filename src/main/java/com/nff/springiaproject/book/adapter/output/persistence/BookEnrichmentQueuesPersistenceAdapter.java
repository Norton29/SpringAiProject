package com.nff.springiaproject.book.adapter.output.persistence;

import com.nff.springiaproject.book.domain.BookEnrichmentQueues;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookEnrichmentQueuesPersistenceAdapter implements BookEnrichmentQueues {

    private final SpringDataBookEnrichmentQueueMessageRepository queueMessageRepository;

    @Override
    public void enqueue(UUID bookId) {
        BookEnrichmentQueueMessageJpaEntity queueMessage = new BookEnrichmentQueueMessageJpaEntity();
        queueMessage.setBookId(bookId);
        queueMessageRepository.save(queueMessage);
    }

    @Override
    public List<PendingMessage> findPending(int limit) {
        return queueMessageRepository.findByPublishedAtIsNullOrderByCreatedAtAsc(PageRequest.of(0, limit)).stream()
                .map(item -> new PendingMessage(item.getId(), item.getBookId()))
                .toList();
    }

    @Override
    public void markPublished(UUID messageId) {
        queueMessageRepository.findById(messageId).ifPresent(queueMessage -> {
            queueMessage.setPublishedAt(Instant.now());
            queueMessageRepository.save(queueMessage);
        });
    }
}
