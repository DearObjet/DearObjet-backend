package app.dearobjet.backend.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 커서 기반 메시지 조회 응답
 * - 재접속/누락 복구(sync), 최초 진입(latest), 과거 더보기(before)에서 공통으로 사용
 */
@Getter
@AllArgsConstructor
public class ChatMessageCursorResponse {
    private final String roomId;
    private final Long oldestMessageId;
    private final Long latestMessageId;
    private final List<ChatMessageResponse> messages;
}

