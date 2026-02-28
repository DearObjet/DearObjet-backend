// file: .../domain/payment/PaymentBadRequestException.java
package app.dearobjet.backend.domain.payment;

import app.dearobjet.backend.global.exception.BusinessException;
import app.dearobjet.backend.global.exception.ErrorCode;

public class PaymentBadRequestException extends BusinessException {
    public PaymentBadRequestException(String msg) {
        super(ErrorCode.INVALID_INPUT, msg);
    }
}