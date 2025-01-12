package com.chat.model;

import com.chat.constant.MessageType;
import lombok.Data;

@Data
public class ChatMessage {
    private MessageType type;
    private Long fromUserId;
    private Long toUserId;
    private Long groupId;
    private String content;
    private Long timestamp;
} 