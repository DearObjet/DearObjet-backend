package app.dearobjet.backend.domain.chat.controller;

import app.dearobjet.backend.domain.chat.dto.ChatRoomListResponse;
import app.dearobjet.backend.domain.chat.service.ChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean ChatService chatService;

    @Test
    @DisplayName("채팅방 목록 조회 API는 ApiResponse 포맷인 data 필드로 감싸져서 반환된다.")
    @WithMockUser // Spring Security 통과용 가짜 유저
    void getMyChatRoomsApiTest() throws Exception {
        // given
        ChatRoomListResponse responseDto = new ChatRoomListResponse(
                1L, "마지막 메시지", LocalDateTime.now(), 5,
                2L, "상대방닉네임", "img.jpg"
        );

        // 서비스는 단순히 위 객체를 리스트로 반환한다고 가정 (Mocking)
        given(chatService.getMyChatRooms(any())).willReturn(List.of(responseDto));

        // when & then
        mockMvc.perform(get("/api/chats")
                        .with(csrf())) // CSRF 보호가 켜져있다면 필요
                .andDo(print())
                .andExpect(status().isOk())
                // ★ ApiResponse 검증: 최상위에 'data' 필드가 있어야 함
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].unreadCount").value(5))
                .andExpect(jsonPath("$.data[0].partnerNickname").value("상대방닉네임"));
    }
}