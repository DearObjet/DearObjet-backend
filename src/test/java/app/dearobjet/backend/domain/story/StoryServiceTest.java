package app.dearobjet.backend.domain.story;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import app.dearobjet.backend.domain.story.dto.CreateStoryRequest;
import app.dearobjet.backend.domain.story.dto.StoryListResponse;
import app.dearobjet.backend.domain.story.dto.StoryResponse;
import app.dearobjet.backend.domain.story.dto.UpdateStoryRequest;
import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.repository.ShopRepository;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.s3.service.S3FileUploadService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class StoryServiceTest {

    @Mock
    private StoryRepository storyRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private S3FileUploadService s3FileUploadService;

    @InjectMocks
    private StoryService storyService;

    @Test
    void createStory_savesStory() {
        User user = User.builder().id(1L).build();
        MockMultipartFile thumbnailImage = new MockMultipartFile(
                "thumbnailImage",
                "thumb.jpg",
                "image/jpeg",
                "image".getBytes()
        );
        CreateStoryRequest request = new CreateStoryRequest("제목", "내용");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(s3FileUploadService.uploadStoryThumbnail(thumbnailImage, 1L)).willReturn("https://cdn/story.jpg");
        given(storyRepository.save(any(Story.class))).willAnswer(invocation -> {
            Story story = invocation.getArgument(0);
            return Story.builder()
                    .storyId(100L)
                    .user(story.getUser())
                    .thumbnailImageUrl(story.getThumbnailImageUrl())
                    .title(story.getTitle())
                    .content(story.getContent())
                    .build();
        });

        StoryResponse response = storyService.createStory(1L, request, thumbnailImage);

        assertThat(response.storyId()).isEqualTo(100L);
        assertThat(response.thumbnailImageUrl()).isEqualTo("https://cdn/story.jpg");
        assertThat(response.title()).isEqualTo("제목");
        assertThat(response.content()).isEqualTo("내용");
    }

    @Test
    void getStories_returnsPaginatedItems() {
        User user = User.builder().id(1L).build();
        Story first = Story.builder()
                .storyId(2L)
                .thumbnailImageUrl("https://cdn/2.jpg")
                .title("둘")
                .content("둘내용")
                .build();
        Story second = Story.builder()
                .storyId(1L)
                .thumbnailImageUrl("https://cdn/1.jpg")
                .title("하나")
                .content("하나내용")
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(storyRepository.findByUser_Id(any(), any()))
                .willReturn(new PageImpl<>(List.of(first, second)));

        StoryListResponse response = storyService.getStories(1L, 1);

        assertThat(response.items()).hasSize(2);
        assertThat(response.items())
                .extracting(StoryListResponse.Item::storyId)
                .containsExactly(2L, 1L);
        assertThat(response.page()).isEqualTo(1);
    }

    @Test
    void getStoriesByShop_returnsShopOwnerStories() {
        User user = User.builder().id(1L).build();
        Shop shop = Shop.builder()
                .shopId(10L)
                .user(user)
                .build();
        Story story = Story.builder()
                .storyId(1L)
                .thumbnailImageUrl("https://cdn/1.jpg")
                .title("샵 스토리")
                .content("내용")
                .build();

        given(shopRepository.findById(10L)).willReturn(Optional.of(shop));
        given(storyRepository.findByUser_Id(any(), any()))
                .willReturn(new PageImpl<>(List.of(story)));

        StoryListResponse response = storyService.getStoriesByShop(10L, 1);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).storyId()).isEqualTo(1L);
        assertThat(response.page()).isEqualTo(1);
    }

    @Test
    void getStory_returnsStory() {
        User user = User.builder().id(1L).build();
        Story story = Story.builder()
                .storyId(1L)
                .thumbnailImageUrl("https://cdn/1.jpg")
                .title("제목")
                .content("내용")
                .build();

        given(storyRepository.findByStoryIdAndUser_Id(1L, 1L)).willReturn(Optional.of(story));

        StoryResponse response = storyService.getStory(1L, 1L);

        assertThat(response.storyId()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("제목");
    }

    @Test
    void updateStory_updatesFieldsAndThumbnail() {
        Story story = Story.builder()
                .storyId(1L)
                .thumbnailImageUrl("https://cdn/old.jpg")
                .title("이전")
                .content("이전내용")
                .build();
        MockMultipartFile thumbnailImage = new MockMultipartFile(
                "thumbnailImage",
                "thumb.jpg",
                "image/jpeg",
                "image".getBytes()
        );
        UpdateStoryRequest request = new UpdateStoryRequest("새제목", "새내용");

        given(storyRepository.findByStoryIdAndUser_Id(1L, 1L)).willReturn(Optional.of(story));
        given(s3FileUploadService.uploadStoryThumbnail(thumbnailImage, 1L)).willReturn("https://cdn/new.jpg");

        StoryResponse response = storyService.updateStory(1L, 1L, request, thumbnailImage);

        assertThat(response.title()).isEqualTo("새제목");
        assertThat(response.content()).isEqualTo("새내용");
        assertThat(response.thumbnailImageUrl()).isEqualTo("https://cdn/new.jpg");
    }

    @Test
    void deleteStory_deletesOwnedStory() {
        Story story = Story.builder().storyId(1L).build();

        given(storyRepository.findByStoryIdAndUser_Id(1L, 1L)).willReturn(Optional.of(story));

        storyService.deleteStory(1L, 1L);
    }

    @Test
    void updateStory_throwsWhenStoryMissing() {
        UpdateStoryRequest request = new UpdateStoryRequest("새제목", "새내용");

        given(storyRepository.findByStoryIdAndUser_Id(1L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> storyService.updateStory(1L, 1L, request, null))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("수정할 스토리를 찾을 수 없습니다.");
    }
}
