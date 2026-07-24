package app.dearobjet.backend.domain.order;

import app.dearobjet.backend.global.exception.BusinessException;
import app.dearobjet.backend.global.exception.ErrorCode;

public class OrderAccessDeniedException extends BusinessException {
  public OrderAccessDeniedException(Long orderId) {
    super(ErrorCode.ORDER_ACCESS_DENIED, "본인 주문만 접근할 수 있습니다. orderId=" + orderId);
  }
}
