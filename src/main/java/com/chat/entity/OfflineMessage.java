package com.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("offline_messages")
public class OfflineMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long messageId;
    private Long userId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
} 