package app.dearobjet.backend.domain.chat.service;

import app.dearobjet.backend.domain.chat.dto.ChatRoomListResponse;
import app.dearobjet.backend.domain.chat.dto.CreateChatRoomResponse;
import app.dearobjet.backend.domain.chat.entity.ChatParticipant;
import app.dearobjet.backend.domain.chat.entity.ChatRoom;
import app.dearobjet.backend.domain.chat.repository.ChatParticipantRepository;
import app.dearobjet.backend.domain.chat.repository.ChatRoomRepository;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ChatServiceTest {

    @Autowired ChatService chatService;
    @Autowired UserRepository userRepository;
    @Autowired ChatRoomRepository chatRoomRepository;
    @Autowired ChatParticipantRepository chatParticipantRepository;

    @Test
    @DisplayName("채팅방 목록 조회 시, 내 unreadCount 와 상대방 정보가 올바르게 매핑되어야 한다.")
    void getMyChatRoomsTest() {
        // given

        // 1. 유저 생성
        User me = userRepository.save(User.builder()
                .email("me@test.com")
                .name("나")
                .phoneNumber("010-1111-1111")
                .role("CUSTOMER")
                .profileImage("my.png")
                .build());

        User partner = userRepository.save(User.builder()
                .email("partner@test.com")
                .name("상대방")
                .phoneNumber("010-2222-2222")
                .role("ARTIST")
                .profileImage("partner.png")
                .build());

        // 2. 채팅방 생성
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.builder()
                .lastMessage("안녕")
                .lastModifiedAt(LocalDateTime.now())
                .build());

        // 3. 참여자 정보 생성
        // 나 (안 읽은 메시지 3개)
        chatParticipantRepository.save(ChatParticipant.builder()
                .chatRoom(chatRoom)
                .user(me)
                .unreadCount(3)
                .build());

        // 상대방 (안 읽은 메시지 0개)
        chatParticipantRepository.save(ChatParticipant.builder()
                .chatRoom(chatRoom)
                .user(partner)
                .unreadCount(0)
                .build());

        // when
        List<ChatRoomListResponse> result = chatService.getMyChatRooms(me);

        // then
        assertThat(result).hasSize(1);

        ChatRoomListResponse response = result.get(0);

        // 상대방 정보 검증
        assertThat(response.partnerId()).isEqualTo(partner.getUserId());
        assertThat(response.partnerNickname()).isEqualTo("상대방");
        assertThat(response.partnerProfileImage()).isEqualTo("partner.png");

        // 내 unreadCount(3)가 나와야 함 (상대방의 0이 아님)
        assertThat(response.unreadCount()).isEqualTo(3);

        // 메시지 검증
        assertThat(response.lastMessage()).isEqualTo("안녕");
    }

    @Test
    @DisplayName("채팅방 목록은 마지막 수정 시간(lastModifiedAt) 기준 최신순으로 정렬되어야 한다.")
    void getMyChatRoomsSortingTest() {
        // given
        User me = userRepository.save(User.builder().email("me@sort.com").name("나").phoneNumber("010-3333-3333").role("USER").build());
        User partnerOld = userRepository.save(User.builder().email("old@sort.com").name("과거").phoneNumber("010-4444-4444").role("USER").build());
        User partnerNew = userRepository.save(User.builder().email("new@sort.com").name("최신").phoneNumber("010-5555-5555").role("USER").build());

        // 과거 방 생성
        CreateChatRoomResponse oldResp = chatService.createChatRoom(me, partnerOld.getUserId());
        Long oldRoomId = oldResp.getChatRoomId();

        // 시간 강제 변경 (어제로)
        ChatRoom oldRoom = chatRoomRepository.findById(oldRoomId).get();
        ChatRoom updateOldRoom = ChatRoom.builder()
                .id(oldRoom.getId())
                .lastMessage("과거 메시지")
                .lastModifiedAt(LocalDateTime.now().minusDays(1))
                .build();
        chatRoomRepository.save(updateOldRoom);

        // 최신 방 생성
        CreateChatRoomResponse newResp = chatService.createChatRoom(me, partnerNew.getUserId());
        Long recentRoomId = newResp.getChatRoomId(); // DTO에서 ID 꺼내기

        // 시간/메시지 설정
        ChatRoom recentRoom = chatRoomRepository.findById(recentRoomId).get();
        ChatRoom updateRecentRoom = ChatRoom.builder()
                .id(recentRoom.getId())
                .lastMessage("최신 메시지")
                .lastModifiedAt(LocalDateTime.now())
                .build();
        chatRoomRepository.save(updateRecentRoom);

        // when
        List<ChatRoomListResponse> result = chatService.getMyChatRooms(me);

        // then
        assertThat(result).hasSize(2);

        // 최신 방(recentRoomId)이 0번째
        assertThat(result.get(0).chatRoomId()).isEqualTo(recentRoomId);
        assertThat(result.get(0).lastMessage()).isEqualTo("최신 메시지");

        // 과거 방(oldRoomId)이 1번째
        assertThat(result.get(1).chatRoomId()).isEqualTo(oldRoomId);
    }

    @Test
    @DisplayName("1:1 채팅방 생성 시, 없으면 새로 만들고 있으면 기존 방을 반환한다.")
    void createOrGetChatRoomTest() {
        // given
        User me = userRepository.save(User.builder().email("a@test.com").name("a").phoneNumber("010-1111-1111").role("USER").build());
        User partner = userRepository.save(User.builder().email("b@test.com").name("b").phoneNumber("010-2222-2222").role("USER").build());

        // when
        // 첫 번째 요청: 방이 없으므로 새로 생성되어야 함
        CreateChatRoomResponse response1 = chatService.createChatRoom(me, partner.getUserId());

        // 두 번째 요청: 이미 방이 있으므로 기존 방이 반환되어야 함
        CreateChatRoomResponse response2 = chatService.createChatRoom(me, partner.getUserId());

        // then
        // ID가 동일해야 함 (같은 방)
        assertThat(response1.getChatRoomId()).isEqualTo(response2.getChatRoomId());

        assertThat(response1.isNew()).isTrue();  // 첫 요청은 "새 방입니다"
        assertThat(response2.isNew()).isFalse(); // 두 번째는 "헌 방입니다"

        // DB에 저장된 방의 개수는 1개여야 함
        List<ChatRoom> allRooms = chatRoomRepository.findAll();
        assertThat(allRooms).hasSize(1);
    }
}