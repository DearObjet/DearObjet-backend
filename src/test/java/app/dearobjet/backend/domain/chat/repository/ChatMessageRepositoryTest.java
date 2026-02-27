package app.dearobjet.backend.domain.chat.repository;

import app.dearobjet.backend.domain.chat.entity.ChatMessage;
import app.dearobjet.backend.domain.chat.entity.ChatRoom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChatMessageRepository 테스트")
class ChatMessageRepositoryTest extends RepositoryTestBase {

    @Nested
    @DisplayName("findByRoomIdOrderByCreatedAtDesc")
    class FindByRoomIdOrderByCreatedAtDescTest {

        @Test
        @DisplayName("채팅방 메시지 페이징 조회 성공")
        void findByRoomIdOrderByCreatedAtDesc_success() {
            // given
            ChatRoom room = createChatRoom(user1, user2);
            LocalDateTime baseTime = LocalDateTime.now();

            createMessageAt(room, user1, "메시지 1", baseTime.minusMinutes(2));
            createMessageAt(room, user2, "메시지 2", baseTime.minusMinutes(1));
            createMessageAt(room, user1, "메시지 3", baseTime);

            // when
            Page<ChatMessage> messages = chatMessageRepository
                    .findByRoomIdOrderByCreatedAtDesc(room.getRoomId(), PageRequest.of(0, 10));

            // then
            assertThat(messages.getContent()).hasSize(3);
            assertThat(messages.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("최신순 정렬 확인")
        void findByRoomIdOrderByCreatedAtDesc_orderedByCreatedAtDesc() {
            // given
            ChatRoom room = createChatRoom(user1, user2);
            LocalDateTime baseTime = LocalDateTime.now();

            ChatMessage msg1 = createMessageAt(room, user1, "첫 번째", baseTime.minusMinutes(2));
            ChatMessage msg2 = createMessageAt(room, user2, "두 번째", baseTime.minusMinutes(1));
            ChatMessage msg3 = createMessageAt(room, user1, "세 번째", baseTime);

            // when
            Page<ChatMessage> messages = chatMessageRepository
                    .findByRoomIdOrderByCreatedAtDesc(room.getRoomId(), PageRequest.of(0, 10));

            // then
            List<ChatMessage> content = messages.getContent();
            assertThat(content.get(0).getContent()).isEqualTo("세 번째");
            assertThat(content.get(1).getContent()).isEqualTo("두 번째");
            assertThat(content.get(2).getContent()).isEqualTo("첫 번째");
        }

        @Test
        @DisplayName("페이징 동작 확인")
        void findByRoomIdOrderByCreatedAtDesc_pagination() {
            // given
            ChatRoom room = createChatRoom(user1, user2);
            LocalDateTime baseTime = LocalDateTime.now();

            for (int i = 0; i < 5; i++) {
                createMessageAt(room, user1, "메시지 " + i, baseTime.minusMinutes(5 - i));
            }

            // when
            Page<ChatMessage> firstPage = chatMessageRepository
                    .findByRoomIdOrderByCreatedAtDesc(room.getRoomId(), PageRequest.of(0, 2));
            Page<ChatMessage> secondPage = chatMessageRepository
                    .findByRoomIdOrderByCreatedAtDesc(room.getRoomId(), PageRequest.of(1, 2));

            // then
            assertThat(firstPage.getContent()).hasSize(2);
            assertThat(secondPage.getContent()).hasSize(2);
            assertThat(firstPage.getTotalElements()).isEqualTo(5);
            assertThat(firstPage.getTotalPages()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("findByRoomIdAndCreatedAtAfter")
    class FindByRoomIdAndCreatedAtAfterTest {

        @Test
        @DisplayName("특정 시점 이후 메시지 조회 성공")
        void findByRoomIdAndCreatedAtAfter_success() {
            // given
            ChatRoom room = createChatRoom(user1, user2);
            LocalDateTime baseTime = LocalDateTime.now();
            LocalDateTime cutoffTime = baseTime.minusMinutes(1).minusSeconds(30);

            createMessageAt(room, user1, "이전 메시지", baseTime.minusMinutes(2));
            ChatMessage msg2 = createMessageAt(room, user2, "이후 메시지 1", baseTime.minusMinutes(1));
            ChatMessage msg3 = createMessageAt(room, user1, "이후 메시지 2", baseTime);

            // when
            List<ChatMessage> messages = chatMessageRepository
                    .findByRoomIdAndCreatedAtAfter(room.getRoomId(), cutoffTime);

            // then
            assertThat(messages).hasSize(2);
            assertThat(messages)
                    .extracting(ChatMessage::getContent)
                    .containsExactly("이후 메시지 1", "이후 메시지 2");
        }

        @Test
        @DisplayName("해당 시점 이후 메시지 없으면 빈 리스트 반환")
        void findByRoomIdAndCreatedAtAfter_empty() {
            // given
            ChatRoom room = createChatRoom(user1, user2);
            LocalDateTime baseTime = LocalDateTime.now();

            createMessageAt(room, user1, "과거 메시지", baseTime.minusHours(1));

            // when
            List<ChatMessage> messages = chatMessageRepository
                    .findByRoomIdAndCreatedAtAfter(room.getRoomId(), baseTime);

            // then
            assertThat(messages).isEmpty();
        }
    }

    @Nested
    @DisplayName("countByRoomIdAndCreatedAtAfter")
    class CountByRoomIdAndCreatedAtAfterTest {

        @Test
        @DisplayName("특정 시점 이후 메시지 수 계산 성공")
        void countByRoomIdAndCreatedAtAfter_success() {
            // given
            ChatRoom room = createChatRoom(user1, user2);
            LocalDateTime baseTime = LocalDateTime.now();
            LocalDateTime cutoffTime = baseTime.minusMinutes(1).minusSeconds(30);

            createMessageAt(room, user1, "이전 메시지", baseTime.minusMinutes(2));
            createMessageAt(room, user2, "이후 메시지 1", baseTime.minusMinutes(1));
            createMessageAt(room, user1, "이후 메시지 2", baseTime);

            // when
            long count = chatMessageRepository
                    .countByRoomIdAndCreatedAtAfter(room.getRoomId(), cutoffTime);

            // then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("해당 시점 이후 메시지 없으면 0 반환")
        void countByRoomIdAndCreatedAtAfter_zero() {
            // given
            ChatRoom room = createChatRoom(user1, user2);
            LocalDateTime baseTime = LocalDateTime.now();

            createMessageAt(room, user1, "과거 메시지", baseTime.minusHours(1));

            // when
            long count = chatMessageRepository
                    .countByRoomIdAndCreatedAtAfter(room.getRoomId(), baseTime);

            // then
            assertThat(count).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("커서 기반 조회 (id)")
    class CursorBasedQueryTest {

        @Test
        @DisplayName("findByRoomIdOrderByIdDesc - 최신 메시지부터 조회")
        void findByRoomIdOrderByIdDesc_success() {
            // given
            ChatRoom room = createChatRoom(user1, user2);
            createMessage(room, user1, "m1");
            createMessage(room, user2, "m2");
            createMessage(room, user1, "m3");

            // when
            Page<ChatMessage> page = chatMessageRepository
                    .findByRoomIdOrderByIdDesc(room.getRoomId(), PageRequest.of(0, 10));

            // then
            assertThat(page.getContent()).hasSize(3);
            assertThat(page.getContent().get(0).getContent()).isEqualTo("m3");
            assertThat(page.getContent().get(1).getContent()).isEqualTo("m2");
            assertThat(page.getContent().get(2).getContent()).isEqualTo("m1");
        }

        @Test
        @DisplayName("findByRoomIdAndIdGreaterThanOrderByIdAsc - after id 이후 메시지 조회")
        void findByRoomIdAndIdGreaterThanOrderByIdAsc_success() {
            // given
            ChatRoom room = createChatRoom(user1, user2);
            ChatMessage m1 = createMessage(room, user1, "m1");
            ChatMessage m2 = createMessage(room, user2, "m2");
            ChatMessage m3 = createMessage(room, user1, "m3");

            // when
            Page<ChatMessage> page = chatMessageRepository
                    .findByRoomIdAndIdGreaterThanOrderByIdAsc(room.getRoomId(), m1.getId(), PageRequest.of(0, 10));

            // then
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getContent().get(0).getId()).isEqualTo(m2.getId());
            assertThat(page.getContent().get(1).getId()).isEqualTo(m3.getId());
        }

        @Test
        @DisplayName("findByRoomIdAndIdLessThanOrderByIdDesc - before id 이전 메시지 조회")
        void findByRoomIdAndIdLessThanOrderByIdDesc_success() {
            // given
            ChatRoom room = createChatRoom(user1, user2);
            ChatMessage m1 = createMessage(room, user1, "m1");
            ChatMessage m2 = createMessage(room, user2, "m2");
            ChatMessage m3 = createMessage(room, user1, "m3");

            // when
            Page<ChatMessage> page = chatMessageRepository
                    .findByRoomIdAndIdLessThanOrderByIdDesc(room.getRoomId(), m3.getId(), PageRequest.of(0, 10));

            // then
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getContent().get(0).getId()).isEqualTo(m2.getId());
            assertThat(page.getContent().get(1).getId()).isEqualTo(m1.getId());
        }
    }
}
