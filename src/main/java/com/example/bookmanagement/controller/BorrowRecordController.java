package com.example.bookmanagement.controller;

import com.example.bookmanagement.model.BorrowRecord;
import com.example.bookmanagement.service.BorrowRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/borrows")
@RequiredArgsConstructor
public class BorrowRecordController {
    private final BorrowRecordService borrowRecordService;

    @GetMapping("/user/{userId}")
    public List<BorrowRecord> getBorrowsByUserId(@PathVariable Long userId) {
        return borrowRecordService.getBorrowsByUserId(userId);
    }

    @GetMapping("/user/{userId}/current")
    public List<BorrowRecord> getCurrentBorrowsByUserId(@PathVariable Long userId) {
        return borrowRecordService.getCurrentBorrowsByUserId(userId);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<BorrowRecord> getAllBorrowRecords() {
        return borrowRecordService.getAllBorrowRecords();
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> borrowBook(
            @RequestParam Long userId,
            @RequestParam Long bookId) {
        return ResponseEntity.ok(borrowRecordService.borrowBook(userId, bookId));
    }

    @PutMapping("/return/{recordId}")
    public ResponseEntity<Map<String, Object>> returnBook(@PathVariable Long recordId) {
        return ResponseEntity.ok(borrowRecordService.returnBook(recordId));
    }

    @PutMapping("/renew/{recordId}")
    public ResponseEntity<Map<String, Object>> renewBook(@PathVariable Long recordId, @RequestParam Long userId) {
        return ResponseEntity.ok(borrowRecordService.renewBook(recordId, userId));
    }

    // ========== 管理员专用 ==========
    @PutMapping("/admin/return/{recordId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> adminReturnBook(@PathVariable Long recordId) {
        return ResponseEntity.ok(borrowRecordService.adminReturnBook(recordId));
    }

    @PutMapping("/admin/renew/{recordId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> adminRenewBook(@PathVariable Long recordId) {
        return ResponseEntity.ok(borrowRecordService.adminRenewBook(recordId));
    }
}