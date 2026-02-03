package app.dearobjet.backend.domain.chat.entity;

import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 채팅방 엔티티
 */
@Entity
@Table(name = "chat_room", indexes = {
        @Index(name = "idx_participant_hash", columnList = "participant_hash", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long id;

    /**
     * 외부 노출용 채팅방 식별자 (UUID)
     * DB ID 대신 이 값을 API에서 사용
     */
    @Column(name = "room_id", unique = true, nullable = false, length = 36)
    private String roomId;

    /**
     * 채팅방 타입 (1:1 또는 그룹)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ChatRoomType type;

    /**
     * 참여자 해시값 (중복 방지용)
     * UNIQUE 제약조건으로 DB 레벨에서 중복 생성 방지
     */
    @Column(name = "participant_hash", unique = true, nullable = false, length = 64)
    private String participantHash;

    /**
     * 마지막 메시지 내용 (비정규화 - 조회 성능 최적화)
     */
    @Column(name = "last_message", columnDefinition = "TEXT")
    private String lastMessage;

    /**
     * 채팅방 참여자 목록
     * Cascade: 채팅방 삭제 시 참여자 정보도 함께 삭제
     */
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatParticipant> participants = new ArrayList<>();

    // ====================================================================
    // 팩토리 메서드
    // ====================================================================

    /**
     * 1:1 채팅방 생성
     *
     * @param user1 참여자 1
     * @param user2 참여자 2
     * @return 새로 생성된 채팅방
     * @throws IllegalArgumentException 자기 자신과 채팅방 생성 시도 시
     */
    public static ChatRoom createOneToOne(User user1, User user2) {
        validateUsers(user1, user2);

        ChatRoom room = new ChatRoom();
        room.roomId = UUID.randomUUID().toString();
        room.type = ChatRoomType.ONE_TO_ONE;
        room.participantHash = generateParticipantHash(user1.getUserId(), user2.getUserId());
        room.lastMessage = "";

        // 참여자 추가 (양방향 연관관계 설정)
        room.addParticipant(user1);
        room.addParticipant(user2);

        return room;
    }

    /**
     * 그룹 채팅방 생성
     *
     * @param users 참여자 목록 (3명 이상)
     * @return 새로 생성된 그룹 채팅방
     */
    public static ChatRoom createGroup(List<User> users) {
        if (users.size() < 3) {
            throw new IllegalArgumentException("그룹 채팅은 최소 3명 이상이어야 합니다.");
        }

        ChatRoom room = new ChatRoom().builder()
                .roomId(UUID.randomUUID().toString())
                .type(ChatRoomType.GROUP)
                .participantHash(generateParticipantHash(users.stream()
                        .map(User::getUserId)
                        .toArray(Long[]::new)))
                .lastMessage("")
                .build();

        users.forEach(room::addParticipant);

        return room;
    }

    // ====================================================================
    // 비즈니스 메서드
    // ====================================================================

    /**
     * 마지막 메시지 업데이트
     *
     * @param message 마지막 메시지 내용
     */
    public void updateLastMessage(String message) {
        this.lastMessage = message;
    }

    /**
     * 특정 사용자를 제외한 모든 참여자의 읽지 않은 메시지 수 증가
     *
     * @param senderUserId 메시지 발신자 ID (이 사용자는 제외)
     */
    public void incrementUnreadCount(Long senderUserId) {
        participants.stream()
                .filter(p -> !p.getUser().getUserId().equals(senderUserId))
                .forEach(ChatParticipant::incrementUnreadCount);
    }

    /**
     * 참여자 추가 (내부 사용)
     */
    private void addParticipant(User user) {
        ChatParticipant participant = ChatParticipant.builder()
                .chatRoom(this)
                .user(user)
                .unreadCount(0)
                .build();
        this.participants.add(participant);
    }

    // ====================================================================
    // 헬퍼 메서드
    // ====================================================================

    /**
     * 사용자 검증
     */
    private static void validateUsers(User user1, User user2) {
        if (user1 == null || user2 == null) {
            throw new IllegalArgumentException("사용자 정보는 필수입니다.");
        }
        if (user1.getUserId().equals(user2.getUserId())) {
            throw new IllegalArgumentException("자기 자신과는 채팅방을 만들 수 없습니다.");
        }
    }

    /**
     * 참여자 해시 생성
     *
     * @param userIds 참여자 ID 배열
     * @return MD5 해시값
     */
    private static String generateParticipantHash(Long... userIds) {
        String sortedIds = Arrays.stream(userIds)
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        return DigestUtils.md5DigestAsHex(sortedIds.getBytes(StandardCharsets.UTF_8));
    }
}