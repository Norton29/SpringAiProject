package com.nff.springiaproject.book.adapter.output.persistence;

import com.nff.springiaproject.book.domain.Book;
import com.nff.springiaproject.book.domain.BookRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookPersistenceAdapter implements BookRepository {

    private final SpringDataBookRepository springDataBookRepository;

    @Override
    public List<Book> findAll() {
        return springDataBookRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Book> findById(UUID id) {
        return springDataBookRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Book save(Book book) {
        BookJpaEntity entity = toEntity(book);
        return toDomain(springDataBookRepository.save(entity));
    }

    @Override
    public void deleteById(UUID id) {
        springDataBookRepository.deleteById(id);
    }

    private Book toDomain(BookJpaEntity entity) {
        return new Book(
                entity.getId(),
                entity.getTitle(),
                entity.getAuthor(),
                entity.getPublisher(),
                entity.getPublicationYear(),
                entity.getReview(),
                entity.getRecommendations(),
                entity.getGenre(),
                entity.getTargetAudience(),
                entity.getLanguage(),
                entity.getKeywords(),
                entity.getEnrichmentStatus()
        );
    }

    private BookJpaEntity toEntity(Book book) {
        BookJpaEntity entity = new BookJpaEntity();
        entity.setId(book.getId());
        entity.setTitle(book.getTitle());
        entity.setAuthor(book.getAuthor());
        entity.setPublisher(book.getPublisher());
        entity.setPublicationYear(book.getPublicationYear());
        entity.setReview(book.getReview());
        entity.setRecommendations(book.getRecommendations());
        entity.setGenre(book.getGenre());
        entity.setTargetAudience(book.getTargetAudience());
        entity.setLanguage(book.getLanguage());
        entity.setKeywords(book.getKeywords());
        entity.setEnrichmentStatus(book.getEnrichmentStatus());
        return entity;
    }
}
