package app.dearobjet.backend.domain.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminNoticeService {

    private final NoticeRepository noticeRepository;

    private static final int NEW_DAYS = 7;

    public NoticeResponse createNotice(NoticeCreateRequest request) {
        Notice notice = Notice.builder()
                .target(request.getTarget())
                .category(request.getCategory())
                .badge(request.getBadge())
                .title(request.getTitle())
                .body(request.getBody())
                .publishedAt(
                        request.getPublishedAt() != null
                                ? request.getPublishedAt()
                                : OffsetDateTime.now()
                )
                .build();

        return toResponse(noticeRepository.save(notice));
    }

    @Transactional(readOnly = true)
    public NoticeListResponse getNotices(NoticeTarget target, NoticeCategory category, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "publishedAt"));

        Page<Notice> noticePage;
        if (target == null) {
            noticePage = noticeRepository.findAll(pageable);
        } else if (category == null) {
            noticePage = noticeRepository.findByTarget(target, pageable);
        } else {
            noticePage = noticeRepository.findByTargetAndCategory(target, category, pageable);
        }

        List<NoticeResponse> items = noticePage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new NoticeListResponse(items, page, noticePage.getTotalPages());
    }

    @Transactional(readOnly = true)
    public NoticeResponse getNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException(noticeId));
        return toResponse(notice);
    }

    public NoticeResponse updateNotice(Long noticeId, NoticeUpdateRequest request) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException(noticeId));
        notice.update(request);
        return toResponse(notice);
    }

    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException(noticeId));
        noticeRepository.delete(notice);
    }

    private NoticeResponse toResponse(Notice notice) {
        boolean isNew = notice.getPublishedAt() != null
                && notice.getPublishedAt().isAfter(OffsetDateTime.now().minusDays(NEW_DAYS));
        return new NoticeResponse(
                notice.getNotificationId(),
                notice.getTarget(),
                notice.getCategory(),
                notice.getBadge(),
                notice.getTitle(),
                notice.getBody(),
                notice.getPublishedAt(),
                isNew
        );
    }
}
