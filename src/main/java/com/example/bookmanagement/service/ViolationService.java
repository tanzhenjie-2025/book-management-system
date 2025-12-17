package com.example.bookmanagement.service;

import com.example.bookmanagement.model.Violation;
import com.example.bookmanagement.repository.ViolationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ViolationService {
    private final ViolationRepository violationRepository;

    // 添加违规记录
    public Violation addViolation(Violation violation) {
        return violationRepository.save(violation);
    }

    // 按用户ID获取违规记录
    public List<Violation> getViolationsByUserId(Long userId) {
        return violationRepository.findByUserId(userId);
    }

    // 获取所有违规记录
    public List<Violation> getAllViolations() {
        return violationRepository.findAll();
    }

    // 删除违规记录
    public void deleteViolation(Long id) {
        violationRepository.deleteById(id);
    }
}