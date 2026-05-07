package app.dearobjet.backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 애플리케이션 전역 에러 코드 정의
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common (C0XX)
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력입니다"),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "데이터를 찾을 수 없습니다"),
    DUPLICATE_ENTITY(HttpStatus.CONFLICT, "C003", "이미 존재하는 데이터입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "서버 내부 오류가 발생했습니다"),

    // User (U0XX)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "U002", "이미 사용 중인 이메일입니다"),
    PHONE_ALREADY_EXISTS(HttpStatus.CONFLICT, "U003", "이미 사용 중인 전화번호입니다"),
    USER_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "U004", "이미 가입 완료된 사용자입니다"),

    // Chat (CH0XX)
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CH001", "채팅방을 찾을 수 없습니다"),
    NOT_CHAT_PARTICIPANT(HttpStatus.FORBIDDEN, "CH002", "채팅방에 참여하지 않았습니다"),
    CANNOT_CHAT_WITH_SELF(HttpStatus.BAD_REQUEST, "CH003", "자기 자신과는 채팅할 수 없습니다"),
    INVALID_CHAT_PARTICIPANTS(HttpStatus.BAD_REQUEST, "CH004", "유효하지 않은 참여자입니다"),
    GROUP_CHAT_MIN_PARTICIPANTS(HttpStatus.BAD_REQUEST, "CH005", "그룹 채팅은 최소 3명 이상이어야 합니다"),
    CHAT_MESSAGE_PUBLISH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CH006", "메시지 발행에 실패했습니다"),

    // Class Reservation (CR0XX)
    CLASS_OWNER_CANNOT_RESERVE(HttpStatus.BAD_REQUEST, "CR001", "클래스를 개설한 본인은 예약할 수 없습니다"),
    ACTIVE_CLASS_RESERVATION_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "CR002", "이미 대기 중인 원데이클래스 예약이 있습니다"),
    CLASS_RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "CR003", "예약한 원데이클래스 내역이 없습니다"),

    // Auth (A0XX)
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A001", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "만료된 토큰입니다"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "A003", "리프레시 토큰을 찾을 수 없습니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A004", "인증이 필요합니다"),

    // Notice (N0XX)
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "N001", "공지사항을 찾을 수 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
