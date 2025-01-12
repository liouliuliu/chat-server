package com.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chat.constant.MessageType;
import com.chat.constant.MessageStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("messages")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long messageId;
    
    private MessageType type;
    private Long fromUserId;
    private Long toUserId;
    private Long groupId;
    private String content;
    private MessageStatus status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
} 