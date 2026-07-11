package app.dearobjet.backend.domain.post.controller;

import app.dearobjet.backend.domain.post.dto.AdminPostListResponse;
import app.dearobjet.backend.domain.post.dto.PostResponse;
import app.dearobjet.backend.domain.post.service.AdminPostService;
import app.dearobjet.backend.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/posts")
public class AdminPostController {

    private final AdminPostService adminPostService;

    @GetMapping
    public ApiResponse<AdminPostListResponse> getPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.of(adminPostService.getPosts(page, keyword));
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getPost(@PathVariable Long postId) {
        return ApiResponse.of(adminPostService.getPost(postId));
    }

    @PatchMapping("/{postId}/blind")
    public ApiResponse<PostResponse> updateBlinded(
            @PathVariable Long postId,
            @RequestParam boolean blinded
    ) {
        return ApiResponse.of(adminPostService.updateBlinded(postId, blinded));
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(@PathVariable Long postId) {
        adminPostService.deletePost(postId);
        return ApiResponse.of(null);
    }
}
