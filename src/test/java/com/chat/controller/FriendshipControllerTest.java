package com.chat.controller;

import com.chat.BaseTest;
import com.chat.dto.FriendRequest;
import com.chat.dto.UserDTO;
import com.chat.dto.UserSearchResponse;
import com.chat.service.FriendshipService;
import com.chat.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class FriendshipControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FriendshipService friendshipService;

    @MockBean
    private JwtUtil jwtUtil;

    private final String TEST_TOKEN = "Bearer test.jwt.token";
    private final Long TEST_USER_ID = 1L;

    @Test
    void searchUser_Success() throws Exception {
        // Arrange
        String username = "testuser";
        UserSearchResponse response = new UserSearchResponse();
        response.setUserId(2L);
        response.setUsername(username);

        when(jwtUtil.parseToken("test.jwt.token")).thenReturn(io.jsonwebtoken.Jwts.claims().setSubject(TEST_USER_ID.toString()));
        when(friendshipService.searchUser(username, TEST_USER_ID)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/friends/search")
                .param("username", username)
                .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.username").value(username));
    }

    @Test
    void sendFriendRequest_Success() throws Exception {
        // Arrange
        FriendRequest request = new FriendRequest();
        request.setUsername("testuser");

        when(jwtUtil.parseToken("test.jwt.token")).thenReturn(io.jsonwebtoken.Jwts.claims().setSubject(TEST_USER_ID.toString()));

        // Act & Assert
        mockMvc.perform(post("/api/friends/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    void getPendingRequests_Success() throws Exception {
        // Arrange
        UserSearchResponse request = new UserSearchResponse();
        request.setUserId(2L);
        request.setUsername("testuser");

        when(jwtUtil.parseToken("test.jwt.token")).thenReturn(io.jsonwebtoken.Jwts.claims().setSubject(TEST_USER_ID.toString()));
        when(friendshipService.getPendingRequests(TEST_USER_ID)).thenReturn(Arrays.asList(request));

        // Act & Assert
        mockMvc.perform(get("/api/friends/requests")
                .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(2))
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    void handleFriendRequest_Accept_Success() throws Exception {
        // Arrange
        Long requestId = 1L;
        when(jwtUtil.parseToken("test.jwt.token")).thenReturn(io.jsonwebtoken.Jwts.claims().setSubject(TEST_USER_ID.toString()));

        // Act & Assert
        mockMvc.perform(post("/api/friends/requests/{requestId}/handle", requestId)
                .param("accept", "true")
                .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    void getFriendList_Success() throws Exception {
        // Arrange
        UserDTO friend = new UserDTO();
        friend.setUserId(2L);
        friend.setUsername("friend");

        when(jwtUtil.parseToken("test.jwt.token")).thenReturn(io.jsonwebtoken.Jwts.claims().setSubject(TEST_USER_ID.toString()));
        when(friendshipService.getFriendList(TEST_USER_ID)).thenReturn(Arrays.asList(friend));

        // Act & Assert
        mockMvc.perform(get("/api/friends/list")
                .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(2))
                .andExpect(jsonPath("$[0].username").value("friend"));
    }

    @Test
    void getFriendList_EmptyList() throws Exception {
        // Arrange
        when(jwtUtil.parseToken("test.jwt.token")).thenReturn(io.jsonwebtoken.Jwts.claims().setSubject(TEST_USER_ID.toString()));
        when(friendshipService.getFriendList(TEST_USER_ID)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/friends/list")
                .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
} 