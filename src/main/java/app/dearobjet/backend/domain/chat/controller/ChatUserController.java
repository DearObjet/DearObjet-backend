package app.dearobjet.backend.domain.chat.controller;

import app.dearobjet.backend.domain.chat.dto.ChatUserSearchResponse;
import app.dearobjet.backend.domain.chat.service.ChatUserSearchService;
import app.dearobjet.backend.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat/users")
@RequiredArgsConstructor
public class ChatUserController {

    private static final int DEFAULT_SEARCH_PAGE = 0;
    private static final int DEFAULT_SEARCH_SIZE = 20;
    private static final int MAX_SEARCH_SIZE = 100;

    private final ChatUserSearchService chatUserSearchService;

    /**
     * 채팅 상대 검색
     *
     * GET /api/chat/users/search
     */
    @GetMapping("/search")
    public ApiResponse<Page<ChatUserSearchResponse>> searchChatUsers(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<ChatUserSearchResponse> users = chatUserSearchService.search(
                userId,
                keyword,
                PageRequest.of(normalizePage(page), normalizeSize(size))
        );
        return ApiResponse.of(users);
    }

    private int normalizePage(int page) {
        return Math.max(page, DEFAULT_SEARCH_PAGE);
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return DEFAULT_SEARCH_SIZE;
        }

        return Math.min(size, MAX_SEARCH_SIZE);
    }
}
