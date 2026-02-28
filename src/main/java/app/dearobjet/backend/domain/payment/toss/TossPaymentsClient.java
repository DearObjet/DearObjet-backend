package app.dearobjet.backend.domain.payment.toss;
import app.dearobjet.backend.domain.payment.dto.TossCancelResponse;
import app.dearobjet.backend.domain.payment.dto.TossConfirmResponse;
import app.dearobjet.backend.domain.payment.dto.TossPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TossPaymentsClient {

    private final RestTemplate restTemplate;
    private final TossPaymentsProperties props;

    public TossConfirmResponse confirm(String paymentKey, String orderId, Long amount) {
        String url = props.getApiBase() + "/v1/payments/confirm";

        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers());
        return restTemplate.exchange(url, HttpMethod.POST, entity, TossConfirmResponse.class).getBody();
    }

    public TossPaymentResponse getPayment(String paymentKey) {
        String url = props.getApiBase() + "/v1/payments/" + paymentKey;
        HttpEntity<Void> entity = new HttpEntity<>(headers());
        return restTemplate.exchange(url, HttpMethod.GET, entity, TossPaymentResponse.class).getBody();
    }

    public TossCancelResponse cancel(String paymentKey, Long cancelAmount, String reason) {
        String url = props.getApiBase() + "/v1/payments/" + paymentKey + "/cancel";

        Map<String, Object> body = Map.of(
                "cancelAmount", cancelAmount,
                "cancelReason", (reason == null || reason.isBlank()) ? "USER_CANCEL" : reason
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers());
        return restTemplate.exchange(url, HttpMethod.POST, entity, TossCancelResponse.class).getBody();
    }

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("Authorization", "Basic " + basicAuth());
        return h;
    }

    private String basicAuth() {
        String raw = props.getSecretKey() + ":";
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}