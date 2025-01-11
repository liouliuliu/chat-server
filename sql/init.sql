-- 创建数据库
CREATE DATABASE IF NOT EXISTS chat_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE chat_db;

-- 用户表
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    status VARCHAR(20) DEFAULT 'offline' COMMENT '在线状态：online/offline',
    last_login_time DATETIME COMMENT '最后登录时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 好友关系表
CREATE TABLE friendships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id1 BIGINT NOT NULL COMMENT '用户1',
    user_id2 BIGINT NOT NULL COMMENT '用户2',
    status VARCHAR(20) NOT NULL COMMENT '状态：pending/accepted/blocked',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id1) REFERENCES users(user_id),
    FOREIGN KEY (user_id2) REFERENCES users(user_id),
    UNIQUE KEY unique_friendship (user_id1, user_id2)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表'; 