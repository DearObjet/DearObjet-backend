package app.dearobjet.backend.domain.notification;

import app.dearobjet.backend.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminNoticeController {

    private final AdminNoticeService adminNoticeService;

    @PostMapping("/notices")
    public ApiResponse<NoticeResponse> createNotice(@RequestBody NoticeCreateRequest request) {
        return ApiResponse.of(adminNoticeService.createNotice(request));
    }

    @GetMapping("/notices")
    public ApiResponse<NoticeListResponse> getNotices(
            @RequestParam(required = false) NoticeTarget target,
            @RequestParam(required = false) NoticeCategory category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.of(adminNoticeService.getNotices(target, category, page, size));
    }

    @GetMapping("/notices/{noticeId}")
    public ApiResponse<NoticeResponse> getNotice(@PathVariable Long noticeId) {
        return ApiResponse.of(adminNoticeService.getNotice(noticeId));
    }

    @PutMapping("/notices/{noticeId}")
    public ApiResponse<NoticeResponse> updateNotice(
            @PathVariable Long noticeId,
            @RequestBody NoticeUpdateRequest request
    ) {
        return ApiResponse.of(adminNoticeService.updateNotice(noticeId, request));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/notices/{noticeId}")
    public void deleteNotice(@PathVariable Long noticeId) {
        adminNoticeService.deleteNotice(noticeId);
    }
}
