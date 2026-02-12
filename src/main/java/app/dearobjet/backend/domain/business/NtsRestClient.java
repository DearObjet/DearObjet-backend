package app.dearobjet.backend.domain.business;

import app.dearobjet.backend.domain.business.dto.NtsValidateRequest;
import app.dearobjet.backend.domain.business.dto.NtsValidateResponse;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class NtsRestClient {
    private static final String URL = "https://api.odcloud.kr/api/nts-businessman/v1/validate";
    private final RestTemplate restTemplate;

    @Value("${nts.api.service-key}")
    private String serviceKey;

    public NtsValidateResponse validate(NtsValidateRequest request) {
        // 1. URI 직접 생성 (serviceKey가 Encoding된 키라면 build(true) 사용)
        URI uri = UriComponentsBuilder.fromHttpUrl(URL)
                .queryParam("serviceKey", serviceKey)
                .queryParam("returnType", "JSON")
                .build(true) // 이중 인코딩 방지
                .toUri();

        System.out.println(uri + " 제발==========================");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<NtsValidateRequest> entity = new HttpEntity<>(request, headers);

        return restTemplate.postForObject(uri, entity, NtsValidateResponse.class);
    }
}