package app.dearobjet.backend.domain.payment.toss;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "toss")
public class TossPaymentsProperties {
    private String secretKey;
    private String clientKey;
    private String apiBase = "https://api.tosspayments.com";
}