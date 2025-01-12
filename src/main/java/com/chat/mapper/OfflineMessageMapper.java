package com.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.entity.OfflineMessage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OfflineMessageMapper extends BaseMapper<OfflineMessage> {
    
    @Select("SELECT message_id FROM offline_messages WHERE user_id = #{userId}")
    List<Long> selectMessageIdsByUserId(Long userId);
    
    @Delete("DELETE FROM offline_messages WHERE message_id = #{messageId} AND user_id = #{userId}")
    int deleteByMessageAndUser(Long messageId, Long userId);
} 