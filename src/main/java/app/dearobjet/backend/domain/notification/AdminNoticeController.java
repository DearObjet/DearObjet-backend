package app.dearobjet.backend.domain.notification;

import app.dearobjet.backend.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
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
}