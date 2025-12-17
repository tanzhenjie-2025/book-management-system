package com.example.bookmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 全局跨域配置（解决前端5173端口跨域问题，适配Spring 6.x）
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 核心修正：改用addAllowedOriginPattern（兼容Spring 6.x + 允许凭证）
        config.addAllowedOriginPattern("http://localhost:5173"); // 前端端口
        config.addAllowedOriginPattern("*"); // 兜底（开发环境）

        // 允许所有请求方法（包含OPTIONS）
        config.addAllowedMethod("*");

        // 允许所有请求头
        config.addAllowedHeader("*");

        // 允许携带凭证（Cookie/JWT）
        config.setAllowCredentials(true);

        // 预检请求有效期（秒）
        config.setMaxAge(3600L);

        // 暴露响应头
        config.addExposedHeader("Authorization");
        config.addExposedHeader("token");

        // 对所有路径生效
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}