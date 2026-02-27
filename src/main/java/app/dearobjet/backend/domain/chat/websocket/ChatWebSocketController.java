package app.dearobjet.backend.domain.chat.websocket;

import app.dearobjet.backend.domain.chat.dto.RedisChatMessageDto;
import app.dearobjet.backend.domain.chat.dto.SendMessageRequest;
import app.dearobjet.backend.domain.chat.dto.TypingIndicatorDto;
import app.dearobjet.backend.domain.chat.repository.ChatParticipantRepository;
import app.dearobjet.backend.domain.chat.service.ChatMessageService;
import app.dearobjet.backend.domain.chat.service.redis.ChatMessagePublisher;
import app.dearobjet.backend.domain.chat.service.redis.PresenceRedisService;
import app.dearobjet.backend.global.exception.BusinessException;
import app.dearobjet.backend.global.exception.ErrorCode;
import app.dearobjet.backend.global.exception.InvalidInputException;
import app.dearobjet.backend.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket STOMP 메시지 컨트롤러
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final ChatMessagePublisher messagePublisher;
    private final PresenceRedisService presenceRedisService;
    private final ChatParticipantRepository chatParticipantRepository;

    /**
     * WebSocket 메시지 처리 중 발생한 예외를 클라이언트에 전달
     */
    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public Map<String, String> handleException(Exception e) {
        log.warn("WebSocket message error: {}", e.getMessage());
        String code = (e instanceof BusinessException be) ? be.getErrorCode().getCode() : "C004";
        return Map.of(
                "code", code,
                "message", e.getMessage()
        );
    }

    /**
     * 메시지 전송
     * 클라이언트 발행: /app/chat/{roomId}/send
     * 구독 경로: /topic/chat/{roomId}
     */
    @MessageMapping("/chat/{roomId}/send")
    public void sendMessage(
            @DestinationVariable String roomId,
            @Payload SendMessageRequest request,
            Principal principal) {

        Long userId = extractUserIdOrThrow(principal);

        log.debug("Message received - roomId: {}, userId: {}, type: {}",
                roomId, userId, request.getMessageType());

        // 메시지 저장 및 브로드캐스트 (참여자 검증은 서비스 레이어에서 수행)
        chatMessageService.sendMessage(userId, roomId, request);
    }

    /**
     * 타이핑 상태 전송
     * 클라이언트 발행: /app/chat/{roomId}/typing
     * 구독 경로: /topic/chat/{roomId}/typing
     */
    @MessageMapping("/chat/{roomId}/typing")
    public void sendTyping(
            @DestinationVariable String roomId,
            @Payload TypingIndicatorDto typing,
            Principal principal) {

        Long userId = extractUserIdOrThrow(principal);
        validateParticipant(roomId, userId);

        // 타이핑 이벤트를 Redis Pub/Sub으로 브로드캐스트
        RedisChatMessageDto typingEvent = RedisChatMessageDto.ofTyping(roomId, userId, typing.getUserName());
        messagePublisher.publishToRoom(typingEvent);

        log.debug("Typing event - roomId: {}, userId: {}", roomId, userId);
    }

    /**
     * 읽음 처리
     * 클라이언트 발행: /app/chat/{roomId}/read
     * 구독 경로: /topic/chat/{roomId}/read
     */
    @MessageMapping("/chat/{roomId}/read")
    public void markAsRead(
            @DestinationVariable String roomId,
            Principal principal) {

        Long userId = extractUserIdOrThrow(principal);

        // 읽음 처리 (서비스 레이어에서 참여자 검증 + Redis + DB 동기화)
        chatMessageService.markAsRead(userId, roomId);

        log.debug("Read event - roomId: {}, userId: {}", roomId, userId);
    }

    /**
     * 채팅방 입장
     * 클라이언트 발행: /app/chat/{roomId}/join
     */
    @MessageMapping("/chat/{roomId}/join")
    public void joinRoom(
            @DestinationVariable String roomId,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor) {

        Long userId = extractUserIdOrThrow(principal);
        validateParticipant(roomId, userId);

        presenceRedisService.joinRoom(roomId, userId);
        log.debug("User {} joined room {}", userId, roomId);
    }

    /**
     * 채팅방 퇴장
     * 클라이언트 발행: /app/chat/{roomId}/leave
     */
    @MessageMapping("/chat/{roomId}/leave")
    public void leaveRoom(
            @DestinationVariable String roomId,
            Principal principal) {

        Long userId = extractUserIdOrThrow(principal);
        validateParticipant(roomId, userId);

        presenceRedisService.leaveRoom(roomId, userId);
        log.debug("User {} left room {}", userId, roomId);
    }

    /**
     * Principal에서 userId 추출, 없으면 UnauthorizedException
     */
    private Long extractUserIdOrThrow(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            Object principalObj = auth.getPrincipal();
            if (principalObj instanceof Long userId) {
                return userId;
            }
        }
        throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
    }

    /**
     * 채팅방 참여자 검증
     */
    private void validateParticipant(String roomId, Long userId) {
        chatParticipantRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new InvalidInputException(ErrorCode.NOT_CHAT_PARTICIPANT));
    }
}
