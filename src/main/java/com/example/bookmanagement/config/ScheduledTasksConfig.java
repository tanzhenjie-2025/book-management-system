package com.example.bookmanagement.config;

import com.example.bookmanagement.model.User;
import com.example.bookmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 定时任务配置类：检查用户违规次数并处理
 */
@Component
@RequiredArgsConstructor
public class ScheduledTasksConfig {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasksConfig.class);
    private final UserService userService;

    /**
     * 每天凌晨2点执行检查
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "*/10 * * * * *")
    public void checkUserViolations() {
        log.info("开始执行用户违规次数检查任务");

        List<User> allUsers = userService.getAllUsers();
        int disabledCount = 0;

        for (User user : allUsers) {
            // 检查违规次数是否达到3次且用户当前是启用状态
            if (user.getViolationCount() >= 3 && user.isEnabled()) {
                userService.toggleUserStatus(user.getId(), false);
                log.warn("用户违规次数达到3次，已自动禁用：userId={}, username={}, 违规次数={}",
                        user.getId(), user.getUsername(), user.getViolationCount());
                disabledCount++;
            }
        }

        log.info("用户违规次数检查任务完成，共禁用{}名用户", disabledCount);
    }
}