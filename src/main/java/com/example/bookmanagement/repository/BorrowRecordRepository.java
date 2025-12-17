package com.example.bookmanagement.repository;

import com.example.bookmanagement.model.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    List<BorrowRecord> findByUserId(Long userId);
    List<BorrowRecord> findByBookId(Long bookId);
    List<BorrowRecord> findByUserIdAndIsReturnedFalse(Long userId);
    BorrowRecord findByIdAndUserId(Long id, Long userId);
}