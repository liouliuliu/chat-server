package com.chat.service;

import com.chat.BaseTest;
import com.chat.dto.LoginRequest;
import com.chat.dto.RegisterRequest;
import com.chat.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest extends BaseTest {

    @Autowired
    private UserService userService;

    @Test
    void register_Success() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setNickname("Test User");

        // Act
        User user = userService.register(request);

        // Assert
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("Test User", user.getNickname());
        assertNotNull(user.getPassword());
        assertEquals("offline", user.getStatus());
    }

    @Test
    void register_DuplicateUsername() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        // Act & Assert
        userService.register(request);
        assertThrows(RuntimeException.class, () -> userService.register(request));
    }

    @Test
    void login_Success() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");
        userService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        // Act
        String token = userService.login(loginRequest);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void login_WrongPassword() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");
        userService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.login(loginRequest));
    }
} 