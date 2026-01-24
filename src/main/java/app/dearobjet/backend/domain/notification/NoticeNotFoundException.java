package app.dearobjet.backend.domain.notification;

public class NoticeNotFoundException extends RuntimeException {
    public NoticeNotFoundException(Long noticeId) {
        super("해당 공지사항을 찾을 수 없습니다. noticeId=" + noticeId);
    }
}
