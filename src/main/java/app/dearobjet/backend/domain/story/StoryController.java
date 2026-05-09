package app.dearobjet.backend.domain.story;

import app.dearobjet.backend.domain.story.dto.CreateStoryRequest;
import app.dearobjet.backend.domain.story.dto.StoryListResponse;
import app.dearobjet.backend.domain.story.dto.StoryResponse;
import app.dearobjet.backend.domain.story.dto.UpdateStoryRequest;
import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.auth.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stories")
public class StoryController {

    private final StoryService storyService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<StoryResponse> createStory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestPart("request") CreateStoryRequest request,
            @RequestPart("thumbnailImage") MultipartFile thumbnailImage
    ) {
        return ApiResponse.of(storyService.createStory(userDetails.getUserId(), request, thumbnailImage));
    }

    @GetMapping
    public ApiResponse<StoryListResponse> getStories(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "1") int page
    ) {
        return ApiResponse.of(storyService.getStories(userDetails.getUserId(), page));
    }

    @GetMapping("/{storyId}")
    public ApiResponse<StoryResponse> getStory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storyId
    ) {
        return ApiResponse.of(storyService.getStory(userDetails.getUserId(), storyId));
    }

    @PutMapping(value = "/{storyId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<StoryResponse> updateStory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storyId,
            @Valid @RequestPart("request") UpdateStoryRequest request,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
    ) {
        return ApiResponse.of(storyService.updateStory(userDetails.getUserId(), storyId, request, thumbnailImage));
    }

    @DeleteMapping("/{storyId}")
    public ApiResponse<Void> deleteStory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storyId
    ) {
        storyService.deleteStory(userDetails.getUserId(), storyId);
        return ApiResponse.of(null);
    }
}
