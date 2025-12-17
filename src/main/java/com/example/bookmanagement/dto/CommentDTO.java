package com.example.bookmanagement.dto;

import lombok.Data;

@Data
public class CommentDTO {
    private Long userId;
    private Long bookId;
    private Integer score;
    private String content;
}