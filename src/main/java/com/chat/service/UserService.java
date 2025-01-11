package com.chat.service;

import com.chat.dto.LoginRequest;
import com.chat.dto.RegisterRequest;
import com.chat.entity.User;
import com.chat.mapper.UserMapper;
import com.chat.util.JwtUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public User register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (lambdaQuery().eq(User::getUsername, request.getUsername()).exists()) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setStatus("offline");

        save(user);
        return user;
    }

    public String login(LoginRequest request) {
        User user = lambdaQuery()
                .eq(User::getUsername, request.getUsername())
                .one();

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 更新登录状态
        user.setStatus("online");
        user.setLastLoginTime(LocalDateTime.now());
        updateById(user);

        // 生成JWT token
        return jwtUtil.generateToken(user.getUserId(), user.getUsername());
    }
} 