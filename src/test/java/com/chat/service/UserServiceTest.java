package com.chat.service;

import com.chat.dto.LoginRequest;
import com.chat.dto.RegisterRequest;
import com.chat.entity.User;
import com.chat.mapper.UserMapper;
import com.chat.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_WithValidData_ShouldSucceed() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        
        when(userMapper.selectByUsername(request.getUsername())).thenReturn(null);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // Act
        User result = userService.register(request);

        // Assert
        assertNotNull(result);
        assertEquals(request.getUsername(), result.getUsername());
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void register_WithExistingUsername_ShouldThrowException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");

        User existingUser = new User();
        existingUser.setUsername(request.getUsername());
        
        when(userMapper.selectByUsername(request.getUsername())).thenReturn(existingUser);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.register(request));
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void login_WithValidCredentials_ShouldSucceed() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        User user = new User();
        user.setUserId(1L);
        user.setUsername(request.getUsername());
        user.setPassword("encodedPassword");

        when(userMapper.selectByUsername(request.getUsername())).thenReturn(user);
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);

        // Act
        User result = userService.login(request);

        // Assert
        assertNotNull(result);
        assertEquals(user.getUserId(), result.getUserId());
        assertEquals(user.getUsername(), result.getUsername());
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowException() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword("encodedPassword");

        when(userMapper.selectByUsername(request.getUsername())).thenReturn(user);
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.login(request));
    }

    @Test
    void getUserById_WithValidId_ShouldReturnUser() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setUserId(userId);
        user.setUsername("testuser");

        when(userMapper.selectById(userId)).thenReturn(user);

        // Act
        User result = userService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(user.getUsername(), result.getUsername());
    }

    @Test
    void getUserById_WithInvalidId_ShouldReturnNull() {
        // Arrange
        Long userId = 999L;
        when(userMapper.selectById(userId)).thenReturn(null);

        // Act
        User result = userService.getUserById(userId);

        // Assert
        assertNull(result);
    }

    @Test
    void updateUser_WithValidData_ShouldSucceed() {
        // Arrange
        User user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");
        user.setNickname("Test User");

        when(userMapper.updateById(user)).thenReturn(1);

        // Act
        userService.updateUser(user);

        // Assert
        verify(userMapper).updateById(user);
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        // Arrange
        User user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");

        String expectedToken = "valid.jwt.token";
        when(jwtUtil.generateToken(user.getUserId())).thenReturn(expectedToken);

        // Act
        String result = userService.generateToken(user);

        // Assert
        assertNotNull(result);
        assertEquals(expectedToken, result);
        verify(jwtUtil).generateToken(user.getUserId());
    }
} 