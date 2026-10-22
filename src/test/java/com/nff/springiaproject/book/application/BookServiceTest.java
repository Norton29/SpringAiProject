package com.nff.springiaproject.book.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nff.springiaproject.book.domain.Book;
import com.nff.springiaproject.book.domain.BookEnrichment;
import com.nff.springiaproject.book.domain.BookEnrichmentQueues;
import com.nff.springiaproject.book.domain.BookEnrichmentStatus;
import com.nff.springiaproject.book.domain.BookRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookEnrichment bookEnrichment;

    @Mock
    private BookEnrichmentQueues bookEnrichmentQueues;

    @InjectMocks
    private BookService bookService;

    @Test
    void savePersistsBookAsPendingAndEnqueuesItsId() {
        UUID bookId = UUID.randomUUID();
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            book.setId(bookId);
            return book;
        });

        Book savedBook = bookService.save(new BookInput("Clean Code", "Robert C. Martin", "Prentice Hall", 2008));

        assertThat(savedBook.getId()).isEqualTo(bookId);
        assertThat(savedBook.getEnrichmentStatus()).isEqualTo(BookEnrichmentStatus.PENDING);
        assertThat(savedBook.getTitle()).isEqualTo("Clean Code");
        verify(bookEnrichmentQueues).enqueue(bookId);
        verify(bookEnrichment, never()).enrich(any(), any());
    }

    @Test
    void enrichBookStoresGeminiDetailsAndMarksBookCompleted() {
        UUID bookId = UUID.randomUUID();
        Book book = new Book();
        book.setId(bookId);
        book.setTitle("Clean Code");
        book.setAuthor("Robert C. Martin");
        book.setEnrichmentStatus(BookEnrichmentStatus.PENDING);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookEnrichment.enrich("Clean Code", "Robert C. Martin"))
                .thenReturn(new BookEnrichment.BookDetails(
                        "A book about writing clean code.",
                        "Refactoring, The Pragmatic Programmer, Clean Architecture",
                        "Software Engineering",
                        "Software developers",
                        "English",
                        "clean code, software design"
                ));
        when(bookRepository.save(book)).thenReturn(book);

        bookService.enrichBook(bookId);

        assertThat(book.getReview()).isEqualTo("A book about writing clean code.");
        assertThat(book.getRecommendations()).contains("Refactoring");
        assertThat(book.getGenre()).isEqualTo("Software Engineering");
        assertThat(book.getTargetAudience()).isEqualTo("Software developers");
        assertThat(book.getLanguage()).isEqualTo("English");
        assertThat(book.getKeywords()).isEqualTo("clean code, software design");
        assertThat(book.getEnrichmentStatus()).isEqualTo(BookEnrichmentStatus.COMPLETED);
        verify(bookRepository).save(book);
    }

    @Test
    void enrichBookDoesNothingWhenBookDoesNotExist() {
        UUID bookId = UUID.randomUUID();
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        bookService.enrichBook(bookId);

        verify(bookEnrichment, never()).enrich(any(), any());
        verify(bookRepository, never()).save(any());
    }

    @Test
    void enrichBookDoesNotCallGeminiWhenAlreadyCompleted() {
        UUID bookId = UUID.randomUUID();
        Book book = new Book();
        book.setId(bookId);
        book.setEnrichmentStatus(BookEnrichmentStatus.COMPLETED);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        bookService.enrichBook(bookId);

        verify(bookEnrichment, never()).enrich(any(), any());
        verify(bookRepository, never()).save(any());
    }
}
