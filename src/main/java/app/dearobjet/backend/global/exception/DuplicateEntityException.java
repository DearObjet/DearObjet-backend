package app.dearobjet.backend.global.exception;

/**
 * 중복된 엔티티가 존재할 때 발생하는 예외
 */
public class DuplicateEntityException extends BusinessException {

    public DuplicateEntityException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DuplicateEntityException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
