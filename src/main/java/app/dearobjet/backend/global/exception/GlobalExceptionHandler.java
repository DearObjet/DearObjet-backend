package app.dearobjet.backend.global.exception;

import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.api.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 전역 예외 처리기
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * BusinessException 처리 (커스텀 비즈니스 예외)
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleBusinessException(BusinessException e) {
        if (e.getCause() != null) {
            log.warn("BusinessException: {}", e.getMessage(), e);
        } else {
            log.warn("BusinessException: {}", e.getMessage());
        }
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = ErrorResponse.of(errorCode, e.getMessage());
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.of(response));
    }

    /**
     * @Valid 검증 실패 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        log.warn("Validation failed: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT, e.getBindingResult());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(response));
    }

    /**
     * 필수 요청 파라미터 누락 처리 (400)
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        log.warn("MissingServletRequestParameterException: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(response));
    }

    /**
     * 요청 파라미터/경로변수 타입 불일치 처리 (400)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.warn("MethodArgumentTypeMismatchException: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INVALID_INPUT,
                "'" + e.getName() + "' 파라미터의 형식이 올바르지 않습니다."
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(response));
    }

    /**
     * 잘못된 형식의 요청 본문 처리 (400)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadableException: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT, "요청 본문 형식이 올바르지 않습니다.");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(response));
    }

    /**
     * 지원하지 않는 요청 미디어 타입 처리 (415)
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException e) {
        log.warn("HttpMediaTypeNotSupportedException: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT, "지원하지 않는 요청 형식입니다.");
        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.of(response));
    }

    /**
     * IllegalArgumentException 처리 (하위 호환성)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleIllegalArgumentException(
            IllegalArgumentException e) {
        log.warn("IllegalArgumentException: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(response));
    }

    /**
     * IllegalStateException 처리 (하위 호환성)
     */
    @ExceptionHandler(IllegalStateException.class)
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleIllegalStateException(
            IllegalStateException e) {
        log.warn("IllegalStateException: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(response));
    }

    /**
     * 존재하지 않는 리소스 요청 처리 (404)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleNoResourceFoundException(
            NoResourceFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.ENTITY_NOT_FOUND, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.of(response));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e) {
        log.warn("MaxUploadSizeExceededException: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INVALID_INPUT,
                "업로드 가능한 요청 크기를 초과했습니다."
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(response));
    }

    /**
     * 그 외 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleException(Exception e) {
        log.error("Unexpected exception: ", e);
        ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(response));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleDataIntegrityViolation(
            DataIntegrityViolationException e) {
        String msg = String.valueOf(e.getMostSpecificCause().getMessage());

        if (msg.contains("phone_number") || msg.contains("uk_users_phone_number")) {
            ErrorResponse response = ErrorResponse.of(
                    ErrorCode.PHONE_ALREADY_EXISTS,
                    ErrorCode.PHONE_ALREADY_EXISTS.getMessage()
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.of(response));
        }

        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT, "데이터 제약 조건 위반");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.of(response));
    }
}
