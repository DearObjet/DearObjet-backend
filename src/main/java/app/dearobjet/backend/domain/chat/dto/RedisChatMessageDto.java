package app.dearobjet.backend.domain.chat.dto;

import app.dearobjet.backend.domain.chat.entity.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Redis Pub/Sub을 통해 전달되는 채팅 메시지
 * 스케일 아웃 환경에서 다중 서버 간 메시지 브로드캐스트용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedisChatMessageDto {

    /**
     * 메시지 이벤트 타입
     */
    public enum EventType {
        MESSAGE,    // 일반 메시지
        TYPING,     // 타이핑 중
        READ,       // 읽음 처리
        JOIN,       // 채팅방 입장
        LEAVE       // 채팅방 퇴장
    }

    private EventType eventType;
    private String roomId;
    private Long messageId;
    private Long senderId;
    private String senderName;
    private String senderProfileImage;
    private String content;
    private MessageType messageType;
    private LocalDateTime timestamp;

    /**
     * 일반 메시지 생성
     */
    public static RedisChatMessageDto ofMessage(String roomId, Long messageId, Long senderId,
                                              String senderName, String senderProfileImage,
                                              String content,
                                              MessageType messageType, LocalDateTime timestamp) {
        return RedisChatMessageDto.builder()
                .eventType(EventType.MESSAGE)
                .roomId(roomId)
                .messageId(messageId)
                .senderId(senderId)
                .senderName(senderName)
                .senderProfileImage(senderProfileImage)
                .content(content)
                .messageType(messageType)
                .timestamp(timestamp)
                .build();
    }

    /**
     * 타이핑 이벤트 생성
     */
    public static RedisChatMessageDto ofTyping(String roomId, Long senderId, String senderName) {
        return RedisChatMessageDto.builder()
                .eventType(EventType.TYPING)
                .roomId(roomId)
                .senderId(senderId)
                .senderName(senderName)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 읽음 이벤트 생성
     */
    public static RedisChatMessageDto ofRead(String roomId, Long senderId, LocalDateTime readAt) {
        return RedisChatMessageDto.builder()
                .eventType(EventType.READ)
                .roomId(roomId)
                .senderId(senderId)
                .timestamp(readAt)
                .build();
    }
}
