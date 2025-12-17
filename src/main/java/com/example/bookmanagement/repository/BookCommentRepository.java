package com.example.bookmanagement.repository;

import com.example.bookmanagement.model.BookComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookCommentRepository extends JpaRepository<BookComment, Long> {
    // 根据书籍ID查询已审核的评价（原有）
    List<BookComment> findByBookIdAndIsAuditTrueAndIsDeletedFalse(Long bookId);

    // 检查用户是否已评价过该书籍（原有）
    Optional<BookComment> findByUserIdAndBookIdAndIsDeletedFalse(Long userId, Long bookId);

    // 查询书籍的平均评分（原有）
    @Query("SELECT AVG(c.score) FROM BookComment c WHERE c.bookId = :bookId AND c.isAudit = true AND c.isDeleted = false")
    Double findAverageScoreByBookId(@Param("bookId") Long bookId);

    // 查询书籍的评价数量（原有）
    @Query("SELECT COUNT(c) FROM BookComment c WHERE c.bookId = :bookId AND c.isAudit = true AND c.isDeleted = false")
    Long countByBookId(@Param("bookId") Long bookId);

    // 新增：管理员视角 - 某本书所有未删除评价（含未审核）
    List<BookComment> findByBookIdAndIsDeletedFalse(Long bookId);

    // 新增：普通用户视角 - 已审核评价 + 自己的未审核评价
    @Query("SELECT bc FROM BookComment bc WHERE bc.bookId = :bookId AND bc.isDeleted = false AND " +
            "(bc.isAudit = true OR (bc.isAudit = false AND bc.userId = :userId))")
    List<BookComment> findVisibleCommentsForUser(@Param("bookId") Long bookId, @Param("userId") Long userId);
}