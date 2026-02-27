package app.dearobjet.backend.domain.chat.service.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Redis 기반 WebSocket 세션 관리 서비스
 * 스케일 아웃 환경에서 사용자가 어느 서버에 연결되어 있는지 추적
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionRedisService {

    private static final String SESSION_KEY_PREFIX = "session:user:";
    private static final String SERVER_USERS_KEY_PREFIX = "session:server:";
    private static final Duration SESSION_TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, Object> chatRedisTemplate;

    /**
     * 사용자 세션 등록
     *
     * @param userId    사용자 ID
     * @param serverId  서버 인스턴스 ID
     * @param sessionId WebSocket 세션 ID
     */
    public void registerSession(Long userId, String serverId, String sessionId) {
        String sessionKey = SESSION_KEY_PREFIX + userId;

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("serverId", serverId);
        sessionData.put("sessionId", sessionId);
        sessionData.put("connectedAt", LocalDateTime.now().toString());

        chatRedisTemplate.opsForHash().putAll(sessionKey, sessionData);
        chatRedisTemplate.expire(sessionKey, SESSION_TTL);

        // 서버별 연결 사용자 목록에 추가
        String serverKey = SERVER_USERS_KEY_PREFIX + serverId + ":users";
        chatRedisTemplate.opsForSet().add(serverKey, userId);

        log.info("Session registered - userId: {}, serverId: {}, sessionId: {}", userId, serverId, sessionId);
    }

    /**
     * 사용자 세션 해제
     *
     * @param userId   사용자 ID
     * @param serverId 서버 인스턴스 ID
     */
    public void removeSession(Long userId, String serverId) {
        String sessionKey = SESSION_KEY_PREFIX + userId;
        chatRedisTemplate.delete(sessionKey);

        // 서버별 연결 사용자 목록에서 제거
        String serverKey = SERVER_USERS_KEY_PREFIX + serverId + ":users";
        chatRedisTemplate.opsForSet().remove(serverKey, userId);

        log.info("Session removed - userId: {}, serverId: {}", userId, serverId);
    }

    /**
     * 사용자가 연결된 서버 ID 조회
     *
     * @param userId 사용자 ID
     * @return 서버 ID (연결되어 있지 않으면 null)
     */
    public String getServerIdForUser(Long userId) {
        String sessionKey = SESSION_KEY_PREFIX + userId;
        Object serverId = chatRedisTemplate.opsForHash().get(sessionKey, "serverId");
        return serverId != null ? serverId.toString() : null;
    }

    /**
     * 사용자 연결 여부 확인
     *
     * @param userId 사용자 ID
     * @return true - 연결됨, false - 미연결
     */
    public boolean isUserConnected(Long userId) {
        String sessionKey = SESSION_KEY_PREFIX + userId;
        return Boolean.TRUE.equals(chatRedisTemplate.hasKey(sessionKey));
    }

    /**
     * 세션 TTL 갱신 (heartbeat)
     *
     * @param userId 사용자 ID
     */
    public void refreshSession(Long userId) {
        String sessionKey = SESSION_KEY_PREFIX + userId;
        chatRedisTemplate.expire(sessionKey, SESSION_TTL);
    }

    /**
     * 현재 활성 채팅방 설정
     *
     * @param userId 사용자 ID
     * @param roomId 채팅방 ID
     */
    public void setActiveRoom(Long userId, String roomId) {
        String sessionKey = SESSION_KEY_PREFIX + userId;
        chatRedisTemplate.opsForHash().put(sessionKey, "activeRoomId", roomId);
    }

    /**
     * 현재 활성 채팅방 조회
     *
     * @param userId 사용자 ID
     * @return 활성 채팅방 ID (없으면 null)
     */
    public String getActiveRoom(Long userId) {
        String sessionKey = SESSION_KEY_PREFIX + userId;
        Object roomId = chatRedisTemplate.opsForHash().get(sessionKey, "activeRoomId");
        return roomId != null ? roomId.toString() : null;
    }

    /**
     * 서버 종료 시 해당 서버의 모든 세션 정리
     *
     * @param serverId 서버 인스턴스 ID
     */
    public void removeAllSessionsForServer(String serverId) {
        String serverKey = SERVER_USERS_KEY_PREFIX + serverId + ":users";
        Set<Object> userIds = chatRedisTemplate.opsForSet().members(serverKey);

        if (userIds != null) {
            for (Object userId : userIds) {
                String sessionKey = SESSION_KEY_PREFIX + userId;
                chatRedisTemplate.delete(sessionKey);
            }
        }

        chatRedisTemplate.delete(serverKey);
        log.info("All sessions removed for server: {}", serverId);
    }
}
