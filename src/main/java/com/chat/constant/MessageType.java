package com.chat.constant;

public enum MessageType {
    CONNECT("CONNECT", "连接消息"),
    PRIVATE_MSG("PRIVATE_MSG", "私聊消息"),
    GROUP_MSG("GROUP_MSG", "群聊消息"),
    SYSTEM_MSG("SYSTEM_MSG", "系统消息");

    private final String code;
    private final String desc;

    MessageType(String code, String desc) {
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