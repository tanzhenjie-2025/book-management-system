// filePath: book-management-system/src/main/java/com/example/bookmanagement/service/OverdueCheckService.java
package com.example.bookmanagement.service;

import com.example.bookmanagement.model.BorrowRecord;
import com.example.bookmanagement.model.Violation;
import com.example.bookmanagement.repository.BorrowRecordRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OverdueCheckService {
    private static final Logger log = LoggerFactory.getLogger(OverdueCheckService.class);

    private final BorrowRecordRepository borrowRecordRepository;
    private final UserService userService;
    private final ViolationService violationService;

    // 每分钟执行一次检查
    @Scheduled(cron = "*/10 * * * * *")
    @Transactional
    public void checkOverdueBooks() {
        log.info("开始检查逾期书籍...");

        LocalDate today = LocalDate.now();
        // 查询所有未归还且已逾期的借阅记录（借阅超过7天）
        List<BorrowRecord> overdueRecords = borrowRecordRepository.findByIsReturnedFalseAndBorrowTimeBefore(
                today.minusDays(7)
        );

        if (overdueRecords.isEmpty()) {
            log.info("未发现逾期书籍");
            return;
        }

        log.info("发现 {} 条逾期记录，开始处理", overdueRecords.size());

        for (BorrowRecord record : overdueRecords) {
            try {
                // 计算逾期天数
                long overdueDays = LocalDate.now().toEpochDay() - record.getBorrowTime().toEpochDay() - 7;

                // 为用户添加违规记录
                Violation violation = new Violation();
                violation.setUserId(record.getUserId());
                violation.setBookId(record.getBookId());
                violation.setViolationDate(LocalDate.now());
                violation.setReason("书籍逾期未归还");
                violation.setOverdueDays((int) overdueDays);

                violationService.addViolation(violation);

                // 增加用户违规次数（达到3次会自动禁用）
                userService.increaseViolationCount(record.getUserId());

                log.info("处理逾期记录: 用户ID={}, 书籍ID={}, 逾期天数={}",
                        record.getUserId(), record.getBookId(), overdueDays);
            } catch (Exception e) {
                log.error("处理逾期记录失败: 记录ID={}, 错误信息={}", record.getId(), e.getMessage());
            }
        }

        log.info("逾期书籍检查处理完成");
    }
}