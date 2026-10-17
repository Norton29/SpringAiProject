package com.nff.springiaproject.services;

import com.nff.springiaproject.dtos.BookRecordDto;
import com.nff.springiaproject.models.BookModel;
import com.nff.springiaproject.repositories.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final GeminiService geminiService;

    public List<BookModel> findAll() {
        return bookRepository.findAll();
    }

    public Optional<BookModel> findById(UUID id) {
        return bookRepository.findById(id);
    }

    public BookModel save(BookRecordDto bookRecordDto) {
        BookModel bookModel = new BookModel();
        BeanUtils.copyProperties(bookRecordDto, bookModel);
        bookModel.setReview(geminiService.generateReview(bookRecordDto.title()));
        bookModel.setRecommendations(geminiService.generateRecommendations(
                bookRecordDto.title(),
                bookRecordDto.author()
        ));
        return bookRepository.save(bookModel);
    }

    public BookModel update(UUID id, BookRecordDto bookRecordDto) {
        BookModel bookModel = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + id));

        BeanUtils.copyProperties(bookRecordDto, bookModel);
        return bookRepository.save(bookModel);
    }

    public void delete(UUID id) {
        bookRepository.deleteById(id);
    }
}
