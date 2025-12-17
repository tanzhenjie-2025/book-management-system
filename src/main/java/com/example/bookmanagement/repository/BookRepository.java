package com.example.bookmanagement.repository;

import com.example.bookmanagement.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByNameAndAuthor(String name, String author);
}