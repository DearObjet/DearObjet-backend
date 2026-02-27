package app.dearobjet.backend.domain.chat.service.redis;

import app.dearobjet.backend.domain.chat.dto.RedisChatMessageDto;
import app.dearobjet.backend.global.exception.BusinessException;
import app.dearobjet.backend.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis Pub/Sub 메시지 발행 서비스
 * 스케일 아웃 환경에서 다중 서버 간 메시지 브로드캐스트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessagePublisher {

    private static final String CHANNEL_PREFIX = "chat:room:";
    private static final String USER_CHANNEL_PREFIX = "chat:user:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper chatObjectMapper;

    /**
     * 채팅방에 메시지 발행
     *
     * @param message 발행할 메시지
     */
    public void publishToRoom(RedisChatMessageDto message) {
        String channel = CHANNEL_PREFIX + message.getRoomId();
        publish(channel, message);
    }

    /**
     * 특정 사용자에게 메시지 발행 (개인 알림용)
     *
     * @param userId  대상 사용자 ID
     * @param message 발행할 메시지
     */
    public void publishToUser(Long userId, RedisChatMessageDto message) {
        String channel = USER_CHANNEL_PREFIX + userId;
        publish(channel, message);
    }

    /**
     * 채팅방의 모든 참여자에게 메시지 발행
     *
     * @param roomId  채팅방 ID
     * @param message 발행할 메시지
     */
    public void broadcastToRoom(String roomId, RedisChatMessageDto message) {
        publishToRoom(message);
    }

    private void publish(String channel, RedisChatMessageDto message) {
        try {
            String jsonMessage = chatObjectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(channel, jsonMessage);
            log.debug("Published message to channel {}: {}", channel, message.getEventType());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for channel {}", channel, e);
            throw new BusinessException(ErrorCode.CHAT_MESSAGE_PUBLISH_FAILED, e);
        }
    }
}
