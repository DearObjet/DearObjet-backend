package app.dearobjet.backend.domain.chat.repository;

import app.dearobjet.backend.domain.chat.entity.*;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import app.dearobjet.backend.global.common.config.JpaConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Repository 테스트 베이스 클래스
 *
 * @DataJpaTest:
 * - JPA 관련 컴포넌트만 로드 (가벼운 테스트)
 * - 트랜잭션 자동 롤백 (테스트 격리)
 * - H2 인메모리 DB 사용
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
public abstract class RepositoryTestBase {

    @Autowired
    protected ChatRoomRepository chatRoomRepository;

    @Autowired
    protected ChatParticipantRepository chatParticipantRepository;

    @Autowired
    protected ChatMessageRepository chatMessageRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected EntityManager entityManager;

    protected User user1;
    protected User user2;
    protected User user3;

    /**
     * 각 테스트 실행 전 실행
     * 테스트 데이터 초기화
     */
    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        user1 = createUser("alice@example.com", "Alice", "010-1111-1111");
        user2 = createUser("bob@example.com", "Bob", "010-2222-2222");
        user3 = createUser("charlie@example.com", "Charlie", "010-3333-3333");
    }

    /**
     * 테스트 사용자 생성 헬퍼
     */
    protected User createUser(String email, String name, String phoneNumber) {
        User user = User.builder()
                .email(email)
                .name(name)
                .phoneNumber(phoneNumber)
                .role(Role.CUSTOMER)
                .userStatus(UserStatus.ACTIVE)
                .build();
        return userRepository.save(user);
    }

    /**
     * 채팅방 생성 헬퍼
     */
    protected ChatRoom createChatRoom(User user1, User user2) {
        ChatRoom room = ChatRoom.createOneToOne(user1, user2);
        return chatRoomRepository.save(room);
    }

    /**
     * participantHash 생성 헬퍼
     */
    protected String generateHash(Long id1, Long id2) {
        Long[] sorted = {id1, id2};
        Arrays.sort(sorted);
        String ids = sorted[0] + "," + sorted[1];
        return DigestUtils.md5DigestAsHex(ids.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 채팅 메시지 생성 헬퍼
     */
    protected ChatMessage createMessage(ChatRoom chatRoom, User sender, String content) {
        ChatMessage message = ChatMessage.builder()
                .roomId(chatRoom.getRoomId())
                .chatRoom(chatRoom)
                .sender(sender)
                .content(content)
                .messageType(MessageType.TEXT)
                .createdAt(LocalDateTime.now())
                .build();
        return chatMessageRepository.save(message);
    }

    /**
     * 특정 시간의 채팅 메시지 생성 헬퍼
     */
    protected ChatMessage createMessageAt(ChatRoom chatRoom, User sender, String content, LocalDateTime createdAt) {
        ChatMessage message = ChatMessage.builder()
                .roomId(chatRoom.getRoomId())
                .chatRoom(chatRoom)
                .sender(sender)
                .content(content)
                .messageType(MessageType.TEXT)
                .createdAt(createdAt)
                .build();
        return chatMessageRepository.save(message);
    }

    /**
     * 영속성 컨텍스트 초기화 (지연 로딩 테스트용)
     */
    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}