package com.example.bookmanagement.service;

import com.example.bookmanagement.model.OperationLog;
import com.example.bookmanagement.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogRepository logRepository;

    @Transactional
    public void save(OperationLog log) {
        logRepository.save(log);
    }

    public List<OperationLog> getAllLogs() {
        return logRepository.findAllByOrderByOperateTimeDesc();
    }

    public List<OperationLog> getByUsername(String username) {
        return logRepository.findByUsername(username);
    }
}