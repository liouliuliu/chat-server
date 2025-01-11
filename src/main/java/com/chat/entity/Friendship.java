package com.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("friendships")
public class Friendship {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId1;
    private Long userId2;
    private String status; // pending/accepted/blocked
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
} 