package app.dearobjet.backend.domain.order;

public class OrderAccessDeniedException extends RuntimeException {
  public OrderAccessDeniedException(Long orderId) {
    super("본인 주문만 접근할 수 있습니다. orderId=" + orderId);
  }
}
