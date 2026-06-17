package com.example.bookmanagement.service;

import com.example.bookmanagement.model.BorrowRecord;
import com.example.bookmanagement.model.Violation;
import com.example.bookmanagement.repository.BorrowRecordRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BorrowRecordService {
    private static final Logger log = LoggerFactory.getLogger(BorrowRecordService.class);

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookService bookService;
    private final ViolationService violationService;
    private final UserService userService;

    public List<BorrowRecord> getBorrowsByUserId(Long userId) {
        List<BorrowRecord> records = borrowRecordRepository.findByUserId(userId);
        records.forEach(record -> log.info("借阅记录ID:{}，bookId:{}，isReturned:{}",
                record.getId(), record.getBookId(), record.isReturned()));
        return records;
    }

    public List<BorrowRecord> getCurrentBorrowsByUserId(Long userId) {
        return borrowRecordRepository.findByUserIdAndIsReturnedFalse(userId);
    }

    public Map<String, Object> borrowBook(Long userId, Long bookId) {
        Map<String, Object> result = new HashMap<>();
        try {
            bookService.decreaseStock(bookId);
            BorrowRecord record = new BorrowRecord();
            record.setUserId(userId);
            record.setBookId(bookId);
            record.setBorrowTime(LocalDate.now());
            record.setReturned(false);
            borrowRecordRepository.save(record);
            bookService.increaseBorrowCount(bookId);
            result.put("success", true);
            result.put("message", "借阅成功");
            result.put("record", record);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> returnBook(Long recordId) {
        Map<String, Object> result = new HashMap<>();
        try {
            BorrowRecord record = borrowRecordRepository.findById(recordId)
                    .orElseThrow(() -> new RuntimeException("借阅记录不存在"));
            if (record.isReturned()) {
                throw new RuntimeException("该书籍已归还");
            }
            LocalDate borrowDate = record.getBorrowTime();
            LocalDate returnDate = LocalDate.now();
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(borrowDate, returnDate);
            int overdueDays = daysBetween > 7 ? (int) (daysBetween - 7) : 0;

            record.setReturned(true);
            record.setReturnTime(returnDate);
            borrowRecordRepository.save(record);

            bookService.increaseStock(record.getBookId());

            if (overdueDays > 0) {
                Violation violation = new Violation();
                violation.setUserId(record.getUserId());
                violation.setBookId(record.getBookId());
                violation.setViolationDate(returnDate);
                violation.setReason("逾期归还");
                violation.setOverdueDays(overdueDays);
                violationService.addViolation(violation);
                userService.increaseViolationCount(record.getUserId());
                result.put("overdue", true);
                result.put("overdueDays", overdueDays);
                result.put("violation", true);
            }
            result.put("success", true);
            result.put("message", overdueDays > 0 ? "归还成功（逾期" + overdueDays + "天）" : "归还成功");
            result.put("record", record);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    // 普通续借
    public Map<String, Object> renewBook(Long recordId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            BorrowRecord record = borrowRecordRepository.findById(recordId)
                    .orElseThrow(() -> new RuntimeException("借阅记录不存在"));
            if (!record.getUserId().equals(userId)) {
                throw new RuntimeException("无权操作此记录");
            }
            if (record.isReturned()) {
                throw new RuntimeException("该书籍已归还，无法续借");
            }
            if (record.getRenewCount() >= 1) {
                throw new RuntimeException("该书籍已续借过一次，无法再次续借");
            }
            LocalDate borrowDate = record.getBorrowTime();
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(borrowDate, LocalDate.now());
            if (daysBetween > 7) {
                throw new RuntimeException("已逾期，无法续借");
            }
            record.setBorrowTime(LocalDate.now());
            record.setRenewCount(record.getRenewCount() + 1);
            borrowRecordRepository.save(record);
            result.put("success", true);
            result.put("message", "续借成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    // 管理员归还（直接复用 returnBook，无权限限制）
    public Map<String, Object> adminReturnBook(Long recordId) {
        return returnBook(recordId);
    }

    // 管理员续借（可强制续借，忽略逾期和次数限制？为安全还是限制1次）
    public Map<String, Object> adminRenewBook(Long recordId) {
        Map<String, Object> result = new HashMap<>();
        try {
            BorrowRecord record = borrowRecordRepository.findById(recordId)
                    .orElseThrow(() -> new RuntimeException("借阅记录不存在"));
            if (record.isReturned()) {
                throw new RuntimeException("该书籍已归还，无法续借");
            }
            if (record.getRenewCount() >= 1) {
                throw new RuntimeException("该书籍已续借过一次，无法再次续借");
            }
            // 管理员可强制续借，忽略逾期检查
            record.setBorrowTime(LocalDate.now());
            record.setRenewCount(record.getRenewCount() + 1);
            borrowRecordRepository.save(record);
            result.put("success", true);
            result.put("message", "续借成功（管理员操作）");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    public List<BorrowRecord> getAllBorrowRecords() {
        return borrowRecordRepository.findAll();
    }
}