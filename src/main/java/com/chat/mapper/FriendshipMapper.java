package com.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.entity.Friendship;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FriendshipMapper extends BaseMapper<Friendship> {
    
    Friendship selectByUserAndFriend(@Param("userId") Long userId, @Param("friendId") Long friendId);
    
    List<Friendship> selectByUserId(@Param("userId") Long userId);
} 