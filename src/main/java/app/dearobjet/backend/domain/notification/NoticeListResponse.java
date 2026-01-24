package app.dearobjet.backend.domain.notification;

import app.dearobjet.backend.domain.notification.NoticeResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NoticeListResponse {
    private final List<NoticeResponse> items;
    private final int page;
    private final int totalPages;
}
