package com.chat.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class FriendRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
} 