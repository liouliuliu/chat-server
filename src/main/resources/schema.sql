-- 用户表
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    avatar_url VARCHAR(255),
    status VARCHAR(20) DEFAULT 'offline',
    last_login_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_username UNIQUE (username)
);

-- 好友关系表
CREATE TABLE IF NOT EXISTS friendships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id1 BIGINT NOT NULL,
    user_id2 BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user1 FOREIGN KEY (user_id1) REFERENCES users(user_id),
    CONSTRAINT fk_user2 FOREIGN KEY (user_id2) REFERENCES users(user_id),
    CONSTRAINT uk_friendship UNIQUE (user_id1, user_id2)
);

-- 消息表
CREATE TABLE IF NOT EXISTS messages (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(20) NOT NULL COMMENT '消息类型：PRIVATE_MSG-私聊消息，GROUP_MSG-群聊消息，SYSTEM_MSG-系统消息',
    from_user_id BIGINT COMMENT '发送者ID',
    to_user_id BIGINT COMMENT '接收者ID（私聊消息）',
    group_id BIGINT COMMENT '群组ID（群聊消息）',
    content TEXT NOT NULL COMMENT '消息内容',
    status VARCHAR(20) NOT NULL DEFAULT 'UNSENT' COMMENT '消息状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_from_user FOREIGN KEY (from_user_id) REFERENCES users(user_id),
    CONSTRAINT fk_message_to_user FOREIGN KEY (to_user_id) REFERENCES users(user_id)
);

-- 消息接收状态表
CREATE TABLE IF NOT EXISTS message_receipts (
    receipt_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DELIVERED',
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_receipt_message FOREIGN KEY (message_id) REFERENCES messages(message_id),
    CONSTRAINT fk_receipt_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT uk_message_user UNIQUE (message_id, user_id)
);

-- 离线消息表
CREATE TABLE IF NOT EXISTS offline_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_offline_message FOREIGN KEY (message_id) REFERENCES messages(message_id),
    CONSTRAINT fk_offline_user FOREIGN KEY (user_id) REFERENCES users(user_id)
); 