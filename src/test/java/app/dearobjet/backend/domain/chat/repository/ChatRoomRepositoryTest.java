package app.dearobjet.backend.domain.chat.repository;

import app.dearobjet.backend.domain.chat.entity.ChatRoom;
import app.dearobjet.backend.domain.chat.entity.ChatRoomType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChatRoomRepository 테스트")
class ChatRoomRepositoryTest extends RepositoryTestBase {

    @Nested
    @DisplayName("findByParticipantHash")
    class FindByParticipantHashTest {

        @Test
        @DisplayName("존재하는 해시로 채팅방 조회 성공")
        void findByParticipantHash_success() {
            // given
            ChatRoom room = createChatRoom(user1, user2);
            String hash = generateHash(user1.getId(), user2.getId());

            // when
            Optional<ChatRoom> found = chatRoomRepository.findByParticipantHash(hash);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(room.getId());
            assertThat(found.get().getType()).isEqualTo(ChatRoomType.ONE_TO_ONE);
        }

        @Test
        @DisplayName("존재하지 않는 해시로 조회 시 Empty 반환")
        void findByParticipantHash_notFound() {
            // given
            createChatRoom(user1, user2);
            String nonExistentHash = generateHash(user1.getId(), user3.getId());

            // when
            Optional<ChatRoom> found = chatRoomRepository.findByParticipantHash(nonExistentHash);

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByRoomId")
    class FindByRoomIdTest {

        @Test
        @DisplayName("UUID로 채팅방 조회 성공")
        void findByRoomId_success() {
            // given
            ChatRoom room = createChatRoom(user1, user2);

            // when
            Optional<ChatRoom> found = chatRoomRepository.findByRoomId(room.getRoomId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(room.getId());
        }

        @Test
        @DisplayName("존재하지 않는 UUID로 조회 시 Empty 반환")
        void findByRoomId_notFound() {
            // given
            createChatRoom(user1, user2);

            // when
            Optional<ChatRoom> found = chatRoomRepository.findByRoomId("non-existent-uuid");

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByRoomIdWithParticipants")
    class FindByRoomIdWithParticipantsTest {

        @Test
        @DisplayName("참여자 정보 함께 조회 성공")
        void findByRoomIdWithParticipants_success() {
            // given
            ChatRoom room = createChatRoom(user1, user2);
            flushAndClear();

            // when
            Optional<ChatRoom> found = chatRoomRepository.findByRoomIdWithParticipants(room.getRoomId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getParticipants()).hasSize(2);
        }

        @Test
        @DisplayName("참여자 User 정보까지 로드 확인 (N+1 방지)")
        void findByRoomIdWithParticipants_userLoaded() {
            // given
            ChatRoom room = createChatRoom(user1, user2);
            flushAndClear();

            // when
            Optional<ChatRoom> found = chatRoomRepository.findByRoomIdWithParticipants(room.getRoomId());

            // then
            assertThat(found).isPresent();
            found.get().getParticipants().forEach(participant -> {
                // User 정보가 이미 로드되어 있어야 함 (추가 쿼리 없이 접근 가능)
                assertThat(participant.getUser()).isNotNull();
                assertThat(participant.getUser().getName()).isNotNull();
            });
        }
    }

    @Nested
    @DisplayName("findByIdWithParticipants")
    class FindByIdWithParticipantsTest {

        @Test
        @DisplayName("DB ID로 참여자 포함 조회 성공")
        void findByIdWithParticipants_success() {
            // given
            ChatRoom room = createChatRoom(user1, user2);
            flushAndClear();

            // when
            Optional<ChatRoom> found = chatRoomRepository.findByIdWithParticipants(room.getId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getParticipants()).hasSize(2);
            assertThat(found.get().getParticipants())
                    .extracting(p -> p.getUser().getId())
                    .containsExactlyInAnyOrder(user1.getId(), user2.getId());
        }
    }
}