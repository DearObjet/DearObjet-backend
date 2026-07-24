package app.dearobjet.backend.domain.order;

import app.dearobjet.backend.global.exception.BusinessException;
import app.dearobjet.backend.global.exception.ErrorCode;

public class OrderNotFoundException extends BusinessException {
    public OrderNotFoundException(Long orderId) {
        super(ErrorCode.ORDER_NOT_FOUND, "해당 주문을 찾을 수 없습니다. orderId=" + orderId);
    }
}
