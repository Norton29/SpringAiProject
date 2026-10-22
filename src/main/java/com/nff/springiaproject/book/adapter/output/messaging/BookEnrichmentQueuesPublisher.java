package com.nff.springiaproject.book.adapter.output.messaging;

import com.nff.springiaproject.book.application.BookEnrichmentRequest;
import com.nff.springiaproject.book.domain.BookEnrichmentQueues;
import com.nff.springiaproject.config.RabbitMqConfig;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookEnrichmentQueuesPublisher {

    private final BookEnrichmentQueues bookEnrichmentQueues;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelayString = "${book.enrichment.queue-poll-interval:1000}")
    public void publishPendingMessages() {
        bookEnrichmentQueues.findPending(20).forEach(this::publish);
    }

    private void publish(BookEnrichmentQueues.PendingMessage pendingMessage) {
        CorrelationData correlationData = new CorrelationData(pendingMessage.id().toString());

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.BOOK_ENRICHMENT_EXCHANGE,
                    RabbitMqConfig.BOOK_ENRICHMENT_ROUTING_KEY,
                    new BookEnrichmentRequest(pendingMessage.bookId()),
                    message -> {
                        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return message;
                    },
                    correlationData
            );

            CorrelationData.Confirm confirmation = correlationData.getFuture().get(10, TimeUnit.SECONDS);
            if (!confirmation.ack() || correlationData.getReturned() != null) {
                throw new IllegalStateException("RabbitMQ did not confirm the book enrichment message.");
            }

            bookEnrichmentQueues.markPublished(pendingMessage.id());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for the RabbitMQ confirmation.", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not publish the book enrichment message.", exception);
        }
    }
}
