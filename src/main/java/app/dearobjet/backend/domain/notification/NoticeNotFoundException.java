package app.dearobjet.backend.domain.notification;

import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;

public class NoticeNotFoundException extends EntityNotFoundException {
    public NoticeNotFoundException(Long noticeId) {
        super(ErrorCode.NOTICE_NOT_FOUND, "해당 공지사항을 찾을 수 없습니다. noticeId=" + noticeId);
    }
}
