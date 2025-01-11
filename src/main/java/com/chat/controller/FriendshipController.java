package com.chat.controller;

import com.chat.dto.FriendRequest;
import com.chat.dto.UserSearchResponse;
import com.chat.service.FriendshipService;
import com.chat.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/friends")
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final JwtUtil jwtUtil;

    public FriendshipController(FriendshipService friendshipService, JwtUtil jwtUtil) {
        this.friendshipService = friendshipService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUser(@RequestParam String username, @RequestHeader("Authorization") String token) {
        Long currentUserId = Long.parseLong(jwtUtil.parseToken(token.substring(7)).getSubject());
        UserSearchResponse user = friendshipService.searchUser(username, currentUserId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/request")
    public ResponseEntity<?> sendFriendRequest(
            @Valid @RequestBody FriendRequest request,
            @RequestHeader("Authorization") String token) {
        Long currentUserId = Long.parseLong(jwtUtil.parseToken(token.substring(7)).getSubject());
        friendshipService.sendFriendRequest(currentUserId, request.getUsername());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/requests")
    public ResponseEntity<?> getPendingRequests(@RequestHeader("Authorization") String token) {
        Long currentUserId = Long.parseLong(jwtUtil.parseToken(token.substring(7)).getSubject());
        return ResponseEntity.ok(friendshipService.getPendingRequests(currentUserId));
    }

    @PostMapping("/requests/{requestId}/handle")
    public ResponseEntity<?> handleFriendRequest(
            @PathVariable Long requestId,
            @RequestParam boolean accept,
            @RequestHeader("Authorization") String token) {
        Long currentUserId = Long.parseLong(jwtUtil.parseToken(token.substring(7)).getSubject());
        friendshipService.handleFriendRequest(currentUserId, requestId, accept);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<?> getFriendList(@RequestHeader("Authorization") String token) {
        Long currentUserId = Long.parseLong(jwtUtil.parseToken(token.substring(7)).getSubject());
        return ResponseEntity.ok(friendshipService.getFriendList(currentUserId));
    }
} 