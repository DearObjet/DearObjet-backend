package app.dearobjet.backend.domain.chat.service.redis;

import app.dearobjet.backend.domain.chat.dto.ChatMessageResponse;
import app.dearobjet.backend.domain.chat.dto.RedisChatMessageDto;
import app.dearobjet.backend.domain.chat.dto.ReadReceiptDto;
import app.dearobjet.backend.domain.chat.dto.TypingIndicatorDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;

/**
 * Redis Pub/Sub 메시지 구독 서비스
 * 다른 서버에서 발행한 메시지를 수신하여 로컬 WebSocket 클라이언트에게 전달
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageSubscriber implements MessageListener {

    private static final String ROOM_PATTERN = "chat:room:*";
    private static final String USER_PATTERN = "chat:user:*";

    private final RedisMessageListenerContainer listenerContainer;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper chatObjectMapper;

    @PostConstruct
    public void init() {
        // 채팅방 메시지 패턴 구독
        listenerContainer.addMessageListener(this, new PatternTopic(ROOM_PATTERN));
        // 개인 메시지 패턴 구독
        listenerContainer.addMessageListener(this, new PatternTopic(USER_PATTERN));
        log.info("Subscribed to Redis channels: {}, {}", ROOM_PATTERN, USER_PATTERN);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
            String body = new String(message.getBody(), StandardCharsets.UTF_8);

            RedisChatMessageDto redisMessage = chatObjectMapper.readValue(body, RedisChatMessageDto.class);
            log.debug("Received message from channel {}: {}", channel, redisMessage.getEventType());

            handleMessage(channel, redisMessage);
        } catch (Exception e) {
            log.error("Failed to process Redis message", e);
        }
    }

    private void handleMessage(String channel, RedisChatMessageDto message) {
        String roomId = message.getRoomId();
        String destination = "/topic/chat/" + roomId;

        switch (message.getEventType()) {
            case MESSAGE -> {
                // 채팅 메시지를 WebSocket으로 전달
                ChatMessageResponse dto = ChatMessageResponse.builder()
                        .id(message.getMessageId())
                        .roomId(message.getRoomId())
                        .senderId(message.getSenderId())
                        .senderName(message.getSenderName())
                        .senderProfileImage(message.getSenderProfileImage())
                        .content(message.getContent())
                        .messageType(message.getMessageType())
                        .createdAt(message.getTimestamp())
                        .build();
                messagingTemplate.convertAndSend(destination, dto);
            }
            case TYPING -> {
                // 타이핑 상태 전달
                TypingIndicatorDto dto = TypingIndicatorDto.of(
                        roomId, message.getSenderId(), message.getSenderName(), message.getTyping());
                messagingTemplate.convertAndSend(destination + "/typing", dto);
            }
            case READ -> {
                // 읽음 처리 전달
                ReadReceiptDto dto = ReadReceiptDto.of(
                        roomId, message.getSenderId(), message.getTimestamp());
                messagingTemplate.convertAndSend(destination + "/read", dto);
            }
            case JOIN, LEAVE -> {
                // 입장/퇴장 알림 전달
                messagingTemplate.convertAndSend(destination + "/presence", message);
            }
        }
    }
}
