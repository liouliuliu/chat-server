package com.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.entity.MessageReceipt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MessageReceiptMapper extends BaseMapper<MessageReceipt> {
    
    @Select("SELECT * FROM message_receipts WHERE message_id = #{messageId} AND user_id = #{userId}")
    MessageReceipt selectByMessageAndUser(Long messageId, Long userId);
} 