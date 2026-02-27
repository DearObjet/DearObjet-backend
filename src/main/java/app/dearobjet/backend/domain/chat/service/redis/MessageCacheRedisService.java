package app.dearobjet.backend.domain.chat.service.redis;

import app.dearobjet.backend.domain.chat.dto.ChatMessageResponse;
import app.dearobjet.backend.domain.chat.entity.ChatMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Redis 기반 최근 메시지 캐싱 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageCacheRedisService {

    private static final String MESSAGES_KEY_PREFIX = "messages:room:";
    private static final int MAX_CACHED_MESSAGES = 50;
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper chatObjectMapper;

    /**
     * 최근 메시지 목록에 메시지 추가
     *
     * @param message 추가할 메시지
     */
    public void addRecentMessage(ChatMessage message, Long currentUserId) {
        String key = buildKey(message.getRoomId());

        try {
            ChatMessageResponse dto = ChatMessageResponse.from(message, currentUserId);
            String json = chatObjectMapper.writeValueAsString(dto);

            // 리스트 앞에 추가 (최신 메시지가 앞에)
            redisTemplate.opsForList().leftPush(key, json);
            // 최대 개수 유지
            redisTemplate.opsForList().trim(key, 0, MAX_CACHED_MESSAGES - 1);
            // TTL 갱신
            redisTemplate.expire(key, CACHE_TTL);

            log.debug("Added message to cache for room {}", message.getRoomId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for caching", e);
        }
    }

    /**
     * 채팅방의 최근 메시지 목록 조회
     *
     * @param roomId 채팅방 ID
     * @param limit  조회할 개수
     * @return 메시지 목록 (최신순)
     */
    public List<ChatMessageResponse> getRecentMessages(String roomId, int limit) {
        String key = buildKey(roomId);

        try {
            List<String> jsonMessages = redisTemplate.opsForList().range(key, 0, limit - 1);
            if (jsonMessages == null || jsonMessages.isEmpty()) {
                return Collections.emptyList();
            }

            return jsonMessages.stream()
                    .map(this::deserializeMessage)
                    .filter(dto -> dto != null)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to get recent messages from cache for room {}", roomId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 캐시 존재 여부 확인
     *
     * @param roomId 채팅방 ID
     * @return true - 캐시 존재, false - 캐시 없음
     */
    public boolean hasCache(String roomId) {
        String key = buildKey(roomId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 캐시 무효화
     *
     * @param roomId 채팅방 ID
     */
    public void invalidateCache(String roomId) {
        String key = buildKey(roomId);
        redisTemplate.delete(key);
        log.debug("Invalidated message cache for room {}", roomId);
    }

    /**
     * 캐시 워밍업: DB에서 조회한 메시지로 캐시 초기화
     *
     * @param roomId        채팅방 ID
     * @param messages      메시지 목록 (최신순)
     * @param currentUserId 현재 사용자 ID
     */
    public void warmUp(String roomId, List<ChatMessage> messages, Long currentUserId) {
        if (messages.isEmpty()) {
            return;
        }

        String key = buildKey(roomId);

        // 기존 캐시 삭제
        redisTemplate.delete(key);

        // 메시지 추가 (역순으로 추가해야 최신이 앞에 위치)
        List<ChatMessage> reversed = new java.util.ArrayList<>(messages);
        java.util.Collections.reverse(reversed);
        for (ChatMessage message : reversed) {
            try {
                ChatMessageResponse dto = ChatMessageResponse.from(message, currentUserId);
                String json = chatObjectMapper.writeValueAsString(dto);
                redisTemplate.opsForList().leftPush(key, json);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize message during warmup", e);
            }
        }

        redisTemplate.expire(key, CACHE_TTL);
        log.debug("Warmed up message cache for room {} with {} messages", roomId, messages.size());
    }

    private String buildKey(String roomId) {
        return MESSAGES_KEY_PREFIX + roomId + ":recent";
    }

    private ChatMessageResponse deserializeMessage(String json) {
        try {
            return chatObjectMapper.readValue(json, ChatMessageResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize message from cache", e);
            return null;
        }
    }
}
