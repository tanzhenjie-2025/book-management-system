package com.example.bookmanagement.controller;

import com.example.bookmanagement.dto.CommentDTO;
import com.example.bookmanagement.model.BookComment;
import com.example.bookmanagement.service.BookCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class BookCommentController {
    private final BookCommentService bookCommentService;

    // 根据书籍ID获取评价（补充异常处理）
    @GetMapping("/book/{bookId}")
    public ResponseEntity<Map<String, Object>> getCommentsByBookId(@PathVariable Long bookId) {
        try {
            List<BookComment> comments = bookCommentService.getCommentsByBookId(bookId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "获取评价成功",
                    "data", comments
            ));
        } catch (Exception e) {
            log.error("获取书籍评价失败, bookId: {}", bookId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "获取评价失败：" + e.getMessage()));
        }
    }

    // 添加书籍评价（补充异常处理）
    @PostMapping
    public ResponseEntity<Map<String, Object>> addComment(@RequestBody CommentDTO commentDTO) {
        try {
            Map<String, Object> result = bookCommentService.addComment(commentDTO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("添加书籍评价失败, 参数: {}", commentDTO, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "提交评价失败：" + e.getMessage()));
        }
    }

    // 审核评价（管理员）
    @PutMapping("/audit/{commentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> auditComment(
            @PathVariable Long commentId,
            @RequestParam boolean pass) {
        try {
            Map<String, Object> result = bookCommentService.auditComment(commentId, pass);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("审核评价失败, commentId: {}, pass: {}", commentId, pass, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "审核评价失败：" + e.getMessage()));
        }
    }

    // 删除评价（管理员）
    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable Long commentId) {
        try {
            Map<String, Object> result = bookCommentService.deleteComment(commentId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("删除评价失败, commentId: {}", commentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "删除评价失败：" + e.getMessage()));
        }
    }
}