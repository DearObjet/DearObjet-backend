package app.dearobjet.backend.domain.story;

import app.dearobjet.backend.domain.story.dto.AdminStoryBlindUpdateRequest;
import app.dearobjet.backend.domain.story.dto.AdminStoryDetailResponse;
import app.dearobjet.backend.domain.story.dto.AdminStoryListResponse;
import app.dearobjet.backend.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/stories")
public class AdminStoryController {

    private final AdminStoryService adminStoryService;

    @GetMapping
    public ApiResponse<AdminStoryListResponse> getStories(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.of(adminStoryService.getStories(page, keyword));
    }

    @GetMapping("/{storyId}")
    public ApiResponse<AdminStoryDetailResponse> getStory(@PathVariable Long storyId) {
        return ApiResponse.of(adminStoryService.getStory(storyId));
    }

    @PatchMapping("/{storyId}/blind")
    public ApiResponse<AdminStoryDetailResponse> updateBlinded(
            @PathVariable Long storyId,
            @Valid @RequestBody AdminStoryBlindUpdateRequest request
    ) {
        return ApiResponse.of(adminStoryService.updateBlinded(storyId, request.blinded()));
    }

    @DeleteMapping("/{storyId}")
    public ApiResponse<Void> deleteStory(@PathVariable Long storyId) {
        adminStoryService.deleteStory(storyId);
        return ApiResponse.of(null);
    }
}
