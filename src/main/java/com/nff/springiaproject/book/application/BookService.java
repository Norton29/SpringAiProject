package com.nff.springiaproject.book.application;

import com.nff.springiaproject.book.domain.model.Book;
import com.nff.springiaproject.book.domain.intefaces.BookEnrichment;
import com.nff.springiaproject.book.domain.intefaces.BookEnrichmentQueues;
import com.nff.springiaproject.book.domain.BookEnrichmentStatus;
import com.nff.springiaproject.book.domain.intefaces.BookRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookEnrichment bookEnrichment;
    private final BookEnrichmentQueues bookEnrichmentQueues;

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Optional<Book> findById(UUID id) {
        return bookRepository.findById(id);
    }

    @Transactional
    public Book save(BookInput bookInput) {
        Book book = new Book();
        BeanUtils.copyProperties(bookInput, book);
        book.setEnrichmentStatus(BookEnrichmentStatus.PENDING);
        Book savedBook = bookRepository.save(book);
        bookEnrichmentQueues.enqueue(savedBook.getId());
        return savedBook;
    }

    public Book update(UUID id, BookInput bookInput) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + id));
        BeanUtils.copyProperties(bookInput, book);
        return bookRepository.save(book);
    }

    public void delete(UUID id) {
        bookRepository.deleteById(id);
    }

    public void enrichBook(UUID bookId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null || book.getEnrichmentStatus() == BookEnrichmentStatus.COMPLETED) {
            return;
        }

        BookEnrichment.BookDetails details = bookEnrichment.enrich(book.getTitle(), book.getAuthor());
        book.setReview(details.review());
        book.setRecommendations(details.recommendations());
        book.setGenre(details.genre());
        book.setTargetAudience(details.targetAudience());
        book.setLanguage(details.language());
        book.setKeywords(details.keywords());
        book.setEnrichmentStatus(BookEnrichmentStatus.COMPLETED);
        bookRepository.save(book);
    }

    public void markEnrichmentFailed(UUID bookId) {
        bookRepository.findById(bookId).ifPresent(book -> {
            book.setEnrichmentStatus(BookEnrichmentStatus.FAILED);
            bookRepository.save(book);
        });
    }

}
