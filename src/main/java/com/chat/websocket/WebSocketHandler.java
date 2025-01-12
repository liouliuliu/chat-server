package com.chat.websocket;

import com.chat.entity.Message;
import com.chat.model.ChatMessage;
import com.chat.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import io.netty.util.AttributeKey;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Sharable
@Component
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    public static final AttributeKey<Long> USER_ID_KEY = AttributeKey.valueOf("userId");

    private static final ConcurrentHashMap<Long, Channel> userChannels = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Channel, Long> channelUsers = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    public WebSocketHandler(ObjectMapper objectMapper, MessageService messageService) {
        this.objectMapper = objectMapper;
        this.messageService = messageService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        String text = frame.text();
        try {
            ChatMessage chatMessage = objectMapper.readValue(text, ChatMessage.class);
            log.info("Received message: {}", chatMessage);

            switch (chatMessage.getType()) {
                case CONNECT:
                    handleConnect(ctx.channel(), chatMessage);
                    break;
                case PRIVATE_MSG:
                    handlePrivateMessage(chatMessage);
                    break;
                default:
                    log.warn("Unknown message type: {}", chatMessage.getType());
            }
        } catch (Exception e) {
            log.error("Error processing message: {}", text, e);
            ctx.close();
        }
    }

    private void handleConnect(Channel channel, ChatMessage message) {
        if (message.getFromUserId() == null) {
            log.error("Connect message missing fromUserId");
            channel.close();
            return;
        }

        Long userId = message.getFromUserId();
        // 移除旧的连接
        Channel oldChannel = userChannels.get(userId);
        if (oldChannel != null) {
            channelUsers.remove(oldChannel);
            oldChannel.close();
        }
        // 保存新的连接
        userChannels.put(userId, channel);
        channelUsers.put(channel, userId);
        log.info("User {} connected", userId);
    }

    private void handlePrivateMessage(ChatMessage chatMessage) {
        try {
            // 保存消息到数据库
            Message message = messageService.saveMessage(chatMessage);
            
            // 发送消息给接收者
            Channel toChannel = userChannels.get(chatMessage.getToUserId());
            if (toChannel != null && toChannel.isActive()) {
                String messageText = objectMapper.writeValueAsString(chatMessage);
                toChannel.writeAndFlush(new TextWebSocketFrame(messageText));
                // 标记消息为已投递
                messageService.markAsDelivered(message.getMessageId(), chatMessage.getToUserId());
            } else {
                log.warn("User {} is offline, saving offline message", chatMessage.getToUserId());
                // 存储离线消息
                messageService.saveOfflineMessage(message, chatMessage.getToUserId());
            }
        } catch (Exception e) {
            log.error("Error handling private message", e);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Long userId = channelUsers.remove(ctx.channel());
        if (userId != null) {
            userChannels.remove(userId);
            log.info("User {} disconnected", userId);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("WebSocket error", cause);
        channelInactive(ctx);
        ctx.close();
    }
} 