package app.dearobjet.backend.domain.chat.entity;

import app.dearobjet.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 엔티티
 */
@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_room_created", columnList = "room_id, created_at DESC")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_messages_id")
    private Long id;

    /**
     * 채팅방 외부 식별자 (UUID)
     */
    @Column(name = "room_id", nullable = false, length = 36)
    private String roomId;

    /**
     * 메시지 타입 (TEXT, IMAGE, FILE, SYSTEM)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private MessageType messageType;

    /**
     * 메시지 내용
     * - TEXT: 텍스트 메시지
     * - IMAGE: 이미지 URL
     * - FILE: 파일 URL
     * - SYSTEM: 시스템 메시지 ("OOO님이 입장하였습니다" 등)
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 메시지 생성 시간
     * 정렬 및 읽음 처리에 사용
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 채팅방 (FK)
     * 주로 참조 무결성을 위해 유지
     * 조회 시에는 roomId 사용
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    /**
     * 메시지 발신자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User sender;

    // ====================================================================
    // 팩토리 메서드
    // ====================================================================

    /**
     * 메시지 생성
     *
     * @param roomId 채팅방 UUID
     * @param sender 발신자
     * @param content 메시지 내용
     * @param type 메시지 타입
     * @return 새로 생성된 메시지
     */
    public static ChatMessage create(String roomId, User sender, String content, MessageType type) {
        return ChatMessage.builder()
                .roomId(roomId)
                .sender(sender)
                .content(content)
                .messageType(type)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 텍스트 메시지 생성 (편의 메서드)
     */
    public static ChatMessage createTextMessage(String roomId, User sender, String content) {
        return create(roomId, sender, content, MessageType.TEXT);
    }

    /**
     * 시스템 메시지 생성 (편의 메서드)
     * 예: "홍길동님이 입장하였습니다"
     */
    public static ChatMessage createSystemMessage(String roomId, String content) {
        return ChatMessage.builder()
                .roomId(roomId)
                .sender(null)  // 시스템 메시지는 발신자 없음
                .content(content)
                .messageType(MessageType.SYSTEM)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ====================================================================
    // JPA Lifecycle Callbacks
    // ====================================================================

    /**
     * 엔티티 저장 전 호출
     * createdAt 자동 설정
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (messageType == null) {
            messageType = MessageType.TEXT;
        }
    }

    // ====================================================================
    // 비즈니스 메서드
    // ====================================================================

    /**
     * 특정 사용자가 이 메시지를 읽었는지 확인
     *
     * @param participant 확인할 참여자
     * @return true - 읽음, false - 안 읽음
     */
    public boolean isReadBy(ChatParticipant participant) {
        return participant.hasRead(this.createdAt);
    }

    /**
     * 시스템 메시지인지 확인
     */
    public boolean isSystemMessage() {
        return this.messageType == MessageType.SYSTEM;
    }

    /**
     * 텍스트 메시지인지 확인
     */
    public boolean isTextMessage() {
        return this.messageType == MessageType.TEXT;
    }
}