package com.example.bookmanagement.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private Long id;
    private String username;
    private String role;
    private String token;
}