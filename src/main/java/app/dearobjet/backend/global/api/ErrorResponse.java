package app.dearobjet.backend.global.api;

import app.dearobjet.backend.global.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.BindingResult;

import java.util.List;

/**
 * API 에러 응답 DTO
 */
@Getter
@Builder
public class ErrorResponse {

    private final String code;
    private final String message;
    private final List<FieldError> errors;

    /**
     * ErrorCode로부터 ErrorResponse 생성
     */
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    /**
     * ErrorCode와 커스텀 메시지로 ErrorResponse 생성
     */
    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(message)
                .build();
    }

    /**
     * 검증 오류가 포함된 ErrorResponse 생성
     */
    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .errors(FieldError.of(bindingResult))
                .build();
    }

    /**
     * 단순 메시지만 포함하는 ErrorResponse 생성 (하위 호환성)
     */
    public static ErrorResponse of(String message) {
        return ErrorResponse.builder()
                .message(message)
                .build();
    }

    /**
     * 필드 검증 오류 정보
     */
    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final String value;
        private final String reason;

        public static List<FieldError> of(BindingResult bindingResult) {
            return bindingResult.getFieldErrors().stream()
                    .map(error -> FieldError.builder()
                            .field(error.getField())
                            .value(error.getRejectedValue() != null ? error.getRejectedValue().toString() : "")
                            .reason(error.getDefaultMessage())
                            .build())
                    .toList();
        }
    }
}
