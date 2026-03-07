package app.dearobjet.backend.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PaymentListResponse {
    private final List<PaymentResponse> items;
    private final int page;
    private final int totalPages;
}