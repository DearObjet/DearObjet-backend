package app.dearobjet.backend.domain.chat.service;

import app.dearobjet.backend.domain.chat.dto.ChatRoomListResponse;
import app.dearobjet.backend.domain.chat.dto.CreateChatRoomResponse;
import app.dearobjet.backend.domain.chat.entity.ChatMessage;
import app.dearobjet.backend.domain.chat.entity.ChatParticipant;
import app.dearobjet.backend.domain.chat.entity.ChatRoom;
import app.dearobjet.backend.domain.chat.repository.ChatParticipantRepository;
import app.dearobjet.backend.domain.chat.repository.ChatRoomRepository;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    public List<ChatRoomListResponse> getMyChatRooms(User user) {
        // 내 참여 정보 리스트 조회
        List<ChatParticipant> myParticipations = chatParticipantRepository.findAllMyself(user.getUserId());

        if (myParticipations.isEmpty()) {
            return List.of();
        }

        // 채팅방 ID 목록 추출
        List<Long> chatRoomIds = myParticipations.stream()
                .map(cp -> cp.getChatRoom().getId())
                .toList();

        // 해당 채팅방들의 상대방 정보 조회
        List<ChatParticipant> partnerParticipations = chatParticipantRepository.findAllPartners(chatRoomIds, user.getUserId());

        // 상대방 정보를 Map으로 변환 (Key: ChatRoomId, Value: Partner User)
        Map<Long, User> partnerMap = partnerParticipations.stream()
                .collect(Collectors.toMap(
                        cp -> cp.getChatRoom().getId(),
                        ChatParticipant::getUser
                ));

        // 내 참여 정보와 상대방 정보를 조합하여 응답 생성
        return myParticipations.stream()
                .map(myCp -> {
                    Long roomId = myCp.getChatRoom().getId();
                    User partner = partnerMap.get(roomId);

                    return ChatRoomListResponse.of(
                            myCp.getChatRoom(),
                            partner,
                            myCp.getUnreadCount() // 내 안 읽은 메시지 수 사용
                    );
                })
                .toList();
    }

    @Transactional
    public CreateChatRoomResponse createChatRoom(User me, Long partnerId) {
        // 이미 둘 사이에 방이 있는지 확인
        Optional<ChatRoom> existingRoom = chatParticipantRepository.findExistingChatRoom(me.getUserId(), partnerId);

        // 있다면 그 방의 ID를 바로 반환
        if (existingRoom.isPresent()) {
            return new CreateChatRoomResponse(existingRoom.get().getId(), false);
        }

        // 없다면 새로 생성
        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new EntityNotFoundException("상대방을 찾을 수 없습니다."));

        // 방 생성
        ChatRoom newRoom = chatRoomRepository.save(ChatRoom.builder()
                .lastModifiedAt(LocalDateTime.now())
                .lastMessage("")
                .build());

        // 참여자 연결 (나, 상대방)
        chatParticipantRepository.save(ChatParticipant.builder()
                .chatRoom(newRoom).user(me).unreadCount(0).build());
        chatParticipantRepository.save(ChatParticipant.builder()
                .chatRoom(newRoom).user(partner).unreadCount(0).build());

        return new CreateChatRoomResponse(newRoom.getId(), true);
    }

}