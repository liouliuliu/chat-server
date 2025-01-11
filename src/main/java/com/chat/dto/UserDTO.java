package com.chat.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String status;
} 