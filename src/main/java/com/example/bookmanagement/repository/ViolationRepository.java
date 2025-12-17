package com.example.bookmanagement.repository;

import com.example.bookmanagement.model.Violation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ViolationRepository extends JpaRepository<Violation, Long> {
    List<Violation> findByUserId(Long userId);
}