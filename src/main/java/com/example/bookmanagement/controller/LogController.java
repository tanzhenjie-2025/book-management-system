package com.example.bookmanagement.controller;

import com.example.bookmanagement.model.OperationLog;
import com.example.bookmanagement.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LogController {

    private final OperationLogService logService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OperationLog>> getAllLogs() {
        return ResponseEntity.ok(logService.getAllLogs());
    }

    @GetMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OperationLog>> getByUser(@PathVariable String username) {
        return ResponseEntity.ok(logService.getByUsername(username));
    }
}