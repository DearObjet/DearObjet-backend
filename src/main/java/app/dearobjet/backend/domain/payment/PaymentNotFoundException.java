package app.dearobjet.backend.domain.payment;

import app.dearobjet.backend.global.exception.BusinessException;
import app.dearobjet.backend.global.exception.ErrorCode;

public class PaymentNotFoundException extends BusinessException {
    public PaymentNotFoundException(Long id) {
        super(ErrorCode.PAYMENT_NOT_FOUND, "결제 정보를 찾을 수 없습니다. paymentId=" + id);
    }
}