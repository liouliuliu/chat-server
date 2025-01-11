package com.chat.dto;

import lombok.Data;

@Data
public class UserSearchResponse {
    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String friendshipStatus; // null/pending/accepted/blocked
    private Long requestId;
} 