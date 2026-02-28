package app.dearobjet.backend.domain.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossCancelResponse {
    private String paymentKey;
    private String status;
}