package app.dearobjet.backend.domain.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminNoticeService {

    private final NoticeRepository noticeRepository;

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

        Notice savedNotice = noticeRepository.save(notice);

        boolean isNew = savedNotice.getPublishedAt() != null
                && savedNotice.getPublishedAt().isAfter(OffsetDateTime.now().minusDays(7));

        return new NoticeResponse(
                savedNotice.getNotificationId(),
                savedNotice.getTarget(),
                savedNotice.getCategory(),
                savedNotice.getBadge(),
                savedNotice.getTitle(),
                savedNotice.getBody(),
                savedNotice.getPublishedAt(),
                isNew
        );
    }
}