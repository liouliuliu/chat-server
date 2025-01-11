package com.chat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.chat.mapper")
@EnableTransactionManagement
public class ChatServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatServerApplication.class, args);
    }
} 