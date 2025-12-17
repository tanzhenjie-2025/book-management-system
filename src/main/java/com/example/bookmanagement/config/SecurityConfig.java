package com.example.bookmanagement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 启用CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 关闭CSRF（前后端分离）
                .csrf(csrf -> csrf.disable())
                // 无状态Session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 权限配置（核心修复：路径匹配规则，**必须放在末尾）
                .authorizeHttpRequests(auth -> auth
                        // 放行预检请求、登录/注册、H2控制台
                        .requestMatchers("OPTIONS/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        // 书籍查询接口：匿名可访问
                        .requestMatchers("/books").permitAll()
                        // 评价接口：登录用户可访问（提交/查看），审核/删除由@PreAuthorize控制
                        .requestMatchers("/comments/**").hasAnyRole("USER", "ADMIN")
                        // 借阅/归还接口：登录用户可访问
                        .requestMatchers("/borrows/**").hasAnyRole("USER", "ADMIN")
                        // 书籍管理接口：仅管理员（修复路径：/** 替代 /**/*，避免解析错误）
                        .requestMatchers("/books/**").hasRole("ADMIN")
                        // 用户管理：仅管理员
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        // 其他所有接口需认证
                        .anyRequest().authenticated()
                )
                // 允许H2控制台frame嵌套
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
                // 设置认证提供者
                .authenticationProvider(authenticationProvider())
                // 添加JWT过滤器（在用户名密码过滤器之前）
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 跨域配置（兼容Spring 6.x，匹配前端5173端口）
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 允许前端域名（Vue默认5173端口）
        config.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        // 允许所有请求方法
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许所有请求头（包含Authorization）
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        // 允许携带Cookie/Token
        config.setAllowCredentials(true);
        // 预检请求有效期（1小时）
        config.setMaxAge(3600L);

        // 应用到所有接口（包含/api上下文路径）
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}