package com.ic.filemanage.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer{
        @Autowired
        private JwtInterceptor jwtInterceptor;

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(jwtInterceptor)
                    .addPathPatterns("/**")  // 拦截所有请求
                    .excludePathPatterns(
                            "/api/auth/login",           // 登录接口
                            "/doc.html",        // Knife4j主页
                            "/webjars/**",      // 静态资源
                            "/v3/api-docs/**",  // OpenAPI JSON数据
                            "/swagger-resources/**",  // Swagger资源
                            "/swagger-ui/**",   // Swagger UI页面
                            "/swagger-ui.html", // 原生Swagger UI
                            "/favicon.ico",     // 网站图标
                            "/error",           // 错误页面
                            "/actuator/**"      // Spring Boot监控端点
                    );
        }
}

