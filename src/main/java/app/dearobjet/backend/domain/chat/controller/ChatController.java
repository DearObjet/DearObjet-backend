package app.dearobjet.backend.domain.chat.controller;

import app.dearobjet.backend.domain.chat.dto.ChatRoomListResponse;
import app.dearobjet.backend.domain.chat.service.ChatService;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;

    @GetMapping
    public ApiResponse<List<ChatRoomListResponse>> getMyChatRooms(
            @AuthenticationPrincipal User user // 현재 로그인한 유저 주입
    ) {
        List<ChatRoomListResponse> response = chatService.getMyChatRooms(user);
        return ApiResponse.of(response);
    }
}
