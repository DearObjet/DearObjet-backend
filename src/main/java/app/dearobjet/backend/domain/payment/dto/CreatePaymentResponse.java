package app.dearobjet.backend.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreatePaymentResponse {
    private final Long paymentId;
    private final String orderNo;
    private final Long amount;
    private final String clientKey;
}