package app.dearobjet.backend.domain.story;

import app.dearobjet.backend.domain.story.dto.CreateStoryRequest;
import app.dearobjet.backend.domain.story.dto.StoryListResponse;
import app.dearobjet.backend.domain.story.dto.StoryResponse;
import app.dearobjet.backend.domain.story.dto.UpdateStoryRequest;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;
import app.dearobjet.backend.global.s3.service.S3FileUploadService;
import java.util.ArrayList;
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
public class StoryService {
    private static final int STORY_PAGE_SIZE = 10;

    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final S3FileUploadService s3FileUploadService;

    @Transactional
    public StoryResponse createStory(Long userId, CreateStoryRequest request, MultipartFile thumbnailImage) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        Story story = storyRepository.save(Story.builder()
                .user(user)
                .thumbnailImageUrl(s3FileUploadService.uploadStoryThumbnail(thumbnailImage, userId))
                .title(request.title())
                .content(request.content())
                .build());

        return toResponse(story);
    }

    @Transactional(readOnly = true)
    public StoryListResponse getStories(Long userId, int page) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                STORY_PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "storyId"))
        );
        Page<Story> storyPage = storyRepository.findByUser_Id(userId, pageable);

        List<StoryListResponse.Item> items = new ArrayList<>();
        for (Story story : storyPage.getContent()) {
            items.add(new StoryListResponse.Item(
                    story.getStoryId(),
                    story.getThumbnailImageUrl(),
                    story.getTitle(),
                    story.getContent(),
                    story.getCreatedAt()
            ));
        }

        return new StoryListResponse(items, page, storyPage.getTotalPages());
    }

    @Transactional(readOnly = true)
    public StoryResponse getStory(Long userId, Long storyId) {
        Story story = storyRepository.findByStoryIdAndUser_Id(storyId, userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "스토리를 찾을 수 없습니다."));
        return toResponse(story);
    }

    @Transactional
    public StoryResponse updateStory(
            Long userId,
            Long storyId,
            UpdateStoryRequest request,
            MultipartFile thumbnailImage
    ) {
        Story story = storyRepository.findByStoryIdAndUser_Id(storyId, userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "수정할 스토리를 찾을 수 없습니다."));

        String thumbnailImageUrl = null;
        if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
            thumbnailImageUrl = s3FileUploadService.uploadStoryThumbnail(thumbnailImage, userId);
        }

        story.update(request.title(), request.content(), thumbnailImageUrl);
        return toResponse(story);
    }

    @Transactional
    public void deleteStory(Long userId, Long storyId) {
        Story story = storyRepository.findByStoryIdAndUser_Id(storyId, userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "삭제할 스토리를 찾을 수 없습니다."));
        storyRepository.delete(story);
    }

    private StoryResponse toResponse(Story story) {
        return new StoryResponse(
                story.getStoryId(),
                story.getThumbnailImageUrl(),
                story.getTitle(),
                story.getContent(),
                story.getCreatedAt()
        );
    }
}
