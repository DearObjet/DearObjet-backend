package app.dearobjet.backend.global.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate(ObjectMapper objectMapper) {

        MappingJackson2HttpMessageConverter jsonConverter =
                new MappingJackson2HttpMessageConverter(objectMapper);

        jsonConverter.setDefaultCharset(StandardCharsets.UTF_8);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(List.of(jsonConverter));

        restTemplate.setRequestFactory(
                new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
                    setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
                    setReadTimeout((int) Duration.ofSeconds(5).toMillis());
                }}
        );

        return restTemplate;
    }
}
