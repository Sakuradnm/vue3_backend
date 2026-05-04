package com.example.vue3_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射，将 /upload/** 映射到项目根目录下的 uploads/ 文件夹
        // 使用 file: 前缀指定文件系统路径
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(3600) // 缓存1小时
                .resourceChain(true);
    }
}
