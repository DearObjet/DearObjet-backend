package app.dearobjet.backend.domain.order;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long orderId) {
        super("해당 주문을 찾을 수 없습니다. orderId=" + orderId);
    }
}
