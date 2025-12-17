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
@RequestMapping("/borrows") // 核心修改：去掉/api前缀（Context Path已包含/api）
@RequiredArgsConstructor
public class BorrowRecordController {
    private final BorrowRecordService borrowRecordService;

    // 按用户ID获取借阅记录（最终路径：/api/borrows/user/{userId}）
    @GetMapping("/user/{userId}")
    public List<BorrowRecord> getBorrowsByUserId(@PathVariable Long userId) {
        return borrowRecordService.getBorrowsByUserId(userId);
    }

    // 按用户ID获取未归还的借阅记录（最终路径：/api/borrows/user/{userId}/current）
    @GetMapping("/user/{userId}/current")
    public List<BorrowRecord> getCurrentBorrowsByUserId(@PathVariable Long userId) {
        return borrowRecordService.getCurrentBorrowsByUserId(userId);
    }

    // 获取所有借阅记录（管理员权限，最终路径：/api/borrows）
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // 管理员才能查看所有记录
    public List<BorrowRecord> getAllBorrowRecords() {
        return borrowRecordService.getAllBorrowRecords();
    }

    // 借阅书籍（登录用户即可，最终路径：/api/borrows）
    @PostMapping
    public ResponseEntity<Map<String, Object>> borrowBook(
            @RequestParam Long userId,
            @RequestParam Long bookId) {
        return ResponseEntity.ok(borrowRecordService.borrowBook(userId, bookId));
    }

    // 归还书籍（登录用户即可，最终路径：/api/borrows/return/{recordId}）
    @PutMapping("/return/{recordId}")
    public ResponseEntity<Map<String, Object>> returnBook(@PathVariable Long recordId) {
        return ResponseEntity.ok(borrowRecordService.returnBook(recordId));
    }
}