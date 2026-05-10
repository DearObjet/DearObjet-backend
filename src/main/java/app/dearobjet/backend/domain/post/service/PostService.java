package app.dearobjet.backend.domain.post.service;

import app.dearobjet.backend.domain.post.dto.CreatePostRequest;
import app.dearobjet.backend.domain.post.dto.PostListResponse;
import app.dearobjet.backend.domain.post.dto.PostResponse;
import app.dearobjet.backend.domain.post.dto.UpdatePostRequest;
import app.dearobjet.backend.domain.post.entity.Post;
import app.dearobjet.backend.domain.post.repository.PostRepository;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import app.dearobjet.backend.global.exception.BusinessException;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;
import app.dearobjet.backend.global.s3.service.S3FileUploadService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final int POST_PAGE_SIZE = 12;

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final S3FileUploadService s3FileUploadService;
    private final ObjectMapper objectMapper;

    @Transactional
    public PostResponse createPost(Long userId, CreatePostRequest request, List<MultipartFile> images) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != Role.ARTIST) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        List<String> urls = uploadImages(images, userId);

        Post post = postRepository.save(Post.builder()
                .user(user)
                .title(request.title())
                .content(request.content())
                .imageUrls(toJson(urls))
                .viewCount(0)
                .likeCount(0)
                .commentCount(0)
                .isPublic(request.isPublic() != null ? request.isPublic() : true)
                .build());

        return toResponse(post, urls);
    }

    @Transactional(readOnly = true)
    public PostListResponse getPosts(Long targetUserId, int page) {
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                POST_PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "postId"))
        );

        Page<Post> postPage = postRepository.findByUser_Id(targetUserId, pageable);

        List<PostListResponse.Item> items = new ArrayList<>();
        for (Post post : postPage.getContent()) {
            List<String> urls = fromJson(post.getImageUrls());
            String thumbnail = urls.isEmpty() ? null : urls.get(0);
            items.add(new PostListResponse.Item(
                    post.getPostId(),
                    thumbnail,
                    post.getTitle(),
                    post.getCreatedAt()
            ));
        }

        return new PostListResponse(items, page, postPage.getTotalPages());
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "포스트를 찾을 수 없습니다."));

        return toResponse(post, fromJson(post.getImageUrls()));
    }

    @Transactional
    public PostResponse updatePost(Long userId, Long postId, UpdatePostRequest request, List<MultipartFile> images) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "포스트를 찾을 수 없습니다."));

        if (!post.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        String imageUrlsJson = null;
        List<String> urls;
        if (images != null && !images.isEmpty()) {
            urls = uploadImages(images, userId);
            imageUrlsJson = toJson(urls);
        } else {
            urls = fromJson(post.getImageUrls());
        }

        post.update(request.title(), request.content(), imageUrlsJson, request.isPublic());
        return toResponse(post, urls);
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "포스트를 찾을 수 없습니다."));

        if (!post.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        postRepository.delete(post);
    }

    private List<String> uploadImages(List<MultipartFile> images, Long userId) {
        if (images == null || images.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> urls = new ArrayList<>();
        for (MultipartFile image : images) {
            if (image != null && !image.isEmpty()) {
                urls.add(s3FileUploadService.uploadPostImage(image, userId));
            }
        }
        return urls;
    }

    private String toJson(List<String> urls) {
        try {
            return objectMapper.writeValueAsString(urls);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "이미지 URL 직렬화에 실패했습니다.");
        }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private PostResponse toResponse(Post post, List<String> urls) {
        return new PostResponse(
                post.getPostId(),
                post.getUser().getId(),
                post.getUser().getName(),
                post.getTitle(),
                post.getContent(),
                urls,
                post.getViewCount(),
                post.getLikeCount(),
                post.getIsPublic(),
                post.getCreatedAt()
        );
    }
}
