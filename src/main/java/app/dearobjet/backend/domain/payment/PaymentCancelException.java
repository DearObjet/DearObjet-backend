package app.dearobjet.backend.domain.payment;

import app.dearobjet.backend.global.exception.BusinessException;
import app.dearobjet.backend.global.exception.ErrorCode;

public class PaymentCancelException extends BusinessException {
    public PaymentCancelException(Throwable cause) {
        super(ErrorCode.PAYMENT_CANCEL_FAILED, cause);
    }
}
