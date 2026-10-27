package com.nff.springiaproject.book.adapter.output.persistence;

import java.util.UUID;

import com.nff.springiaproject.book.adapter.output.persistence.entities.BookJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataBookRepository extends JpaRepository<BookJpaEntity, UUID> {
}
