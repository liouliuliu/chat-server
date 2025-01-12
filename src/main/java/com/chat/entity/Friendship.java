package com.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chat.constant.FriendshipStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("friendships")
public class Friendship {
    @TableId(type = IdType.AUTO)
    private Long friendshipId;
    private Long userId;
    private Long friendId;
    private FriendshipStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 