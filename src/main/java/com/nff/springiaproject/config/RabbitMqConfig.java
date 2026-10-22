package com.nff.springiaproject.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class RabbitMqConfig {

    public static final String BOOK_ENRICHMENT_EXCHANGE = "book.enrichment.exchange";
    public static final String BOOK_ENRICHMENT_QUEUE = "book.enrichment.queue";
    public static final String BOOK_ENRICHMENT_ROUTING_KEY = "book.enrichment";
    public static final String BOOK_ENRICHMENT_DEAD_LETTER_EXCHANGE = "book.enrichment.dead-letter.exchange";
    public static final String BOOK_ENRICHMENT_DEAD_LETTER_QUEUE = "book.enrichment.dead-letter.queue";
    public static final String BOOK_ENRICHMENT_DEAD_LETTER_ROUTING_KEY = "book.enrichment.dead-letter";

    @Bean
    public DirectExchange bookEnrichmentExchange() {
        return new DirectExchange(BOOK_ENRICHMENT_EXCHANGE);
    }

    @Bean
    public DirectExchange bookEnrichmentDeadLetterExchange() {
        return new DirectExchange(BOOK_ENRICHMENT_DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Queue bookEnrichmentQueue() {
        return QueueBuilder.durable(BOOK_ENRICHMENT_QUEUE)
                .deadLetterExchange(BOOK_ENRICHMENT_DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(BOOK_ENRICHMENT_DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue bookEnrichmentDeadLetterQueue() {
        return QueueBuilder.durable(BOOK_ENRICHMENT_DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding bookEnrichmentBinding() {
        return BindingBuilder.bind(bookEnrichmentQueue())
                .to(bookEnrichmentExchange())
                .with(BOOK_ENRICHMENT_ROUTING_KEY);
    }

    @Bean
    public Binding bookEnrichmentDeadLetterBinding() {
        return BindingBuilder.bind(bookEnrichmentDeadLetterQueue())
                .to(bookEnrichmentDeadLetterExchange())
                .with(BOOK_ENRICHMENT_DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
