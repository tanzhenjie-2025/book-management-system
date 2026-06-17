package com.example.bookmanagement.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "operation_logs")
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 操作人用户名 */
    private String username;

    /** 操作类型：ADD_BOOK, UPDATE_BOOK, DELETE_BOOK, SOFT_DELETE, RESTORE, UPDATE_STOCK, IMPORT, EXPORT */
    @Column(nullable = false)
    private String operation;

    /** 操作描述（书籍名称或ID） */
    private String description;

    /** 操作结果：SUCCESS / FAILURE */
    @Column(nullable = false)
    private String result;

    /** 操作时间 */
    @Column(nullable = false)
    private LocalDateTime operateTime;

    /** 操作 IP */
    private String ip;

    /** 执行耗时（毫秒） */
    private Long duration;
}