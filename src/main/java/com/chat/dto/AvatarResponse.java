package com.chat.dto;

import lombok.Data;

@Data
public class AvatarResponse {
    private String avatarUrl;

    public AvatarResponse(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
} 