package com.chat.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chat.constant.FriendshipStatus;
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
                                .eq(Friendship::getUserId, currentUserId)
                                .eq(Friendship::getFriendId, user.getUserId()))
                        .or(or -> or
                                .eq(Friendship::getUserId, user.getUserId())
                                .eq(Friendship::getFriendId, currentUserId)))
                .one();

        response.setFriendshipStatus(friendship != null ? friendship.getStatus().toString() : null);
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
                                .eq(Friendship::getUserId, fromUserId)
                                .eq(Friendship::getFriendId, toUser.getUserId()))
                        .or(or -> or
                                .eq(Friendship::getUserId, toUser.getUserId())
                                .eq(Friendship::getFriendId, fromUserId)))
                .exists();

        if (exists) {
            throw new RuntimeException("已经是好友或已发送过好友请求");
        }

        // 创建好友请求
        Friendship friendship = new Friendship();
        friendship.setUserId(fromUserId);
        friendship.setFriendId(toUser.getUserId());
        friendship.setStatus(FriendshipStatus.PENDING);
        save(friendship);
    }

    @Transactional
    public void handleFriendRequest(Long userId, Long requestId, boolean accept) {
        Friendship friendship = getById(requestId);
        if (friendship == null || !friendship.getFriendId().equals(userId)) {
            throw new RuntimeException("好友请求不存在或无权处理");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new RuntimeException("该请求已被处理");
        }

        if (accept) {
            friendship.setStatus(FriendshipStatus.ACTIVE);
            updateById(friendship);
            
            // 创建反向的好友关系
            Friendship reverseFriendship = new Friendship();
            reverseFriendship.setUserId(friendship.getFriendId());
            reverseFriendship.setFriendId(friendship.getUserId());
            reverseFriendship.setStatus(FriendshipStatus.ACTIVE);
            save(reverseFriendship);
        } else {
            friendship.setStatus(FriendshipStatus.REJECTED);
            updateById(friendship);
        }
    }

    public List<UserSearchResponse> getPendingRequests(Long userId) {
        List<Friendship> requests = lambdaQuery()
                .eq(Friendship::getFriendId, userId)
                .eq(Friendship::getStatus, FriendshipStatus.PENDING)
                .list();

        return requests.stream().map(friendship -> {
            User fromUser = userService.getById(friendship.getUserId());
            UserSearchResponse response = new UserSearchResponse();
            response.setUserId(fromUser.getUserId());
            response.setUsername(fromUser.getUsername());
            response.setNickname(fromUser.getNickname());
            response.setAvatarUrl(fromUser.getAvatarUrl());
            response.setFriendshipStatus("pending");
            response.setRequestId(friendship.getFriendshipId());
            return response;
        }).collect(Collectors.toList());
    }

    public List<UserDTO> getFriendList(Long userId) {
        List<Friendship> friendships = lambdaQuery()
                .and(q -> q
                        .or(or -> or.eq(Friendship::getUserId, userId))
                        .or(or -> or.eq(Friendship::getFriendId, userId)))
                .eq(Friendship::getStatus, FriendshipStatus.ACTIVE)
                .list();

        return friendships.stream()
                .map(friendship -> {
                    Long friendId = friendship.getUserId().equals(userId) 
                            ? friendship.getFriendId() 
                            : friendship.getUserId();
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