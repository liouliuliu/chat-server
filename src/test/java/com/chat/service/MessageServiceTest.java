package com.chat.service;

import com.chat.constant.MessageStatus;
import com.chat.constant.MessageType;
import com.chat.entity.Message;
import com.chat.entity.MessageReceipt;
import com.chat.entity.OfflineMessage;
import com.chat.mapper.MessageMapper;
import com.chat.mapper.MessageReceiptMapper;
import com.chat.mapper.OfflineMessageMapper;
import com.chat.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class MessageServiceTest {

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private MessageReceiptMapper messageReceiptMapper;

    @Mock
    private OfflineMessageMapper offlineMessageMapper;

    @InjectMocks
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveMessage_PrivateMessage_ShouldSucceed() {
        // Arrange
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(MessageType.PRIVATE_MSG);
        chatMessage.setFromUserId(1L);
        chatMessage.setToUserId(2L);
        chatMessage.setContent("Hello");

        when(messageMapper.insert(any(Message.class))).thenReturn(1);

        // Act
        Message result = messageService.saveMessage(chatMessage);

        // Assert
        assertNotNull(result);
        assertEquals(MessageType.PRIVATE_MSG, result.getType());
        assertEquals(chatMessage.getFromUserId(), result.getFromUserId());
        assertEquals(chatMessage.getToUserId(), result.getToUserId());
        assertEquals(chatMessage.getContent(), result.getContent());
        assertEquals(MessageStatus.SENT, result.getStatus());
        verify(messageMapper).insert(any(Message.class));
    }

    @Test
    void markAsDelivered_ShouldUpdateStatusAndRemoveOfflineMessage() {
        // Arrange
        Long messageId = 1L;
        Long userId = 2L;
        Message message = new Message();
        message.setMessageId(messageId);
        message.setStatus(MessageStatus.SENT);

        when(messageMapper.selectById(messageId)).thenReturn(message);
        when(messageMapper.updateById(any(Message.class))).thenReturn(1);

        // Act
        messageService.markAsDelivered(messageId, userId);

        // Assert
        assertEquals(MessageStatus.DELIVERED, message.getStatus());
        verify(messageMapper).updateById(message);
        verify(messageReceiptMapper).updateByMessageAndUser(messageId, userId, MessageStatus.DELIVERED);
        verify(offlineMessageMapper).deleteByMessageAndUser(messageId, userId);
    }

    @Test
    void markAsRead_ShouldUpdateStatus() {
        // Arrange
        Long messageId = 1L;
        Long userId = 2L;
        Message message = new Message();
        message.setMessageId(messageId);
        message.setStatus(MessageStatus.DELIVERED);

        when(messageMapper.selectById(messageId)).thenReturn(message);
        when(messageMapper.updateById(any(Message.class))).thenReturn(1);

        // Act
        messageService.markAsRead(messageId, userId);

        // Assert
        assertEquals(MessageStatus.READ, message.getStatus());
        verify(messageMapper).updateById(message);
        verify(messageReceiptMapper).updateByMessageAndUser(messageId, userId, MessageStatus.READ);
    }

    @Test
    void getHistoryMessages_ShouldReturnMessages() {
        // Arrange
        Long currentUserId = 1L;
        Long otherUserId = 2L;
        List<Message> expectedMessages = Arrays.asList(
            createMessage(1L, currentUserId, otherUserId),
            createMessage(2L, otherUserId, currentUserId)
        );

        when(messageMapper.selectList(any())).thenReturn(expectedMessages);

        // Act
        List<Message> result = messageService.getHistoryMessages(currentUserId, otherUserId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(messageMapper).selectList(any());
    }

    @Test
    void getOfflineMessages_ShouldReturnMessages() {
        // Arrange
        Long userId = 1L;
        List<Long> messageIds = Arrays.asList(1L, 2L);
        List<Message> expectedMessages = Arrays.asList(
            createMessage(1L, 2L, userId),
            createMessage(2L, 3L, userId)
        );

        when(offlineMessageMapper.selectMessageIdsByUserId(userId)).thenReturn(messageIds);
        when(messageMapper.selectList(any())).thenReturn(expectedMessages);

        // Act
        List<Message> result = messageService.getOfflineMessages(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(offlineMessageMapper).selectMessageIdsByUserId(userId);
        verify(messageMapper).selectList(any());
    }

    @Test
    void saveOfflineMessage_ShouldSucceed() {
        // Arrange
        Message message = createMessage(1L, 2L, 3L);
        Long userId = 3L;

        when(offlineMessageMapper.insert(any(OfflineMessage.class))).thenReturn(1);

        // Act
        messageService.saveOfflineMessage(message, userId);

        // Assert
        verify(offlineMessageMapper).insert(any(OfflineMessage.class));
    }

    private Message createMessage(Long messageId, Long fromUserId, Long toUserId) {
        Message message = new Message();
        message.setMessageId(messageId);
        message.setType(MessageType.PRIVATE_MSG);
        message.setFromUserId(fromUserId);
        message.setToUserId(toUserId);
        message.setContent("Test message");
        message.setStatus(MessageStatus.SENT);
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }
} 