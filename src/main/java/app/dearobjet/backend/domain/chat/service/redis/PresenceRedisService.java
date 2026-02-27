package app.dearobjet.backend.domain.chat.service.redis;

import app.dearobjet.backend.domain.chat.dto.UserPresenceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redis 기반 사용자 온라인 상태 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceRedisService {

    private static final String PRESENCE_KEY_PREFIX = "presence:";
    private static final String ROOM_PRESENCE_KEY_PREFIX = "presence:room:";
    private static final Duration PRESENCE_TTL = Duration.ofMinutes(5);

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 사용자 온라인 상태 설정
     *
     * @param userId 사용자 ID
     * @param status 상태
     */
    public void setPresence(Long userId, UserPresenceDto.Status status) {
        String key = PRESENCE_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(key, status.name(), PRESENCE_TTL);
        log.debug("Set presence for user {}: {}", userId, status);
    }

    /**
     * 사용자 온라인 상태 조회
     *
     * @param userId 사용자 ID
     * @return 상태 (없으면 OFFLINE)
     */
    public UserPresenceDto.Status getPresence(Long userId) {
        String key = PRESENCE_KEY_PREFIX + userId;
        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return UserPresenceDto.Status.OFFLINE;
        }

        try {
            return UserPresenceDto.Status.valueOf(value);
        } catch (IllegalArgumentException e) {
            return UserPresenceDto.Status.OFFLINE;
        }
    }

    /**
     * 사용자 온라인 상태 갱신 (heartbeat)
     *
     * @param userId 사용자 ID
     */
    public void refreshPresence(Long userId) {
        String key = PRESENCE_KEY_PREFIX + userId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.expire(key, PRESENCE_TTL);
        }
    }

    /**
     * 사용자 오프라인 처리
     *
     * @param userId 사용자 ID
     */
    public void setOffline(Long userId) {
        String key = PRESENCE_KEY_PREFIX + userId;
        redisTemplate.delete(key);
        log.debug("User {} is now offline", userId);
    }

    /**
     * 채팅방에 사용자 입장 기록
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     */
    public void joinRoom(String roomId, Long userId) {
        String key = ROOM_PRESENCE_KEY_PREFIX + roomId;
        double score = Instant.now().toEpochMilli();
        redisTemplate.opsForZSet().add(key, userId.toString(), score);
        log.debug("User {} joined room {}", userId, roomId);
    }

    /**
     * 채팅방에서 사용자 퇴장 기록
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     */
    public void leaveRoom(String roomId, Long userId) {
        String key = ROOM_PRESENCE_KEY_PREFIX + roomId;
        redisTemplate.opsForZSet().remove(key, userId.toString());
        log.debug("User {} left room {}", userId, roomId);
    }

    /**
     * 채팅방의 온라인 사용자 목록 조회
     *
     * @param roomId 채팅방 ID
     * @return 온라인 사용자 ID 목록
     */
    public Set<Long> getOnlineUsersInRoom(String roomId) {
        String key = ROOM_PRESENCE_KEY_PREFIX + roomId;
        Set<String> members = redisTemplate.opsForZSet().range(key, 0, -1);

        if (members == null) {
            return Set.of();
        }

        // 5분 이상 활동 없는 사용자 제거
        long cutoffTime = Instant.now().minusMillis(PRESENCE_TTL.toMillis()).toEpochMilli();
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, cutoffTime);

        return members.stream()
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }

    /**
     * 채팅방 내 사용자 활동 시간 갱신
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     */
    public void updateRoomActivity(String roomId, Long userId) {
        String key = ROOM_PRESENCE_KEY_PREFIX + roomId;
        double score = Instant.now().toEpochMilli();
        redisTemplate.opsForZSet().add(key, userId.toString(), score);
    }

    /**
     * 사용자의 마지막 활동 시간 조회
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     * @return 마지막 활동 시간 (없으면 null)
     */
    public LocalDateTime getLastActivity(String roomId, Long userId) {
        String key = ROOM_PRESENCE_KEY_PREFIX + roomId;
        Double score = redisTemplate.opsForZSet().score(key, userId.toString());

        if (score == null) {
            return null;
        }

        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(score.longValue()),
                ZoneId.systemDefault()
        );
    }
}
