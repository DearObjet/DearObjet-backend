package app.dearobjet.backend.domain.payment.dto;

import app.dearobjet.backend.domain.payment.entity.PaymentProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {
    private Long orderId;
    private PaymentProvider provider;
    private Long amount;
}