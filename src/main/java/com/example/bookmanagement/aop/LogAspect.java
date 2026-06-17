package com.example.bookmanagement.aop;

import com.example.bookmanagement.annotation.LogOperation;
import com.example.bookmanagement.model.OperationLog;
import com.example.bookmanagement.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LogAspect {

    private final OperationLogService logService;

    @Around("@annotation(com.example.bookmanagement.annotation.LogOperation)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        String errorMsg = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            errorMsg = t.getMessage();
            throw t;
        } finally {
            long duration = System.currentTimeMillis() - start;
            saveLog(joinPoint, duration, errorMsg);
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, long duration, String errorMsg) {
        try {
            // 获取方法上的注解
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            LogOperation annotation = method.getAnnotation(LogOperation.class);
            if (annotation == null) return;

            // 当前用户
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (auth != null) ? auth.getName() : "anonymous";

            // 请求 IP
            HttpServletRequest request = null;
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                request = attributes.getRequest();
            }
            String ip = request != null ? request.getRemoteAddr() : "unknown";

            // 构建日志
            OperationLog logEntry = new OperationLog();
            logEntry.setUsername(username);
            logEntry.setOperation(annotation.value());
            logEntry.setDescription(annotation.description());  // 简单描述，可在注解里动态填入
            logEntry.setResult(errorMsg == null ? "SUCCESS" : "FAILURE");
            logEntry.setOperateTime(LocalDateTime.now());
            logEntry.setIp(ip);
            logEntry.setDuration(duration);

            // 如果操作失败，追加失败原因到描述
            if (errorMsg != null) {
                logEntry.setDescription(logEntry.getDescription() + " (失败原因: " + errorMsg + ")");
            }

            logService.save(logEntry);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }
}