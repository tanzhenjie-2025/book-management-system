package com.example.bookmanagement.repository;

import com.example.bookmanagement.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByNameAndAuthor(String name, String author);

    // 更新书籍的平均评分和评价数量
    @Modifying
    @Query("UPDATE Book b SET b.avgScore = :avgScore, b.commentCount = :commentCount WHERE b.id = :bookId")
    void updateBookScores(@Param("bookId") Long bookId,
                          @Param("avgScore") Double avgScore,
                          @Param("commentCount") Long commentCount);
}