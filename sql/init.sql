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

-- 消息表
CREATE TABLE messages (
                          message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          type VARCHAR(20) NOT NULL COMMENT '消息类型：PRIVATE_MSG-私聊消息，GROUP_MSG-群聊消息，SYSTEM_MSG-系统消息',
                          from_user_id BIGINT COMMENT '发送者ID',
                          to_user_id BIGINT COMMENT '接收者ID（私聊消息）',
                          group_id BIGINT COMMENT '群组ID（群聊消息）',
                          content TEXT NOT NULL COMMENT '消息内容',
                          status VARCHAR(20) NOT NULL DEFAULT 'UNSENT' COMMENT '消息状态：UNSENT-未发送，SENT-已发送，DELIVERED-已投递，READ-已读，FAILED-发送失败',
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          FOREIGN KEY (from_user_id) REFERENCES users(user_id),
                          FOREIGN KEY (to_user_id) REFERENCES users(user_id),
                          INDEX idx_from_user (from_user_id),
                          INDEX idx_to_user (to_user_id),
                          INDEX idx_group (group_id),
                          INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- 消息接收状态表（用于群聊消息的已读状态）
CREATE TABLE message_receipts (
                                  receipt_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  message_id BIGINT NOT NULL COMMENT '消息ID',
                                  user_id BIGINT NOT NULL COMMENT '接收者ID',
                                  status VARCHAR(20) NOT NULL DEFAULT 'DELIVERED' COMMENT '接收状态：DELIVERED-已投递，READ-已读',
                                  read_at TIMESTAMP COMMENT '读取时间',
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  FOREIGN KEY (message_id) REFERENCES messages(message_id),
                                  FOREIGN KEY (user_id) REFERENCES users(user_id),
                                  UNIQUE KEY uk_message_user (message_id, user_id),
                                  INDEX idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息接收状态表';

-- 离线消息表（用于存储用户离线时的消息）
CREATE TABLE offline_messages (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  message_id BIGINT NOT NULL COMMENT '消息ID',
                                  user_id BIGINT NOT NULL COMMENT '接收者ID',
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  FOREIGN KEY (message_id) REFERENCES messages(message_id),
                                  FOREIGN KEY (user_id) REFERENCES users(user_id),
                                  INDEX idx_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='离线消息表';

ALTER TABLE users
    ADD COLUMN gender VARCHAR(10) DEFAULT NULL COMMENT '性别: MALE/FEMALE'; 