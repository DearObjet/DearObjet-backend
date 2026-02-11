package app.dearobjet.backend.global.exception;

/**
 * 잘못된 입력값 예외
 */
public class InvalidInputException extends BusinessException {

    public InvalidInputException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidInputException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
