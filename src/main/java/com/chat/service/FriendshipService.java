package com.chat.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chat.dto.FriendRequest;
import com.chat.dto.UserDTO;
import com.chat.dto.UserSearchResponse;
import com.chat.entity.Friendship;
import com.chat.entity.User;
import com.chat.mapper.FriendshipMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendshipService extends ServiceImpl<FriendshipMapper, Friendship> {

    private final UserService userService;

    public FriendshipService(UserService userService) {
        this.userService = userService;
    }

    public UserSearchResponse searchUser(String username, Long currentUserId) {
        User user = userService.lambdaQuery()
                .eq(User::getUsername, username)
                .one();
        
        if (user == null || user.getUserId().equals(currentUserId)) {
            return null;
        }

        UserSearchResponse response = new UserSearchResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatarUrl(user.getAvatarUrl());

        // 检查好友关系
        Friendship friendship = lambdaQuery()
                .and(q -> q
                        .or(or -> or
                                .eq(Friendship::getUserId1, currentUserId)
                                .eq(Friendship::getUserId2, user.getUserId()))
                        .or(or -> or
                                .eq(Friendship::getUserId1, user.getUserId())
                                .eq(Friendship::getUserId2, currentUserId)))
                .one();

        response.setFriendshipStatus(friendship != null ? friendship.getStatus() : null);
        return response;
    }

    @Transactional
    public void sendFriendRequest(Long fromUserId, String toUsername) {
        User toUser = userService.lambdaQuery()
                .eq(User::getUsername, toUsername)
                .one();

        if (toUser == null) {
            throw new RuntimeException("用户不存在");
        }

        if (toUser.getUserId().equals(fromUserId)) {
            throw new RuntimeException("不能添加自己为好友");
        }

        // 检查是否已经是好友或已经发送过请求
        boolean exists = lambdaQuery()
                .and(q -> q
                        .or(or -> or
                                .eq(Friendship::getUserId1, fromUserId)
                                .eq(Friendship::getUserId2, toUser.getUserId()))
                        .or(or -> or
                                .eq(Friendship::getUserId1, toUser.getUserId())
                                .eq(Friendship::getUserId2, fromUserId)))
                .exists();

        if (exists) {
            throw new RuntimeException("已经是好友或已发送过好友请求");
        }

        // 创建好友请求
        Friendship friendship = new Friendship();
        friendship.setUserId1(fromUserId);
        friendship.setUserId2(toUser.getUserId());
        friendship.setStatus("pending");
        save(friendship);
    }

    @Transactional
    public void handleFriendRequest(Long userId, Long requestId, boolean accept) {
        Friendship friendship = getById(requestId);
        if (friendship == null || !friendship.getUserId2().equals(userId)) {
            throw new RuntimeException("好友请求不存在或无权处理");
        }

        if (!"pending".equals(friendship.getStatus())) {
            throw new RuntimeException("该请求已被处理");
        }

        friendship.setStatus(accept ? "accepted" : "rejected");
        updateById(friendship);
    }

    public List<UserSearchResponse> getPendingRequests(Long userId) {
        List<Friendship> requests = lambdaQuery()
                .eq(Friendship::getUserId2, userId)
                .eq(Friendship::getStatus, "pending")
                .list();

        return requests.stream().map(friendship -> {
            User fromUser = userService.getById(friendship.getUserId1());
            UserSearchResponse response = new UserSearchResponse();
            response.setUserId(fromUser.getUserId());
            response.setUsername(fromUser.getUsername());
            response.setNickname(fromUser.getNickname());
            response.setAvatarUrl(fromUser.getAvatarUrl());
            response.setFriendshipStatus("pending");
            response.setRequestId(friendship.getId()); // 需要在UserSearchResponse中添加此字段
            return response;
        }).collect(Collectors.toList());
    }

    public List<UserDTO> getFriendList(Long userId) {
        List<Friendship> friendships = lambdaQuery()
                .and(q -> q
                        .or(or -> or.eq(Friendship::getUserId1, userId))
                        .or(or -> or.eq(Friendship::getUserId2, userId)))
                .eq(Friendship::getStatus, "accepted")
                .list();

        return friendships.stream()
                .map(friendship -> {
                    Long friendId = friendship.getUserId1().equals(userId) 
                            ? friendship.getUserId2() 
                            : friendship.getUserId1();
                    User friend = userService.getById(friendId);
                    return convertToDTO(friend);
                })
                .collect(Collectors.toList());
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setStatus(user.getStatus());
        return dto;
    }
} 