package com.chat.controller;

import com.chat.BaseTest;
import com.chat.entity.Message;
import com.chat.service.MessageService;
import com.chat.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class MessageControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageService messageService;

    @MockBean
    private JwtUtil jwtUtil;

    private final String TEST_TOKEN = "Bearer test.jwt.token";
    private final Long TEST_USER_ID = 1L;

    @Test
    void getHistoryMessages_Success() throws Exception {
        // Arrange
        Long otherUserId = 2L;
        Message message = new Message();
        message.setMessageId(1L);
        message.setFromUserId(TEST_USER_ID);
        message.setToUserId(otherUserId);
        message.setContent("Test message");
        message.setCreatedAt(LocalDateTime.now());

        when(jwtUtil.getUserIdFromToken("test.jwt.token")).thenReturn(TEST_USER_ID);
        when(messageService.getHistoryMessages(TEST_USER_ID, otherUserId))
                .thenReturn(Arrays.asList(message));

        // Act & Assert
        mockMvc.perform(get("/api/messages/history/{userId}", otherUserId)
                .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].messageId").value(1))
                .andExpect(jsonPath("$[0].fromUserId").value(TEST_USER_ID))
                .andExpect(jsonPath("$[0].toUserId").value(otherUserId))
                .andExpect(jsonPath("$[0].content").value("Test message"));
    }

    @Test
    void getHistoryMessages_EmptyList() throws Exception {
        // Arrange
        Long otherUserId = 2L;
        when(jwtUtil.getUserIdFromToken("test.jwt.token")).thenReturn(TEST_USER_ID);
        when(messageService.getHistoryMessages(TEST_USER_ID, otherUserId))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/messages/history/{userId}", otherUserId)
                .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getOfflineMessages_Success() throws Exception {
        // Arrange
        Message message = new Message();
        message.setMessageId(1L);
        message.setFromUserId(2L);
        message.setToUserId(TEST_USER_ID);
        message.setContent("Offline message");
        message.setCreatedAt(LocalDateTime.now());

        when(jwtUtil.getUserIdFromToken("test.jwt.token")).thenReturn(TEST_USER_ID);
        when(messageService.getOfflineMessages(TEST_USER_ID))
                .thenReturn(Arrays.asList(message));

        // Act & Assert
        mockMvc.perform(get("/api/messages/offline")
                .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].messageId").value(1))
                .andExpect(jsonPath("$[0].fromUserId").value(2))
                .andExpect(jsonPath("$[0].toUserId").value(TEST_USER_ID))
                .andExpect(jsonPath("$[0].content").value("Offline message"));
    }

    @Test
    void getOfflineMessages_EmptyList() throws Exception {
        // Arrange
        when(jwtUtil.getUserIdFromToken("test.jwt.token")).thenReturn(TEST_USER_ID);
        when(messageService.getOfflineMessages(TEST_USER_ID))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/messages/offline")
                .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
} 