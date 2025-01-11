package com.chat.util;

import com.chat.BaseTest;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest extends BaseTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void generateAndParseToken() {
        // Arrange
        Long userId = 1L;
        String username = "testuser";

        // Act
        String token = jwtUtil.generateToken(userId, username);
        Claims claims = jwtUtil.parseToken(token);

        // Assert
        assertNotNull(token);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(username, claims.get("username"));
    }

    @Test
    void tokenExpiration() throws InterruptedException {
        // Arrange
        String token = jwtUtil.generateToken(1L, "testuser");

        // Act & Assert
        Claims claims = jwtUtil.parseToken(token);
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().getTime() > System.currentTimeMillis());
    }
} 