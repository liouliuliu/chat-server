package com.chat.constant;

/**
 * 好友关系状态枚举
 */
public enum FriendshipStatus {
    /**
     * 待处理 - 好友请求已发送但未处理
     */
    PENDING,
    
    /**
     * 已接受 - 好友关系已建立
     */
    ACTIVE,
    
    /**
     * 已拒绝 - 好友请求被拒绝
     */
    REJECTED,
    
    /**
     * 已屏蔽 - 好友被屏蔽
     */
    BLOCKED,
    
    /**
     * 已删除 - 好友关系已解除
     */
    DELETED
} 