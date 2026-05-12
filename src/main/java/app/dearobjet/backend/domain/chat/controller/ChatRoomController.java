package app.dearobjet.backend.domain.chat.controller;

import app.dearobjet.backend.domain.chat.dto.ChatRoomResponse;
import app.dearobjet.backend.domain.chat.dto.CreateChatRoomRequest;
import app.dearobjet.backend.domain.chat.service.ChatRoomService;
import app.dearobjet.backend.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 채팅방 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    /**
     * 채팅방 생성 또는 기존 채팅방 조회
     *
     * POST /api/chat/rooms
     */
    @PostMapping
    public ApiResponse<ChatRoomResponse> createOrGetChatRoom(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @Valid @RequestBody CreateChatRoomRequest request) {

        ChatRoomResponse chatRoom = chatRoomService.createOrGetChatRoom(userId, request);
        return ApiResponse.of(chatRoom);
    }

    /**
     * 내 채팅방 목록 조회
     *
     * GET /api/chat/rooms
     */
    @GetMapping
    public ApiResponse<List<ChatRoomResponse>> getMyChatRooms(
            @AuthenticationPrincipal(expression = "userId") Long userId) {

        List<ChatRoomResponse> chatRooms = chatRoomService.getMyChatRooms(userId);
        return ApiResponse.of(chatRooms);
    }

    /**
     * 채팅방 상세 조회
     *
     * GET /api/chat/rooms/{roomId}
     */
    @GetMapping("/{roomId}")
    public ApiResponse<ChatRoomResponse> getChatRoom(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable String roomId) {

        ChatRoomResponse chatRoom = chatRoomService.getChatRoom(roomId, userId);
        return ApiResponse.of(chatRoom);
    }

    /**
     * 1:1 채팅방 생성 (상대방 ID로 바로 생성)
     *
     * POST /api/chat/rooms/direct/{partnerId}
     */
    @PostMapping("/direct/{partnerId}")
    public ApiResponse<ChatRoomResponse> createDirectChatRoom(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long partnerId) {

        CreateChatRoomRequest request = CreateChatRoomRequest.oneToOne(partnerId);
        ChatRoomResponse chatRoom = chatRoomService.createOrGetChatRoom(userId, request);
        return ApiResponse.of(chatRoom);
    }
}
