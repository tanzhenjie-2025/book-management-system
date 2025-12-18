package com.example.bookmanagement.service;

import com.example.bookmanagement.dto.CommentDTO;
import com.example.bookmanagement.model.Book;
import com.example.bookmanagement.model.BookComment;
import com.example.bookmanagement.model.User;
import com.example.bookmanagement.repository.BookCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime; // 新增：导入LocalDateTime
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class BookCommentService {
    private final BookCommentRepository bookCommentRepository;
    private final BookService bookService;
    private final UserService userService;

    /**
     * 优化：根据书籍ID + 当前登录用户获取可见评价
     * - 管理员：所有未删除评价（含未审核）
     * - 普通用户：已审核评价 + 自己的未审核评价
     */
    public List<BookComment> getCommentsByBookId(Long bookId) {
        // 验证书籍是否存在
        bookService.getBookById(bookId);

        // 获取当前登录用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); // 从Security上下文获取当前用户名
        User currentUser = userService.getUserByUsername(username);

        // 管理员视角：返回所有未删除评价
        if ("ROLE_ADMIN".equals(currentUser.getRole())) {
            return bookCommentRepository.findByBookIdAndIsDeletedFalse(bookId);
        }

        // 普通用户视角：已审核评价 + 自己的未审核评价
        return bookCommentRepository.findVisibleCommentsForUser(bookId, currentUser.getId());
    }

    /**
     * 新增：管理员获取待审核评论（可按书籍ID筛选）
     */
    public List<BookComment> getPendingComments(Long bookId) {
        if (bookId != null) {
            // 查指定书籍的未审核、未删除评论
            return bookCommentRepository.findByBookIdAndIsAuditFalseAndIsDeletedFalse(bookId);
        } else {
            // 查所有未审核、未删除评论
            return bookCommentRepository.findByIsAuditFalseAndIsDeletedFalse();
        }
    }

    /**
     * 添加书籍评价（原有逻辑优化，补充时间字段）
     */
    @Transactional
    public Map<String, Object> addComment(CommentDTO commentDTO) {
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = commentDTO.getUserId();
            Long bookId = commentDTO.getBookId();
            Integer score = commentDTO.getScore();

            // 验证用户是否存在
            User user = userService.getUserById(userId);

            // 验证书籍是否存在
            Book book = bookService.getBookById(bookId);

            // 验证评分是否有效
            if (score == null || score < 1 || score > 5) {
                throw new RuntimeException("评分必须在1-5之间");
            }

            // 检查用户是否已评价过该书籍
            if (bookCommentRepository.findByUserIdAndBookIdAndIsDeletedFalse(userId, bookId).isPresent()) {
                throw new RuntimeException("您已评价过该书籍，不能重复评价");
            }

            // 创建评价记录
            BookComment comment = new BookComment();
            comment.setUserId(userId);
            comment.setBookId(bookId);
            comment.setScore(score);
            comment.setContent(commentDTO.getContent());
            // 默认为未审核状态
            comment.setAudit(false);
            // 修复：使用LocalDateTime替代Date
            comment.setCreateTime(LocalDateTime.now());
            comment.setUpdateTime(LocalDateTime.now());
            comment.setDeleted(false);

            bookCommentRepository.save(comment);

            // 更新书籍的评分统计（仅统计已审核评价，审核通过时再更新）
            updateBookScoreStatistics(bookId);

            result.put("success", true);
            result.put("message", "评价提交成功，等待审核");
            result.put("comment", comment);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    /**
     * 更新书籍的评分统计信息（原有）
     */
    @Transactional
    public void updateBookScoreStatistics(Long bookId) {
        Double avgScore = bookCommentRepository.findAverageScoreByBookId(bookId);
        Long commentCount = bookCommentRepository.countByBookId(bookId);

        if (avgScore == null) {
            avgScore = 0.0;
        }

        // 更新书籍的平均评分和评价数量
        bookService.updateBookScores(bookId, avgScore, commentCount);
    }

    /**
     * 审核评价（管理员功能）（原有）
     */
    @Transactional
    public Map<String, Object> auditComment(Long commentId, boolean pass) {
        Map<String, Object> result = new HashMap<>();

        try {
            BookComment comment = bookCommentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("评价不存在"));

            comment.setAudit(pass);
            // 修复：使用LocalDateTime替代Date
            comment.setUpdateTime(LocalDateTime.now());
            bookCommentRepository.save(comment);

            // 如果审核通过，更新书籍评分统计
            if (pass) {
                updateBookScoreStatistics(comment.getBookId());
            }

            result.put("success", true);
            result.put("message", pass ? "评价审核通过" : "评价审核未通过");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    /**
     * 删除评价（管理员功能）（原有）
     */
    @Transactional
    public Map<String, Object> deleteComment(Long commentId) {
        Map<String, Object> result = new HashMap<>();

        try {
            BookComment comment = bookCommentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("评价不存在"));

            // 逻辑删除
            comment.setDeleted(true);
            // 修复：使用LocalDateTime替代Date
            comment.setUpdateTime(LocalDateTime.now());
            bookCommentRepository.save(comment);

            // 更新书籍评分统计
            updateBookScoreStatistics(comment.getBookId());

            result.put("success", true);
            result.put("message", "评价删除成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }
}