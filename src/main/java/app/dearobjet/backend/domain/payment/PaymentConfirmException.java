package app.dearobjet.backend.domain.payment;

import app.dearobjet.backend.global.exception.BusinessException;
import app.dearobjet.backend.global.exception.ErrorCode;

public class PaymentConfirmException extends BusinessException {
    public PaymentConfirmException(Throwable cause) {
        super(ErrorCode.PAYMENT_CONFIRM_FAILED, cause);
    }
}
