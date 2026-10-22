package com.nff.springiaproject.book.adapter.input.messaging;

import com.nff.springiaproject.book.application.BookEnrichmentRequest;
import com.nff.springiaproject.book.application.BookService;
import com.nff.springiaproject.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookEnrichmentListener {

    private final BookService bookService;

    @RabbitListener(queues = RabbitMqConfig.BOOK_ENRICHMENT_QUEUE)
    public void process(BookEnrichmentRequest request) {
        bookService.enrichBook(request.bookId());
    }
}
