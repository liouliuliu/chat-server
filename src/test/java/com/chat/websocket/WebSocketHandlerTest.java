package com.chat.websocket;

import com.chat.constant.MessageType;
import com.chat.entity.Message;
import com.chat.model.ChatMessage;
import com.chat.service.MessageService;
import com.chat.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WebSocketHandlerTest {

    @Mock
    private MessageService messageService;

    @Mock
    private UserService userService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ChannelHandlerContext ctx;

    @Mock
    private Channel channel;

    @InjectMocks
    private WebSocketHandler webSocketHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(ctx.channel()).thenReturn(channel);
    }

    @Test
    void channelRead0_ConnectMessage_ShouldAddChannel() throws Exception {
        // Arrange
        ChatMessage connectMessage = new ChatMessage();
        connectMessage.setType(MessageType.CONNECT);
        connectMessage.setFromUserId(1L);

        TextWebSocketFrame frame = new TextWebSocketFrame("{\"type\":\"CONNECT\",\"fromUserId\":1}");
        when(objectMapper.readValue(anyString(), eq(ChatMessage.class))).thenReturn(connectMessage);

        // Act
        webSocketHandler.channelRead0(ctx, frame);

        // Assert
        verify(channel).attr(WebSocketHandler.USER_ID_KEY).set(1L);
    }

    @Test
    void channelRead0_PrivateMessage_ShouldSaveAndForward() throws Exception {
        // Arrange
        ChatMessage privateMessage = new ChatMessage();
        privateMessage.setType(MessageType.PRIVATE_MSG);
        privateMessage.setFromUserId(1L);
        privateMessage.setToUserId(2L);
        privateMessage.setContent("Hello");

        Message savedMessage = new Message();
        savedMessage.setMessageId(1L);
        savedMessage.setType(MessageType.PRIVATE_MSG);
        savedMessage.setFromUserId(1L);
        savedMessage.setToUserId(2L);
        savedMessage.setContent("Hello");

        TextWebSocketFrame frame = new TextWebSocketFrame(
            "{\"type\":\"PRIVATE_MSG\",\"fromUserId\":1,\"toUserId\":2,\"content\":\"Hello\"}"
        );

        Channel recipientChannel = mock(Channel.class);
        when(objectMapper.readValue(anyString(), eq(ChatMessage.class))).thenReturn(privateMessage);
        when(messageService.saveMessage(privateMessage)).thenReturn(savedMessage);
        when(channel.attr(WebSocketHandler.USER_ID_KEY).get()).thenReturn(1L);

        // Act
        webSocketHandler.channelRead0(ctx, frame);

        // Assert
        verify(messageService).saveMessage(privateMessage);
        verify(messageService).saveOfflineMessage(any(Message.class), eq(2L));
    }

    @Test
    void channelRead0_InvalidMessage_ShouldHandleError() throws Exception {
        // Arrange
        TextWebSocketFrame frame = new TextWebSocketFrame("invalid json");
        when(objectMapper.readValue(anyString(), eq(ChatMessage.class)))
            .thenThrow(new RuntimeException("Invalid JSON"));

        // Act
        webSocketHandler.channelRead0(ctx, frame);

        // Assert
        verify(messageService, never()).saveMessage(any(ChatMessage.class));
    }

    @Test
    void channelInactive_ShouldRemoveChannel() throws Exception {
        // Arrange
        when(channel.attr(WebSocketHandler.USER_ID_KEY).get()).thenReturn(1L);

        // Act
        webSocketHandler.channelInactive(ctx);

        // Assert
        verify(channel).attr(WebSocketHandler.USER_ID_KEY);
    }
} 