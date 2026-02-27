package app.dearobjet.backend.domain.chat.dto;

import app.dearobjet.backend.domain.chat.entity.ChatMessage;
import app.dearobjet.backend.domain.chat.entity.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {

    private Long id;
    private String roomId;
    private Long senderId;
    private String senderName;
    private String senderProfileImage;
    private String content;
    private MessageType messageType;
    private LocalDateTime createdAt;
    private boolean isMyMessage;

    /**
     * 엔티티로부터 DTO 생성
     */
    public static ChatMessageResponse from(ChatMessage message, Long currentUserId) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .roomId(message.getRoomId())
                .senderId(message.getSender() != null ? message.getSender().getId() : null)
                .senderName(message.getSender() != null ? message.getSender().getName() : "시스템")
                .senderProfileImage(message.getSender() != null ? message.getSender().getProfileImage() : null)
                .content(message.getContent())
                .messageType(message.getMessageType())
                .createdAt(message.getCreatedAt())
                .isMyMessage(message.getSender() != null && message.getSender().getId().equals(currentUserId))
                .build();
    }

    /**
     * Redis 메시지로부터 DTO 생성
     */
    public static ChatMessageResponse from(RedisChatMessageDto redisMessage, Long currentUserId) {
        return ChatMessageResponse.builder()
                .id(redisMessage.getMessageId())
                .roomId(redisMessage.getRoomId())
                .senderId(redisMessage.getSenderId())
                .senderName(redisMessage.getSenderName())
                .content(redisMessage.getContent())
                .messageType(redisMessage.getMessageType())
                .createdAt(redisMessage.getTimestamp())
                .isMyMessage(redisMessage.getSenderId() != null && redisMessage.getSenderId().equals(currentUserId))
                .build();
    }
}
