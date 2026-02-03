package app.dearobjet.backend.domain.notification;

import app.dearobjet.backend.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class NoticeController {

    private final NoticeService noticeQueryService;

    @GetMapping("/notices")
    public ApiResponse<NoticeListResponse> getNotices(
            @RequestParam(required = false) NoticeCategory category, // null이면 공지 전체 목록 조회
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.of(noticeQueryService.getNotices(category, page, size));
    }

    @GetMapping("/notices/{noticeId}")
    public ApiResponse<NoticeResponse> getNotice(@PathVariable Long noticeId) {
        return ApiResponse.of(noticeQueryService.getNotice(noticeId));
    }
}
