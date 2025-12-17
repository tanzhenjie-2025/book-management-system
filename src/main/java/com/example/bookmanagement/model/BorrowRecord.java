package com.example.bookmanagement.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "borrow_records")
public class BorrowRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long bookId;

    private LocalDate borrowTime;

    // 核心修改：指定JSON序列化字段名为returned，与前端统一
    @JsonProperty("returned")
    private boolean isReturned = false;

    private LocalDate returnTime;
}