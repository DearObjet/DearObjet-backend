package app.dearobjet.backend.domain.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import app.dearobjet.backend.domain.post.dto.CreatePostRequest;
import app.dearobjet.backend.domain.post.dto.PostListResponse;
import app.dearobjet.backend.domain.post.dto.PostResponse;
import app.dearobjet.backend.domain.post.dto.UpdatePostRequest;
import app.dearobjet.backend.domain.post.entity.Post;
import app.dearobjet.backend.domain.post.repository.PostRepository;
import app.dearobjet.backend.domain.post.service.PostService;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import app.dearobjet.backend.global.exception.BusinessException;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.s3.service.S3FileUploadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private S3FileUploadService s3FileUploadService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private PostService postService;

    @Test
    void createPost_savesPost() {
        User user = User.builder().id(1L).name("작가").build();
        MockMultipartFile image = new MockMultipartFile("images", "img.jpg", "image/jpeg", "img".getBytes());
        CreatePostRequest request = new CreatePostRequest("내용", true);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(s3FileUploadService.uploadPostImage(image, 1L)).willReturn("https://cdn/post.jpg");
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            return Post.builder()
                    .postId(100L)
                    .user(post.getUser())
                    .content(post.getContent())
                    .imageUrls(post.getImageUrls())
                    .isPublic(post.getIsPublic())
                    .build();
        });

        PostResponse response = postService.createPost(1L, request, List.of(image));

        assertThat(response.postId()).isEqualTo(100L);
        assertThat(response.content()).isEqualTo("내용");
        assertThat(response.imageUrls()).containsExactly("https://cdn/post.jpg");
        assertThat(response.isPublic()).isTrue();
    }

    @Test
    void createPost_throwsWhenUserNotFound() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.createPost(1L, new CreatePostRequest("내용", true), null))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getPosts_returnsPaginatedItems() {
        User user = User.builder().id(1L).name("작가").build();
        Post post1 = Post.builder()
                .postId(2L)
                .user(user)
                .imageUrls("[\"https://cdn/2.jpg\"]")
                .build();
        Post post2 = Post.builder()
                .postId(1L)
                .user(user)
                .imageUrls("[]")
                .build();

        given(postRepository.findByUser_Id(any(), any()))
                .willReturn(new PageImpl<>(List.of(post1, post2)));

        PostListResponse response = postService.getPosts(1L, 1);

        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).postId()).isEqualTo(2L);
        assertThat(response.items().get(0).thumbnailUrl()).isEqualTo("https://cdn/2.jpg");
        assertThat(response.items().get(1).thumbnailUrl()).isNull();
        assertThat(response.page()).isEqualTo(1);
    }

    @Test
    void getPost_returnsPost() {
        User user = User.builder().id(1L).name("작가").build();
        Post post = Post.builder()
                .postId(1L)
                .user(user)
                .content("내용")
                .imageUrls("[\"https://cdn/1.jpg\",\"https://cdn/2.jpg\"]")
                .isPublic(true)
                .build();

        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        PostResponse response = postService.getPost(1L);

        assertThat(response.postId()).isEqualTo(1L);
        assertThat(response.content()).isEqualTo("내용");
        assertThat(response.imageUrls()).containsExactly("https://cdn/1.jpg", "https://cdn/2.jpg");
    }

    @Test
    void getPost_throwsWhenNotFound() {
        given(postRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPost(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updatePost_updatesFieldsAndImages() {
        User user = User.builder().id(1L).name("작가").build();
        Post post = Post.builder()
                .postId(1L)
                .user(user)
                .content("이전내용")
                .imageUrls("[\"https://cdn/old.jpg\"]")
                .isPublic(true)
                .build();
        MockMultipartFile image = new MockMultipartFile("images", "new.jpg", "image/jpeg", "img".getBytes());
        UpdatePostRequest request = new UpdatePostRequest("새내용", true);

        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(s3FileUploadService.uploadPostImage(image, 1L)).willReturn("https://cdn/new.jpg");

        PostResponse response = postService.updatePost(1L, 1L, request, List.of(image));

        assertThat(response.content()).isEqualTo("새내용");
        assertThat(response.imageUrls()).containsExactly("https://cdn/new.jpg");
    }

    @Test
    void updatePost_keepsOldImagesWhenNoneProvided() {
        User user = User.builder().id(1L).name("작가").build();
        Post post = Post.builder()
                .postId(1L)
                .user(user)
                .content("이전내용")
                .imageUrls("[\"https://cdn/old.jpg\"]")
                .isPublic(true)
                .build();
        UpdatePostRequest request = new UpdatePostRequest("새내용", true);

        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        PostResponse response = postService.updatePost(1L, 1L, request, null);

        assertThat(response.content()).isEqualTo("새내용");
        assertThat(response.imageUrls()).containsExactly("https://cdn/old.jpg");
    }

    @Test
    void updatePost_throwsWhenNotOwner() {
        User owner = User.builder().id(2L).build();
        Post post = Post.builder().postId(1L).user(owner).build();

        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.updatePost(1L, 1L, new UpdatePostRequest("새내용", true), null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deletePost_deletesOwnedPost() {
        User user = User.builder().id(1L).build();
        Post post = Post.builder().postId(1L).user(user).build();

        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        postService.deletePost(1L, 1L);
    }

    @Test
    void deletePost_throwsWhenNotOwner() {
        User owner = User.builder().id(2L).build();
        Post post = Post.builder().postId(1L).user(owner).build();

        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.deletePost(1L, 1L))
                .isInstanceOf(BusinessException.class);
    }
}
