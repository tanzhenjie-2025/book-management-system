package com.example.bookmanagement.service;

import com.example.bookmanagement.config.JwtService;
import com.example.bookmanagement.dto.LoginRequest;
import com.example.bookmanagement.dto.LoginResponse;
import com.example.bookmanagement.model.User;
import com.example.bookmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 用户服务（整合登录、注册、用户管理全逻辑，适配自定义DTO）
 */
@Service
@RequiredArgsConstructor
@Slf4j // 新增日志注解
public class UserService {
    // 核心依赖注入（确保Spring能正常注入）
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    // 优化：BCrypt密码前缀正则（匹配$2a$/$2b$/$2y$）
    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d+\\$.+$");
    // 优化：角色常量，避免硬编码
    private static final String DEFAULT_ROLE = "ROLE_USER";

    /**
     * 登录接口核心逻辑（返回含ID的LoginResponse）
     */
    public LoginResponse login(LoginRequest request) {
        try {
            // 1. 用户名密码认证（Security核心，失败会抛AuthenticationException）
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // 2. 安全转换User实体（避免ClassCastException）
            User user;
            if (authentication.getPrincipal() instanceof User) {
                user = (User) authentication.getPrincipal();
            } else {
                throw new AuthenticationServiceException("用户信息格式错误：" + request.getUsername());
            }

            // 3. 生成JWT Token
            String token = jwtService.generateToken(user);
            log.info("用户登录成功：username={}, userId={}", user.getUsername(), user.getId());

            // 4. 封装返回结果
            LoginResponse response = new LoginResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setRole(user.getRole());
            response.setToken(token);

            return response;
        } catch (Exception e) {
            log.error("用户登录失败：username={}, 原因={}", request.getUsername(), e.getMessage());
            throw e; // 抛出异常，由全局异常处理器处理
        }
    }

    /**
     * 注册用户（含用户名重复校验+密码加密）
     */
    public User register(User user) {
        // 校验用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            log.warn("注册失败：用户名已存在，username={}", user.getUsername());
            throw new RuntimeException("用户名已存在：" + user.getUsername());
        }

        // 密码加密（BCrypt）
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        log.info("用户注册：用户名={}，密码已加密", user.getUsername());

        // 默认值填充（避免空指针）
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole(DEFAULT_ROLE);
        }
        user.setEnabled(true);
        user.setViolationCount(0);

        // 保存用户到数据库
        User savedUser = userRepository.save(user);
        log.info("用户注册成功：userId={}, username={}, role={}", savedUser.getId(), savedUser.getUsername(), savedUser.getRole());
        return savedUser;
    }

    /**
     * 获取所有用户（管理员权限）
     */
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        log.info("查询所有用户，总数={}", users.size());
        return users;
    }

    /**
     * 按ID更新用户（含密码加密校验）
     */
    public User updateUser(User user) {
        // 校验用户是否存在
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> {
                    log.warn("更新用户失败：用户不存在，userId={}", user.getId());
                    return new RuntimeException("用户不存在：" + user.getId());
                });

        // 优化：BCrypt密码前缀判断（兼容所有BCrypt版本）
        if (user.getPassword() != null && !BCRYPT_PATTERN.matcher(user.getPassword()).matches()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            log.info("更新用户密码：userId={}", user.getId());
        }

        // 更新其他字段
        existingUser.setUsername(user.getUsername());
        existingUser.setRole(user.getRole());
        existingUser.setEnabled(user.isEnabled());
        existingUser.setViolationCount(user.getViolationCount());

        // 保存更新
        User updatedUser = userRepository.save(existingUser);
        log.info("更新用户成功：userId={}, username={}", updatedUser.getId(), updatedUser.getUsername());
        return updatedUser;
    }

    /**
     * 按ID获取用户
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("查询用户失败：用户不存在，userId={}", id);
                    return new RuntimeException("用户不存在：" + id);
                });
    }

    /**
     * 按用户名获取用户（登录/权限校验用）
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("查询用户失败：用户不存在，username={}", username);
                    return new UsernameNotFoundException("用户不存在：" + username);
                });
    }

    /**
     * 增加违规次数（达到3次自动禁用）
     */
    public void increaseViolationCount(Long userId) {
        User user = getUserById(userId);
        int newCount = user.getViolationCount() + 1;
        user.setViolationCount(newCount);

        // 违规次数≥3次，禁用账号
        if (newCount >= 3) {
            user.setEnabled(false);
            log.warn("用户违规次数达到3次，已禁用：userId={}, username={}", userId, user.getUsername());
        }

        userRepository.save(user);
        log.info("用户违规次数+1：userId={}, 当前次数={}", userId, newCount);
    }

    /**
     * 重置用户违规次数
     */
    public void resetViolationCount(Long userId) {
        User user = getUserById(userId);
        user.setViolationCount(0);
        userRepository.save(user);
        log.info("重置用户违规次数：userId={}", userId);
    }

    /**
     * 切换用户启用/禁用状态
     */
    public void toggleUserStatus(Long userId, boolean enabled) {
        User user = getUserById(userId);
        user.setEnabled(enabled);
        userRepository.save(user);
        log.info("切换用户状态：userId={}, enabled={}", userId, enabled);
    }
}