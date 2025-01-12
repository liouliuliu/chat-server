package com.chat.constant;

public enum MessageStatus {
    UNSENT("UNSENT", "未发送"),
    SENT("SENT", "已发送"),
    DELIVERED("DELIVERED", "已投递"),
    READ("READ", "已读"),
    FAILED("FAILED", "发送失败");

    private final String code;
    private final String desc;

    MessageStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
} 