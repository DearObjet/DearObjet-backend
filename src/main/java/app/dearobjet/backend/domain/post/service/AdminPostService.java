package app.dearobjet.backend.domain.post.service;

import app.dearobjet.backend.domain.post.dto.AdminPostListResponse;
import app.dearobjet.backend.domain.post.dto.PostResponse;
import app.dearobjet.backend.domain.post.entity.Post;
import app.dearobjet.backend.domain.post.repository.PostRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminPostService {

    private static final int PAGE_SIZE = 20;
    private static final int PREVIEW_LENGTH = 30;

    private final PostService postService;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public AdminPostListResponse getPosts(int page, String keyword) {
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "postId"))
        );

        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.strip() : null;
        Page<Post> postPage = postRepository.searchForAdmin(normalizedKeyword, pageable);

        List<AdminPostListResponse.Item> items = postPage.getContent().stream()
                .map(post -> new AdminPostListResponse.Item(
                        post.getPostId(),
                        preview(post.getContent()),
                        post.getUser().getName(),
                        post.getCreatedAt(),
                        Boolean.TRUE.equals(post.getBlinded())
                ))
                .toList();

        return new AdminPostListResponse(items, page, postPage.getTotalPages(), postPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId) {
        return postService.getPost(postId);
    }

    @Transactional
    public PostResponse updateBlinded(Long postId, boolean blinded) {
        Post post = findPost(postId);
        post.changeBlinded(blinded);
        return postService.getPost(postId);
    }

    @Transactional
    public void deletePost(Long postId) {
        postRepository.delete(findPost(postId));
    }

    private Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "포스트를 찾을 수 없습니다."));
    }

    private String preview(String content) {
        if (content == null) {
            return "";
        }
        String trimmed = content.strip();
        return trimmed.length() > PREVIEW_LENGTH
                ? trimmed.substring(0, PREVIEW_LENGTH) + "..."
                : trimmed;
    }
}
