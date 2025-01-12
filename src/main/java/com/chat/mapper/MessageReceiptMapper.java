package com.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.constant.MessageStatus;
import com.chat.entity.MessageReceipt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MessageReceiptMapper extends BaseMapper<MessageReceipt> {
    
    @Select("SELECT * FROM message_receipts WHERE message_id = #{messageId} AND user_id = #{userId}")
    MessageReceipt selectByMessageAndUser(Long messageId, Long userId);

    @Update("UPDATE message_receipts SET status = #{status}, updated_at = NOW() " +
            "WHERE message_id = #{messageId} AND user_id = #{userId}")
    int updateByMessageAndUser(@Param("messageId") Long messageId, 
                             @Param("userId") Long userId, 
                             @Param("status") MessageStatus status);
} 