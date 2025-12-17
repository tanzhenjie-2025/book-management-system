package com.example.bookmanagement.controller;

import com.example.bookmanagement.model.BorrowRecord;
import com.example.bookmanagement.service.BorrowRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowRecordController {
    private final BorrowRecordService borrowRecordService;

    // 按用户ID获取借阅记录
    @GetMapping("/user/{userId}")
    public List<BorrowRecord> getBorrowsByUserId(@PathVariable Long userId) {
        return borrowRecordService.getBorrowsByUserId(userId);
    }

    // 按用户ID获取未归还的借阅记录
    @GetMapping("/user/{userId}/current")
    public List<BorrowRecord> getCurrentBorrowsByUserId(@PathVariable Long userId) {
        return borrowRecordService.getCurrentBorrowsByUserId(userId);
    }

    // 获取所有借阅记录（管理员）
    @GetMapping
    public List<BorrowRecord> getAllBorrowRecords() {
        return borrowRecordService.getAllBorrowRecords();
    }

    // 借阅书籍
    @PostMapping
    public ResponseEntity<Map<String, Object>> borrowBook(
            @RequestParam Long userId,
            @RequestParam Long bookId) {
        return ResponseEntity.ok(borrowRecordService.borrowBook(userId, bookId));
    }

    // 归还书籍
    @PutMapping("/return/{recordId}")
    public ResponseEntity<Map<String, Object>> returnBook(@PathVariable Long recordId) {
        return ResponseEntity.ok(borrowRecordService.returnBook(recordId));
    }
}