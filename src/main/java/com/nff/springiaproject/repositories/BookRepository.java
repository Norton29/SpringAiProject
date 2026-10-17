package com.nff.springiaproject.repositories;

import com.nff.springiaproject.models.BookModel;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<BookModel, UUID> {
}
