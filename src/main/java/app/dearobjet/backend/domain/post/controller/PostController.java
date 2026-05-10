package app.dearobjet.backend.domain.post.controller;

import app.dearobjet.backend.domain.post.dto.CreatePostRequest;
import app.dearobjet.backend.domain.post.dto.PostListResponse;
import app.dearobjet.backend.domain.post.dto.PostResponse;
import app.dearobjet.backend.domain.post.dto.UpdatePostRequest;
import app.dearobjet.backend.domain.post.service.PostService;
import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.auth.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostResponse> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestPart("request") CreatePostRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        return ApiResponse.of(postService.createPost(userDetails.getUserId(), request, images));
    }

    @GetMapping
    public ApiResponse<PostListResponse> getPosts(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page
    ) {
        return ApiResponse.of(postService.getPosts(userId, page));
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getPost(
            @PathVariable Long postId
    ) {
        return ApiResponse.of(postService.getPost(postId));
    }

    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostResponse> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @Valid @RequestPart("request") UpdatePostRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        return ApiResponse.of(postService.updatePost(userDetails.getUserId(), postId, request, images));
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        postService.deletePost(userDetails.getUserId(), postId);
        return ApiResponse.of(null);
    }
}
