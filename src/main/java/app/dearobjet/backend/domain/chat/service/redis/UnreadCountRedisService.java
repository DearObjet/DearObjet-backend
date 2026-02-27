package app.dearobjet.backend.domain.chat.service.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

/**
 * Redis 기반 읽지 않은 메시지 수 관리 서비스
 * 원자적 연산(INCR/DECR)으로 동시성 문제 해결
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UnreadCountRedisService {

    private static final String UNREAD_KEY_PREFIX = "unread:";
    private static final String TOTAL_UNREAD_KEY_PREFIX = "unread:total:";
    private static final String DIRTY_SET_KEY = "unread:dirty";
    private static final Duration UNREAD_TTL = Duration.ofDays(7);

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 읽지 않은 메시지 수 증가 (원자적 연산)
     *
     * @param userId 사용자 ID
     * @param roomId 채팅방 ID
     * @return 증가 후 카운트
     */
    public long incrementUnreadCount(Long userId, String roomId) {
        String key = buildUnreadKey(userId, roomId);
        Long count = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, UNREAD_TTL);

        // 전체 읽지 않은 수도 증가
        incrementTotalUnreadCount(userId);

        // DB 동기화 대상으로 등록
        markDirty(userId, roomId);

        log.debug("Incremented unread count for user {} in room {}: {}", userId, roomId, count);
        return count != null ? count : 0;
    }

    /**
     * 읽지 않은 메시지 수 조회
     *
     * @param userId 사용자 ID
     * @param roomId 채팅방 ID
     * @return 읽지 않은 메시지 수
     */
    public int getUnreadCount(Long userId, String roomId) {
        String key = buildUnreadKey(userId, roomId);
        try {
            String value = redisTemplate.opsForValue().get(key);
            return value != null ? Integer.parseInt(value) : 0;
        } catch (DataAccessException e) {
            log.warn("Failed to get unread count from Redis for user {} in room {}", userId, roomId);
            return 0;
        }
    }

    /**
     * 채팅방 읽음 처리 (카운트 초기화)
     *
     * @param userId 사용자 ID
     * @param roomId 채팅방 ID
     * @return 초기화 전 카운트 (DB 동기화용)
     */
    public int markAsRead(Long userId, String roomId) {
        String key = buildUnreadKey(userId, roomId);
        String value = redisTemplate.opsForValue().getAndDelete(key);
        int previousCount = value != null ? Integer.parseInt(value) : 0;

        // 전체 읽지 않은 수에서 차감
        if (previousCount > 0) {
            decrementTotalUnreadCount(userId, previousCount);
        }

        // DB 동기화 대상으로 등록 (0으로 초기화된 상태도 동기화 필요)
        markDirty(userId, roomId);

        log.debug("Marked as read for user {} in room {}, previous count: {}", userId, roomId, previousCount);
        return previousCount;
    }

    /**
     * 사용자의 전체 읽지 않은 메시지 수 조회
     *
     * @param userId 사용자 ID
     * @return 전체 읽지 않은 메시지 수
     */
    public int getTotalUnreadCount(Long userId) {
        String key = TOTAL_UNREAD_KEY_PREFIX + userId;
        try {
            String value = redisTemplate.opsForValue().get(key);
            return value != null ? Integer.parseInt(value) : 0;
        } catch (DataAccessException e) {
            log.warn("Failed to get total unread count from Redis for user {}", userId);
            return 0;
        }
    }

    /**
     * 캐시 워밍업: DB 값으로 Redis 초기화
     *
     * @param userId      사용자 ID
     * @param roomId      채팅방 ID
     * @param unreadCount DB에서 조회한 읽지 않은 수
     */
    public void warmUp(Long userId, String roomId, int unreadCount) {
        if (unreadCount > 0) {
            String key = buildUnreadKey(userId, roomId);
            redisTemplate.opsForValue().set(key, String.valueOf(unreadCount), UNREAD_TTL);
        }
    }

    /**
     * 특정 채팅방의 모든 참여자 unread 카운트 일괄 증가
     *
     * @param roomId         채팅방 ID
     * @param participantIds 참여자 ID 목록
     * @param excludeUserId  제외할 사용자 ID (발신자)
     */
    public void incrementForParticipants(String roomId, Set<Long> participantIds, Long excludeUserId) {
        for (Long userId : participantIds) {
            if (!userId.equals(excludeUserId)) {
                incrementUnreadCount(userId, roomId);
            }
        }
    }

    /**
     * 변경된 (userId, roomId) 쌍을 dirty set에 등록
     */
    private void markDirty(Long userId, String roomId) {
        // "userId:roomId" 형태로 저장
        redisTemplate.opsForSet().add(DIRTY_SET_KEY, userId + ":" + roomId);
    }

    /**
     * DB 동기화 대상 dirty entries 조회 후 제거
     * SMEMBERS + DEL로 구현 (원자적이지 않지만, 유실된 entry는 다음 변경 시 재등록됨)
     *
     * @return dirty entries 목록 ("userId:roomId" 형태)
     */
    public Set<String> popDirtyEntries() {
        Set<String> entries = redisTemplate.opsForSet().members(DIRTY_SET_KEY);
        if (entries != null && !entries.isEmpty()) {
            redisTemplate.delete(DIRTY_SET_KEY);
        }
        return entries != null ? entries : Set.of();
    }

    private String buildUnreadKey(Long userId, String roomId) {
        return UNREAD_KEY_PREFIX + userId + ":" + roomId;
    }

    private void incrementTotalUnreadCount(Long userId) {
        String key = TOTAL_UNREAD_KEY_PREFIX + userId;
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, UNREAD_TTL);
    }

    private void decrementTotalUnreadCount(Long userId, int amount) {
        String key = TOTAL_UNREAD_KEY_PREFIX + userId;
        redisTemplate.opsForValue().decrement(key, amount);
    }
}
