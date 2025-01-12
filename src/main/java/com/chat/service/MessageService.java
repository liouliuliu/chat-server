package com.chat.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chat.constant.MessageStatus;
import com.chat.constant.MessageType;
import com.chat.entity.Message;
import com.chat.entity.MessageReceipt;
import com.chat.entity.OfflineMessage;
import com.chat.mapper.MessageMapper;
import com.chat.mapper.MessageReceiptMapper;
import com.chat.mapper.OfflineMessageMapper;
import com.chat.model.ChatMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService extends ServiceImpl<MessageMapper, Message> {

    private final MessageMapper messageMapper;
    private final MessageReceiptMapper messageReceiptMapper;
    private final OfflineMessageMapper offlineMessageMapper;

    public MessageService(MessageMapper messageMapper, 
                         MessageReceiptMapper messageReceiptMapper,
                         OfflineMessageMapper offlineMessageMapper) {
        this.messageMapper = messageMapper;
        this.messageReceiptMapper = messageReceiptMapper;
        this.offlineMessageMapper = offlineMessageMapper;
    }

    @Transactional
    public Message saveMessage(ChatMessage chatMessage) {
        Message message = new Message();
        message.setType(chatMessage.getType());
        message.setFromUserId(chatMessage.getFromUserId());
        message.setToUserId(chatMessage.getToUserId());
        message.setGroupId(chatMessage.getGroupId());
        message.setContent(chatMessage.getContent());
        message.setStatus(MessageStatus.SENT);
        message.setCreatedAt(LocalDateTime.now());
        
        save(message);

        // 如果是群聊消息，为每个群成员创建消息回执
        if (message.getType() == MessageType.GROUP_MSG) {
            createGroupMessageReceipts(message);
        }
        
        return message;
    }

    @Transactional
    public void markAsDelivered(Long messageId, Long userId) {
        Message message = getById(messageId);
        if (message != null) {
            message.setStatus(MessageStatus.DELIVERED);
            updateById(message);

            // 更新消息回执状态
            updateMessageReceipt(messageId, userId, MessageStatus.DELIVERED);
            
            // 如果消息已投递，从离线消息表中删除
            removeOfflineMessage(messageId, userId);
        }
    }

    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        Message message = getById(messageId);
        if (message != null) {
            message.setStatus(MessageStatus.READ);
            updateById(message);

            // 更新消息回执状态
            updateMessageReceipt(messageId, userId, MessageStatus.READ);
        }
    }

    public List<Message> getHistoryMessages(Long currentUserId, Long otherUserId) {
        return lambdaQuery()
                .and(wrapper -> wrapper
                    .and(w -> w
                        .eq(Message::getFromUserId, currentUserId)
                        .eq(Message::getToUserId, otherUserId))
                    .or(w -> w
                        .eq(Message::getFromUserId, otherUserId)
                        .eq(Message::getToUserId, currentUserId)))
                .orderByAsc(Message::getCreatedAt)
                .list();
    }

    public List<Message> getOfflineMessages(Long userId) {
        // 获取用户的所有离线消息ID
        List<Long> messageIds = offlineMessageMapper.selectMessageIdsByUserId(userId);
        if (messageIds.isEmpty()) {
            return List.of();
        }
        
        // 获取消息详情
        return lambdaQuery()
                .in(Message::getMessageId, messageIds)
                .orderByAsc(Message::getCreatedAt)
                .list();
    }

    @Transactional
    public void saveOfflineMessage(Message message, Long userId) {
        OfflineMessage offlineMessage = new OfflineMessage();
        offlineMessage.setMessageId(message.getMessageId());
        offlineMessage.setUserId(userId);
        offlineMessageMapper.insert(offlineMessage);
    }

    private void createGroupMessageReceipts(Message message) {
        // TODO: 获取群组成员列表，为每个成员创建消息回执
        // List<Long> memberIds = groupService.getGroupMemberIds(message.getGroupId());
        // for (Long memberId : memberIds) {
        //     createMessageReceipt(message.getMessageId(), memberId);
        // }
    }

    private void createMessageReceipt(Long messageId, Long userId) {
        MessageReceipt receipt = new MessageReceipt();
        receipt.setMessageId(messageId);
        receipt.setUserId(userId);
        receipt.setStatus(MessageStatus.DELIVERED);
        receipt.setCreatedAt(LocalDateTime.now());
        messageReceiptMapper.insert(receipt);
    }

    private void updateMessageReceipt(Long messageId, Long userId, MessageStatus status) {
        MessageReceipt receipt = messageReceiptMapper.selectByMessageAndUser(messageId, userId);
        if (receipt != null) {
            receipt.setStatus(status);
            if (status == MessageStatus.READ) {
                receipt.setReadAt(LocalDateTime.now());
            }
            messageReceiptMapper.updateById(receipt);
        }
    }

    private void removeOfflineMessage(Long messageId, Long userId) {
        offlineMessageMapper.deleteByMessageAndUser(messageId, userId);
    }
} 