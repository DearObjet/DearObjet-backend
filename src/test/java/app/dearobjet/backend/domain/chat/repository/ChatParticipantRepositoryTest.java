package app.dearobjet.backend.domain.chat.repository;

import app.dearobjet.backend.domain.chat.entity.ChatParticipant;
import app.dearobjet.backend.domain.chat.entity.ChatRoom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChatParticipantRepository 테스트")
class ChatParticipantRepositoryTest extends RepositoryTestBase {

    @Nested
    @DisplayName("findMyParticipationsWithDetails")
    class FindMyParticipationsWithDetailsTest {

        @Test
        @DisplayName("사용자의 모든 참여 채팅방 목록 조회")
        void findMyParticipationsWithDetails_success() {
            // given
            ChatRoom room1 = createChatRoom(user1, user2);
            ChatRoom room2 = createChatRoom(user1, user3);
            flushAndClear();

            // when
            List<ChatParticipant> participations = chatParticipantRepository
                    .findMyParticipationsWithDetails(user1.getId());

            // then
            assertThat(participations).hasSize(2);
            assertThat(participations)
                    .extracting(p -> p.getChatRoom().getId())
                    .containsExactlyInAnyOrder(room1.getId(), room2.getId());
        }

        @Test
        @DisplayName("참여 채팅방 없는 경우 빈 리스트 반환")
        void findMyParticipationsWithDetails_empty() {
            // given
            createChatRoom(user1, user2);
            flushAndClear();

            // when
            List<ChatParticipant> participations = chatParticipantRepository
                    .findMyParticipationsWithDetails(user3.getId());

            // then
            assertThat(participations).isEmpty();
        }

        @Test
        @DisplayName("updatedAt 기준 내림차순 정렬 확인")
        void findMyParticipationsWithDetails_orderedByUpdatedAt() {
            // given
            ChatRoom room1 = createChatRoom(user1, user2);
            ChatRoom room2 = createChatRoom(user1, user3);

            // room2의 lastMessage 업데이트로 updatedAt 갱신
            room2.updateLastMessage("새 메시지");
            chatRoomRepository.save(room2);
            flushAndClear();

            // when
            List<ChatParticipant> participations = chatParticipantRepository
                    .findMyParticipationsWithDetails(user1.getId());

            // then
            assertThat(participations).hasSize(2);
            // 최신 업데이트된 room2가 먼저 나와야 함
            assertThat(participations.get(0).getChatRoom().getId()).isEqualTo(room2.getId());
        }
    }

    @Nested
    @DisplayName("findPartnerInRoom")
    class FindPartnerInRoomTest {

        @Test
        @DisplayName("1:1 채팅에서 상대방 조회 성공")
        void findPartnerInRoom_success() {
            // given
            ChatRoom room = createChatRoom(user1, user2);
            flushAndClear();

            // when
            Optional<ChatParticipant> partner = chatParticipantRepository
                    .findPartnerInRoom(room.getId(), user1.getId());

            // then
            assertThat(partner).isPresent();
            assertThat(partner.get().getUser().getId()).isEqualTo(user2.getId());
        }

        @Test
        @DisplayName("존재하지 않는 채팅방에서 상대방 조회 시 Empty 반환")
        void findPartnerInRoom_notFound() {
            // given
            createChatRoom(user1, user2);

            // when
            Optional<ChatParticipant> partner = chatParticipantRepository
                    .findPartnerInRoom(999L, user1.getId());

            // then
            assertThat(partner).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllByRoomId")
    class FindAllByRoomIdTest {

        @Test
        @DisplayName("채팅방의 모든 참여자 조회 성공")
        void findAllByRoomId_success() {
            // given
            ChatRoom room = createChatRoom(user1, user2);
            flushAndClear();

            // when
            List<ChatParticipant> participants = chatParticipantRepository
                    .findAllByRoomId(room.getId());

            // then
            assertThat(participants).hasSize(2);
            assertThat(participants)
                    .extracting(p -> p.getUser().getId())
                    .containsExactlyInAnyOrder(user1.getId(), user2.getId());
        }
    }

    @Nested
    @DisplayName("findByChatRoomIdAndUserId")
    class FindByChatRoomIdAndUserIdTest {

        @Test
        @DisplayName("채팅방ID + 유저ID로 참여자 조회 성공")
        void findByChatRoomIdAndUserId_success() {
            // given
            ChatRoom room = createChatRoom(user1, user2);

            // when
            Optional<ChatParticipant> participant = chatParticipantRepository
                    .findByChatRoomIdAndUserId(room.getId(), user1.getId());

            // then
            assertThat(participant).isPresent();
            assertThat(participant.get().getUser().getId()).isEqualTo(user1.getId());
            assertThat(participant.get().getChatRoom().getId()).isEqualTo(room.getId());
        }

        @Test
        @DisplayName("참여하지 않은 사용자로 조회 시 Empty 반환")
        void findByChatRoomIdAndUserId_notFound() {
            // given
            ChatRoom room = createChatRoom(user1, user2);

            // when
            Optional<ChatParticipant> participant = chatParticipantRepository
                    .findByChatRoomIdAndUserId(room.getId(), user3.getId());

            // then
            assertThat(participant).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByRoomIdAndUserId")
    class FindByRoomIdAndUserIdTest {

        @Test
        @DisplayName("UUID + 유저ID로 참여자 조회 성공")
        void findByRoomIdAndUserId_success() {
            // given
            ChatRoom room = createChatRoom(user1, user2);

            // when
            Optional<ChatParticipant> participant = chatParticipantRepository
                    .findByRoomIdAndUserId(room.getRoomId(), user1.getId());

            // then
            assertThat(participant).isPresent();
            assertThat(participant.get().getUser().getId()).isEqualTo(user1.getId());
        }
    }
}