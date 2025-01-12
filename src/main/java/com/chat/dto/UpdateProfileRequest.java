package com.chat.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String nickname;
    private String gender;
} 