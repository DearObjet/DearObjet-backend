package app.dearobjet.backend.domain.chat.entity;

import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 채팅방 참여자 엔티티
 */
@Entity
@Table(name = "chat_participants",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_room_user",
                        columnNames = {"chat_room_id", "user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_user_room", columnList = "user_id, chat_room_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatParticipant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 참여 중인 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 참여 중인 채팅방
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    /**
     * 읽지 않은 메시지 수
     */
    @Column(name = "unread_count", nullable = false)
    private Integer unreadCount;

    /**
     * 채팅방 참여 시점
     */
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    /**
     * 마지막으로 메시지를 읽은 시점
     *
     * 읽음 표시 구현 방식:
     * 1. 사용자가 채팅방 진입 시 현재 시간으로 업데이트
     * 2. 메시지의 createdAt과 비교하여 읽음 여부 판단
     * 3. message.createdAt <= participant.lastReadAt → 읽음
     */
    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    // ====================================================================
    // 비즈니스 메서드
    // ====================================================================

    /**
     * 읽지 않은 메시지 수 증가
     * 새 메시지가 도착했을 때 호출
     */
    public void incrementUnreadCount() {
        if (this.unreadCount == null) {
            this.unreadCount = 0;
        }
        this.unreadCount++;
    }

    /**
     * 메시지 읽음 처리
     * 채팅방 진입 시 호출
     */
    public void markAsRead() {
        this.lastReadAt = LocalDateTime.now();
        this.unreadCount = 0;
    }

    /**
     * 읽지 않은 메시지 수 업데이트
     * 배치 작업 등에서 사용
     *
     * @param count 새로운 읽지 않은 메시지 수
     */
    public void updateUnreadCount(int count) {
        this.unreadCount = Math.max(0, count);  // 음수 방지
    }

    /**
     * 특정 시점 이후의 메시지를 읽음 처리
     *
     * @param readUntil 읽음 처리할 시점
     */
    public void markAsReadUntil(LocalDateTime readUntil) {
        this.lastReadAt = readUntil;
    }

    // ====================================================================
    // JPA Lifecycle Callbacks
    // ====================================================================

    /**
     * 엔티티 저장 전 호출
     * joinedAt 자동 설정
     */
    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
        if (unreadCount == null) {
            unreadCount = 0;
        }
    }

    // ====================================================================
    // 편의 메서드
    // ====================================================================

    /**
     * 특정 메시지를 읽었는지 확인
     *
     * @param messageCreatedAt 메시지 생성 시간
     * @return true - 읽음, false - 안 읽음
     */
    public boolean hasRead(LocalDateTime messageCreatedAt) {
        if (lastReadAt == null) {
            return false;
        }
        return !messageCreatedAt.isAfter(lastReadAt);
    }
}