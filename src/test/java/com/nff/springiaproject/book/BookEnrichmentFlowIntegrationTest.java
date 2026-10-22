package com.nff.springiaproject.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.nff.springiaproject.book.application.BookInput;
import com.nff.springiaproject.book.application.BookService;
import com.nff.springiaproject.book.domain.Book;
import com.nff.springiaproject.book.domain.BookEnrichment;
import com.nff.springiaproject.book.domain.BookEnrichmentStatus;
import com.nff.springiaproject.book.domain.BookRepository;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
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

    @TestConfiguration(proxyBeanMethods = false)
    static class FakeBookEnrichmentConfiguration {

        @Bean
        @Primary
        BookEnrichment fakeBookEnrichment() {
            return (title, author) -> new BookEnrichment.BookDetails(
                    "Generated review",
                    "Generated recommendations",
                    "Software Engineering",
                    "Software developers",
                    "English",
                    "clean code, software design"
            );
        }
    }
}
