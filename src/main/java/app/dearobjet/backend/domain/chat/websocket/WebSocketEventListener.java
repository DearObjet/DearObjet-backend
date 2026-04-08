package app.dearobjet.backend.domain.chat.websocket;

import app.dearobjet.backend.domain.chat.dto.UserPresenceDto;
import app.dearobjet.backend.domain.chat.service.redis.PresenceRedisService;
import app.dearobjet.backend.domain.chat.service.redis.SessionRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import app.dearobjet.backend.domain.chat.repository.ChatParticipantRepository;

import java.security.Principal;

/**
 * WebSocket 이벤트 리스너
 * 연결, 구독, 해제 이벤트 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SessionRedisService sessionRedisService;
    private final PresenceRedisService presenceRedisService;
    private final ChatParticipantRepository chatParticipantRepository;

    @Value("${spring.application.name:backend}")
    private String serverId;

    /**
     * WebSocket 연결 완료 이벤트
     */
    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Long userId = extractUserId(accessor);

        if (userId != null && sessionId != null) {
            // Redis에 세션 등록
            sessionRedisService.registerSession(userId, serverId, sessionId);
            // 온라인 상태 설정
            presenceRedisService.setPresence(userId, UserPresenceDto.Status.ONLINE);

            log.info("WebSocket connected - userId: {}, sessionId: {}", userId, sessionId);
        }
    }

    /**
     * WebSocket 연결 해제 이벤트
     */
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Long userId = extractUserId(accessor);

        if (userId != null) {
            // Redis에서 세션 제거
            sessionRedisService.removeSession(userId, serverId);
            // 오프라인 상태 설정
            presenceRedisService.setOffline(userId);

            // 참여 중인 모든 채팅방에서 presence 제거
            chatParticipantRepository.findMyParticipationsWithDetails(userId)
                    .forEach(p ->
                            presenceRedisService.leaveRoom(p.getChatRoom().getRoomId(), userId)
                    );
            log.info("WebSocket disconnected - userId: {}, sessionId: {}", userId, sessionId);
        }
    }

    /**
     * 채팅방 구독 이벤트
     */
    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        Long userId = extractUserId(accessor);

        if (userId != null && destination != null && destination.startsWith("/topic/chat/")) {
            // 채팅방 구독 시 해당 방에 입장 처리
            String roomId = extractRoomIdFromDestination(destination);
            if (roomId != null) {
                sessionRedisService.setActiveRoom(userId, roomId);
                log.debug("User {} subscribed to room {}", userId, roomId);
            }
        }
    }

    private Long extractUserId(StompHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            Object principalObj = auth.getPrincipal();
            if (principalObj instanceof Long) {
                return (Long) principalObj;
            }
        }
        return null;
    }

    private String extractRoomIdFromDestination(String destination) {
        // /topic/chat/{roomId} 또는 /topic/chat/{roomId}/typing 등에서 roomId 추출
        String[] parts = destination.split("/");
        if (parts.length >= 4) {
            return parts[3];
        }
        return null;
    }
}
