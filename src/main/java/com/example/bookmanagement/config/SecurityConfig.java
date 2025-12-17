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
import org.springframework.web.cors.CorsConfiguration; // 补充核心导入（解决无法解析CorsConfiguration）
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
                // 启用CORS（关联跨域配置）
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 关闭CSRF
                .csrf(csrf -> csrf.disable())
                // 无状态Session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 权限配置：放行/auth/**、OPTIONS、H2控制台（相对于Context Path=/api）
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("OPTIONS/**").permitAll() // 放行OPTIONS
                        .requestMatchers("/auth/**").permitAll() // 放行登录/注册
                        .requestMatchers("/h2-console/**").permitAll() // 放行H2
                        .anyRequest().authenticated() // 其他需认证
                )
                // 允许H2控制台frame嵌套
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
                // 认证提供者
                .authenticationProvider(authenticationProvider())
                // 添加JWT过滤器
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 跨域配置源（关联CorsConfig，修正所有方法解析错误）
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Spring 6.x 兼容方法（解决addAllowedOriginPattern解析错误）
        config.addAllowedOriginPattern("http://localhost:5173"); // 前端端口
        config.addAllowedMethod("*"); // 允许所有请求方法
        config.addAllowedHeader("*"); // 允许所有请求头
        config.setAllowCredentials(true); // 允许携带凭证
        config.setMaxAge(3600L); // 预检请求有效期

        // 注册跨域配置（解决registerCorsConfiguration参数错误）
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}