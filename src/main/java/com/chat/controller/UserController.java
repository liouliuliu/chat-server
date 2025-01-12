package com.chat.controller;

import com.chat.config.UploadConfig;
import com.chat.dto.LoginRequest;
import com.chat.dto.LoginResponse;
import com.chat.dto.RegisterRequest;
import com.chat.dto.UpdateProfileRequest;
import com.chat.dto.AvatarResponse;
import com.chat.dto.ErrorResponse;
import com.chat.entity.User;
import com.chat.service.UserService;
import com.chat.util.ImageUtil;
import com.chat.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UploadConfig uploadConfig;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            User user = userService.login(request);
            String token = userService.generateToken(user);
            
            LoginResponse response = new LoginResponse();
            response.setUserId(user.getUserId());
            response.setUsername(user.getUsername());
            response.setNickname(user.getNickname());
            response.setAvatarUrl(user.getAvatarUrl());
            response.setToken(token);
            
            log.info("User logged in successfully: {}", user.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        try {
            userService.logout(token.replace("Bearer ", ""));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(jwtToken);
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody UpdateProfileRequest request) {
        String jwtToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(jwtToken);
        
        User user = userService.getUserById(userId);
        user.setNickname(request.getNickname());
        user.setGender(request.getGender());
        
        userService.updateUser(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(
            @RequestHeader("Authorization") String token,
            @RequestParam("avatar") MultipartFile file) throws IOException {
        
        // 验证文件大小
        if (file.getSize() > uploadConfig.getMaxSize()) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("文件大小超过限制，最大允许 5MB"));
        }

        // 验证文件类型
        if (!ImageUtil.isValidImageType(file.getContentType(), uploadConfig.getAllowedTypes())) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("不支持的文件类型，仅支持 JPG、PNG 和 GIF"));
        }

        String jwtToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(jwtToken);

        // 创建上传目录
        Path uploadPath = Paths.get(uploadConfig.getPath());
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 处理图片（裁剪和压缩）
        byte[] processedImage = ImageUtil.resizeImage(file, uploadConfig.getWidth(), uploadConfig.getHeight());

        // 生成文件名
        String filename = UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());
        Path filePath = uploadPath.resolve(filename);

        // 保存处理后的图片
        Files.write(filePath, processedImage);

        // 更新用户头像URL
        User user = userService.getUserById(userId);
        user.setAvatarUrl("/api/uploads/avatars/" + filename);
        userService.updateUser(user);

        return ResponseEntity.ok().body(new AvatarResponse(user.getAvatarUrl()));
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserProfile(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证当前用户是否有权限查看
            String jwtToken = token.replace("Bearer ", "");
            Long currentUserId = jwtUtil.getUserIdFromToken(jwtToken);
            
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Failed to get user profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(e.getMessage()));
        }
    }
} 