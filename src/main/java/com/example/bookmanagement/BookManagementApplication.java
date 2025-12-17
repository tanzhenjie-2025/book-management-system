package com.example.bookmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

// 确保扫描范围覆盖config包（默认@SpringBootApplication已覆盖同级别及子包）
@SpringBootApplication
// 如需显式指定，添加以下配置（可选）
// @ComponentScan(basePackages = "com.example.bookmanagement")
public class BookManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookManagementApplication.class, args);
    }
}