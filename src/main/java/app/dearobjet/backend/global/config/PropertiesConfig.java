package app.dearobjet.backend.global.config;

import app.dearobjet.backend.domain.payment.toss.TossPaymentsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TossPaymentsProperties.class)
public class PropertiesConfig {
}