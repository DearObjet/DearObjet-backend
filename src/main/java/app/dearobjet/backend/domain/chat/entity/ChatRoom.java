package app.dearobjet.backend.domain.chat.entity;

import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long id;

    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    @Column(name = "last_message")
    private String lastMessage;

    // 방의 상태를 갱신하는 비즈니스 메서드
    public void updateLastMessage(String message, LocalDateTime now) {
        this.lastMessage = message;
        this.lastModifiedAt = now; // 방의 정렬 순서를 위해 시간 갱신
    }
}