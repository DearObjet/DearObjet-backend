package app.dearobjet.backend.domain.chat.controller;

import app.dearobjet.backend.domain.chat.dto.ChatMessageResponse;
import app.dearobjet.backend.domain.chat.dto.ChatMessageCursorResponse;
import app.dearobjet.backend.domain.chat.dto.SendMessageRequest;
import app.dearobjet.backend.domain.chat.service.ChatMessageService;
import app.dearobjet.backend.domain.chat.service.ChatParticipantService;
import app.dearobjet.backend.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 채팅 메시지 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final ChatParticipantService chatParticipantService;

    /**
     * 메시지 전송 (REST API 방식)
     * WebSocket을 사용할 수 없는 환경에서 폴백으로 사용
     *
     * POST /api/chat/rooms/{roomId}/messages
     */
    @PostMapping("/rooms/{roomId}/messages")
    public ApiResponse<ChatMessageResponse> sendMessage(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable String roomId,
            @Valid @RequestBody SendMessageRequest request) {

        ChatMessageResponse message = chatMessageService.sendMessage(userId, roomId, request);
        return ApiResponse.of(message);
    }

    /**
     * 최신 메시지 N개 조회 (커서 초기화)
     *
     * GET /api/chat/rooms/{roomId}/messages/latest?limit=50
     */
    @GetMapping("/rooms/{roomId}/messages/latest")
    public ApiResponse<ChatMessageCursorResponse> getLatestMessages(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable String roomId,
            @RequestParam(defaultValue = "50") int limit) {

        ChatMessageCursorResponse response = chatMessageService.getLatestMessages(roomId, userId, limit);
        return ApiResponse.of(response);
    }

    /**
     * 누락 메시지 동기화 (재접속 복구)
     *
     * GET /api/chat/rooms/{roomId}/messages/sync?afterMessageId=123&limit=200
     */
    @GetMapping("/rooms/{roomId}/messages/sync")
    public ApiResponse<ChatMessageCursorResponse> syncMessages(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") long afterMessageId,
            @RequestParam(defaultValue = "200") int limit) {

        ChatMessageCursorResponse response = chatMessageService.syncMessages(roomId, userId, afterMessageId, limit);
        return ApiResponse.of(response);
    }

    /**
     * 과거 메시지 더보기
     *
     * GET /api/chat/rooms/{roomId}/messages/before?beforeMessageId=123&limit=50
     */
    @GetMapping("/rooms/{roomId}/messages/before")
    public ApiResponse<ChatMessageCursorResponse> getMessagesBefore(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable String roomId,
            @RequestParam long beforeMessageId,
            @RequestParam(defaultValue = "50") int limit) {

        ChatMessageCursorResponse response = chatMessageService.getMessagesBefore(roomId, userId, beforeMessageId, limit);
        return ApiResponse.of(response);
    }

    /**
     * 채팅방 메시지 히스토리 조회
     *
     * GET /api/chat/rooms/{roomId}/messages?page=0&size=20
     */
    @GetMapping("/rooms/{roomId}/messages")
    public ApiResponse<List<ChatMessageResponse>> getMessages(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<ChatMessageResponse> messages = chatMessageService.getMessages(roomId, userId, page, size);
        return ApiResponse.of(messages);
    }

    /**
     * 읽음 처리
     *
     * POST /api/chat/rooms/{roomId}/read
     */
    @PostMapping("/rooms/{roomId}/read")
    public ApiResponse<Void> markAsRead(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable String roomId) {

        chatMessageService.markAsRead(userId, roomId);
        return ApiResponse.of(null);
    }

    /**
     * 사용자의 전체 읽지 않은 메시지 수 조회
     *
     * GET /api/chat/unread/total
     */
    @GetMapping("/unread/total")
    public ApiResponse<Map<String, Integer>> getTotalUnreadCount(
            @AuthenticationPrincipal(expression = "userId") Long userId) {

        int totalUnread = chatParticipantService.getTotalUnreadCount(userId);
        return ApiResponse.of(Map.of("totalUnread", totalUnread));
    }

    /**
     * 특정 채팅방의 읽지 않은 메시지 수 조회
     *
     * GET /api/chat/rooms/{roomId}/unread
     */
    @GetMapping("/rooms/{roomId}/unread")
    public ApiResponse<Map<String, Integer>> getUnreadCount(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable String roomId) {

        int unreadCount = chatParticipantService.getUnreadCount(userId, roomId);
        return ApiResponse.of(Map.of("unreadCount", unreadCount));
    }
}
