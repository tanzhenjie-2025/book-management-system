package com.example.bookmanagement.controller;

import com.example.bookmanagement.dto.LoginRequest;
import com.example.bookmanagement.dto.LoginResponse;
import com.example.bookmanagement.model.User;
import com.example.bookmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 适配Context Path=/api：将@RequestMapping("/api")改为@RequestMapping("/")
 * 最终接口路径：Context Path(/api) + @RequestMapping("/") + /auth/login = /api/auth/login
 */
@RestController
@RequestMapping("/") // 核心修改：从/api改为/
@RequiredArgsConstructor
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    // 登录（最终路径：/api/auth/login，和前端请求一致）
    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // 新增日志，验证请求到达Controller
        log.info("收到登录请求：用户名={}", request.getUsername());
        return ResponseEntity.ok(userService.login(request));
    }

    // 注册（最终路径：/api/auth/register）
    @PostMapping("/auth/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(userService.register(user));
    }

    // 获取所有用户（管理员，最终路径：/api/users）
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // 按ID获取用户（最终路径：/api/users/{id}）
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // 更新用户（最终路径：/api/users）
    @PutMapping("/users")
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(user));
    }

    // 增加违规次数（最终路径：/api/users/{id}/violation）
    @PutMapping("/users/{id}/violation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> increaseViolationCount(@PathVariable Long id) {
        userService.increaseViolationCount(id);
        return ResponseEntity.ok().build();
    }

    // 重置违规次数（最终路径：/api/users/{id}/violation/reset）
    @PutMapping("/users/{id}/violation/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> resetViolationCount(@PathVariable Long id) {
        userService.resetViolationCount(id);
        return ResponseEntity.ok().build();
    }

    // 切换用户状态（最终路径：/api/users/{id}/status）
    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request) {
        userService.toggleUserStatus(id, request.get("enabled"));
        return ResponseEntity.ok().build();
    }
}