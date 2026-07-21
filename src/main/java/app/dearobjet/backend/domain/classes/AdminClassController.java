package app.dearobjet.backend.domain.classes;

import app.dearobjet.backend.domain.classes.dto.AdminClassBlindUpdateRequest;
import app.dearobjet.backend.domain.classes.dto.AdminClassDetailResponse;
import app.dearobjet.backend.domain.classes.dto.AdminClassListResponse;
import app.dearobjet.backend.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/classes")
public class AdminClassController {

    private final AdminClassService adminClassService;

    @GetMapping
    public ApiResponse<AdminClassListResponse> getClasses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.of(adminClassService.getClasses(page, keyword));
    }

    @GetMapping("/{classId}")
    public ApiResponse<AdminClassDetailResponse> getClass(@PathVariable Long classId) {
        return ApiResponse.of(adminClassService.getClass(classId));
    }

    @PatchMapping("/{classId}/blind")
    public ApiResponse<AdminClassDetailResponse> updateBlinded(
            @PathVariable Long classId,
            @Valid @RequestBody AdminClassBlindUpdateRequest request
    ) {
        return ApiResponse.of(adminClassService.updateBlinded(classId, request.blinded()));
    }
}
