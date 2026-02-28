package app.dearobjet.backend.domain.payment;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(Long id) {
        super("결제 정보를 찾을 수 없습니다. paymentId=" + id);
    }
}