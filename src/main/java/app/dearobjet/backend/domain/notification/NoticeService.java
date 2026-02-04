package app.dearobjet.backend.domain.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public NoticeListResponse getNotices(NoticeCategory category, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "publishedAt")
        );

        Page<Notice> noticePage = (category == null)
                ? noticeRepository.findAll(pageable)
                : noticeRepository.findByCategory(category, pageable);

        List<NoticeResponse> noticeResponses = new ArrayList<>();
        for (Notice notice : noticePage.getContent()) {
            noticeResponses.add(toResponse(notice));
        }

        return new NoticeListResponse(noticeResponses, page, noticePage.getTotalPages());
    }

    public NoticeResponse getNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException(noticeId));

        return toResponse(notice);
    }

    private NoticeResponse toResponse(Notice notice) {
        return new NoticeResponse(
                notice.getNotificationId(),
                notice.getCategory(),
                notice.getTitle(),
                notice.getBody(),
                notice.getPublishedAt()
        );
    }
}
