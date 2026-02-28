package app.dearobjet.backend.domain.payment;

import app.dearobjet.backend.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PaymentWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/payments/webhooks/toss")
    public ApiResponse<Void> tossWebhook(@RequestBody Map<String, Object> body) {
        // paymentKey 뽑아서 결제 조회로 검증 후 반영
        Object paymentKey = body.get("paymentKey");
        if (paymentKey == null && body.get("data") instanceof Map<?, ?> data) {
            Object pk = data.get("paymentKey");
            if (pk != null) paymentKey = pk;
        }

        paymentService.handleWebhookToss(paymentKey == null ? null : paymentKey.toString());
        return ApiResponse.of(null);
    }
}