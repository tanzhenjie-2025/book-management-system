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

    // 按用户ID获取借阅记录
    public List<BorrowRecord> getBorrowsByUserId(Long userId) {
        List<BorrowRecord> records = borrowRecordRepository.findByUserId(userId);
        // 日志：打印获取的借阅记录，确认bookId存在
        records.forEach(record -> log.info("借阅记录ID:{}，bookId:{}，isReturned:{}",
                record.getId(), record.getBookId(), record.isReturned()));
        return records;
    }

    // 按用户ID获取未归还的借阅记录
    public List<BorrowRecord> getCurrentBorrowsByUserId(Long userId) {
        return borrowRecordRepository.findByUserIdAndIsReturnedFalse(userId);
    }

    // 借阅书籍
    public Map<String, Object> borrowBook(Long userId, Long bookId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查书籍库存
            bookService.decreaseStock(bookId);

            // 创建借阅记录
            BorrowRecord record = new BorrowRecord();
            record.setUserId(userId);
            record.setBookId(bookId);
            record.setBorrowTime(LocalDate.now());
            record.setReturned(false); // 改用统一的setter（lombok生成）

            borrowRecordRepository.save(record);

            // 增加书籍借阅次数
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

    // 归还书籍
    public Map<String, Object> returnBook(Long recordId) {
        Map<String, Object> result = new HashMap<>();

        try {
            BorrowRecord record = borrowRecordRepository.findById(recordId)
                    .orElseThrow(() -> new RuntimeException("借阅记录不存在"));

            if (record.isReturned()) {
                throw new RuntimeException("该书籍已归还");
            }

            // 计算逾期天数
            LocalDate borrowDate = record.getBorrowTime();
            LocalDate returnDate = LocalDate.now();
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(borrowDate, returnDate);
            int overdueDays = daysBetween > 7 ? (int) (daysBetween - 7) : 0;

            // 更新借阅记录
            record.setReturned(true); // 改用统一的setter
            record.setReturnTime(returnDate);
            borrowRecordRepository.save(record);

            // 日志：确认归还后的记录信息
            log.info("归还记录ID:{}，bookId:{}，已归还:{}",
                    record.getId(), record.getBookId(), record.isReturned());

            // 增加书籍库存
            bookService.increaseStock(record.getBookId());

            // 如果逾期，记录违规
            if (overdueDays > 0) {
                Violation violation = new Violation();
                violation.setUserId(record.getUserId());
                violation.setBookId(record.getBookId());
                violation.setViolationDate(returnDate);
                violation.setReason("逾期归还");
                violation.setOverdueDays(overdueDays);

                violationService.addViolation(violation);

                // 增加用户违规次数
                userService.increaseViolationCount(record.getUserId());

                result.put("overdue", true);
                result.put("overdueDays", overdueDays);
                result.put("violation", true);
            }

            result.put("success", true);
            result.put("message", overdueDays > 0 ? "归还成功（逾期" + overdueDays + "天）" : "归还成功");
            result.put("record", record); // 返回完整的记录（包含bookId）
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    // 获取所有借阅记录
    public List<BorrowRecord> getAllBorrowRecords() {
        return borrowRecordRepository.findAll();
    }
}