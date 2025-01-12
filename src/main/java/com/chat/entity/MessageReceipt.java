package com.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chat.constant.MessageStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message_receipts")
public class MessageReceipt {
    @TableId(type = IdType.AUTO)
    private Long receiptId;
    
    private Long messageId;
    private Long userId;
    private MessageStatus status;
    private LocalDateTime readAt;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
} 