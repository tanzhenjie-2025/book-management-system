package com.example.bookmanagement.controller;

import com.example.bookmanagement.model.Violation;
import com.example.bookmanagement.service.ViolationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/violations")
@RequiredArgsConstructor
public class ViolationController {
    private final ViolationService violationService;

    // 按用户ID获取违规记录
    @GetMapping("/user/{userId}")
    public List<Violation> getViolationsByUserId(@PathVariable Long userId) {
        return violationService.getViolationsByUserId(userId);
    }

    // 获取所有违规记录（管理员）
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Violation> getAllViolations() {
        return violationService.getAllViolations();
    }

    // 添加违规记录（管理员）
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Violation> addViolation(@RequestBody Violation violation) {
        return ResponseEntity.ok(violationService.addViolation(violation));
    }

    // 删除违规记录（管理员）
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteViolation(@PathVariable Long id) {
        violationService.deleteViolation(id);
        return ResponseEntity.ok().build();
    }
}