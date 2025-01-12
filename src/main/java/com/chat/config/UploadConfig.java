package com.chat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "upload.avatar")
public class UploadConfig {
    private long maxSize;
    private String allowedTypes;
    private int width;
    private int height;
    private String path;
} 