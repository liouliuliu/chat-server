package com.chat.controller;

import com.chat.entity.Message;
import com.chat.service.MessageService;
import com.chat.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final JwtUtil jwtUtil;

    public MessageController(MessageService messageService, JwtUtil jwtUtil) {
        this.messageService = messageService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<Message>> getHistoryMessages(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token) {
        Long currentUserId = jwtUtil.getUserIdFromToken(token.substring(7));
        List<Message> messages = messageService.getHistoryMessages(currentUserId, userId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/offline")
    public ResponseEntity<List<Message>> getOfflineMessages(
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.substring(7));
        List<Message> messages = messageService.getOfflineMessages(userId);
        return ResponseEntity.ok(messages);
    }
} 