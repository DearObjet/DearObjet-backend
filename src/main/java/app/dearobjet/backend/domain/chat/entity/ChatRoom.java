package app.dearobjet.backend.domain.chat.entity;

import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

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
    private Long chatRoomId;

    @Column(name = "last_modified_at")
    private java.time.LocalDateTime lastModifiedAt;

    @Column(name = "unread_count")
    private Integer unreadCount;

    @Column(name = "last_message")
    private String lastMessage;
}