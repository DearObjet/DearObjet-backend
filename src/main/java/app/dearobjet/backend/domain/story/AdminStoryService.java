package app.dearobjet.backend.domain.story;

import app.dearobjet.backend.domain.story.dto.AdminStoryDetailResponse;
import app.dearobjet.backend.domain.story.dto.AdminStoryListResponse;
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
public class AdminStoryService {

    private static final int PAGE_SIZE = 20;

    private final StoryRepository storyRepository;

    @Transactional(readOnly = true)
    public AdminStoryListResponse getStories(int page, String keyword) {
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "storyId"))
        );

        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.strip() : "";
        Page<Story> storyPage = storyRepository.searchForAdmin(normalizedKeyword, pageable);

        List<AdminStoryListResponse.StorySummary> stories = storyPage.getContent().stream()
                .map(story -> new AdminStoryListResponse.StorySummary(
                        story.getStoryId(),
                        story.getTitle(),
                        story.getUser().getName(),
                        story.getCreatedAt(),
                        Boolean.TRUE.equals(story.getBlinded())
                ))
                .toList();

        return new AdminStoryListResponse(stories, page, storyPage.getTotalPages(), storyPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public AdminStoryDetailResponse getStory(Long storyId) {
        return toDetailResponse(findStory(storyId));
    }

    @Transactional
    public AdminStoryDetailResponse updateBlinded(Long storyId, boolean blinded) {
        Story story = findStory(storyId);
        story.changeBlinded(blinded);

        return toDetailResponse(story);
    }

    @Transactional
    public void deleteStory(Long storyId) {
        storyRepository.delete(findStory(storyId));
    }

    private Story findStory(Long storyId) {
        return storyRepository.findById(storyId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "스토리를 찾을 수 없습니다."));
    }

    private AdminStoryDetailResponse toDetailResponse(Story story) {
        return new AdminStoryDetailResponse(
                story.getStoryId(),
                story.getTitle(),
                story.getUser().getName(),
                story.getCreatedAt(),
                Boolean.TRUE.equals(story.getBlinded()),
                story.getContent(),
                story.getThumbnailImageUrl()
        );
    }
}
