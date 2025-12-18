// filePath: book-management-system/src/main/java/com/example/bookmanagement/config/SchedulerConfig.java
package com.example.bookmanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    // 启用Spring的定时任务支持
}