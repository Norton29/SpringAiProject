package com.nff.springiaproject.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.nff.springiaproject.book.application.BookInput;
import com.nff.springiaproject.book.application.BookEnrichmentRequest;
import com.nff.springiaproject.book.application.BookService;
import com.nff.springiaproject.book.domain.model.Book;
import com.nff.springiaproject.book.domain.intefaces.BookEnrichment;
import com.nff.springiaproject.book.domain.BookEnrichmentStatus;
import com.nff.springiaproject.book.domain.intefaces.BookRepository;
import com.nff.springiaproject.config.RabbitMqConfig;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "book.enrichment.queue-poll-interval=100",
                "spring.ai.google.genai.api-key=test-api-key"
        }
)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Import(BookEnrichmentFlowIntegrationTest.FakeBookEnrichmentConfiguration.class)
class BookEnrichmentFlowIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:18")
    );

    @Container
    private static final RabbitMQContainer RABBITMQ = new RabbitMQContainer(
            DockerImageName.parse("rabbitmq:4-management")
    );

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private FakeBookEnrichment fakeBookEnrichment;

    @DynamicPropertySource
    static void configureContainers(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.rabbitmq.host", RABBITMQ::getHost);
        registry.add("spring.rabbitmq.port", RABBITMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBITMQ::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBITMQ::getAdminPassword);
    }

    @Test
    void savesBookPublishesMessageAndPersistsEnrichmentFromRabbitListener() {
        Book savedBook = bookService.save(new BookInput(
                "Clean Code",
                "Robert C. Martin",
                "Prentice Hall",
                2008
        ));

        assertThat(savedBook.getEnrichmentStatus()).isEqualTo(BookEnrichmentStatus.PENDING);

        await()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    Book enrichedBook = bookRepository.findById(savedBook.getId()).orElseThrow();

                    assertThat(enrichedBook.getEnrichmentStatus()).isEqualTo(BookEnrichmentStatus.COMPLETED);
                    assertThat(enrichedBook.getReview()).isEqualTo("Generated review");
                    assertThat(enrichedBook.getRecommendations()).isEqualTo("Generated recommendations");
                    assertThat(enrichedBook.getGenre()).isEqualTo("Software Engineering");
                    assertThat(enrichedBook.getTargetAudience()).isEqualTo("Software developers");
                    assertThat(enrichedBook.getLanguage()).isEqualTo("English");
                    assertThat(enrichedBook.getKeywords()).isEqualTo("clean code, software design");
                });
    }

    @Test
    void processesDuplicateRabbitMessagesWithoutEnrichingBookTwice() {
        fakeBookEnrichment.resetInvocationCount();

        Book book = new Book();
        book.setTitle("Clean Code");
        book.setAuthor("Robert C. Martin");
        book.setPublisher("Prentice Hall");
        book.setPublicationYear(2008);
        book.setEnrichmentStatus(BookEnrichmentStatus.PENDING);
        Book savedBook = bookRepository.save(book);

        BookEnrichmentRequest message = new BookEnrichmentRequest(savedBook.getId());
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.BOOK_ENRICHMENT_EXCHANGE,
                RabbitMqConfig.BOOK_ENRICHMENT_ROUTING_KEY,
                message
        );
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.BOOK_ENRICHMENT_EXCHANGE,
                RabbitMqConfig.BOOK_ENRICHMENT_ROUTING_KEY,
                message
        );

        await()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    Book enrichedBook = bookRepository.findById(savedBook.getId()).orElseThrow();
                    long queuedMessages = rabbitTemplate.execute(
                            channel -> channel.messageCount(RabbitMqConfig.BOOK_ENRICHMENT_QUEUE)
                    );

                    assertThat(enrichedBook.getEnrichmentStatus()).isEqualTo(BookEnrichmentStatus.COMPLETED);
                    assertThat(enrichedBook.getReview()).isEqualTo("Generated review");
                    assertThat(enrichedBook.getRecommendations()).isEqualTo("Generated recommendations");
                    assertThat(enrichedBook.getGenre()).isEqualTo("Software Engineering");
                    assertThat(enrichedBook.getTargetAudience()).isEqualTo("Software developers");
                    assertThat(enrichedBook.getLanguage()).isEqualTo("English");
                    assertThat(enrichedBook.getKeywords()).isEqualTo("clean code, software design");
                    assertThat(queuedMessages).isZero();
                });

        assertThat(fakeBookEnrichment.getInvocationCount()).isEqualTo(1);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class FakeBookEnrichmentConfiguration {

        @Bean
        @Primary
        FakeBookEnrichment fakeBookEnrichment() {
            return new FakeBookEnrichment();
        }
    }

    static class FakeBookEnrichment implements BookEnrichment {

        private final AtomicInteger invocationCount = new AtomicInteger();

        @Override
        public BookDetails enrich(String title, String author) {
            invocationCount.incrementAndGet();
            return new BookDetails(
                    "Generated review",
                    "Generated recommendations",
                    "Software Engineering",
                    "Software developers",
                    "English",
                    "clean code, software design"
            );
        }

        int getInvocationCount() {
            return invocationCount.get();
        }

        void resetInvocationCount() {
            invocationCount.set(0);
        }
    }
}
