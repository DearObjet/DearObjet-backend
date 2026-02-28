package app.dearobjet.backend.domain.payment;

public class PaymentBadRequestException extends RuntimeException {
    public PaymentBadRequestException(String msg) {
        super(msg);
    }
}