package app.dearobjet.backend.domain.shop.client;

import app.dearobjet.backend.domain.shop.service.ShopCoordinate;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Slf4j
@RequiredArgsConstructor
public class KakaoLocalClient {

    private static final String ADDRESS_SEARCH_URL = "https://dapi.kakao.com/v2/local/search/address.json";

    private final RestTemplate restTemplate;

    @Value("${kakao.map.rest-api-key:}")
    private String restApiKey;

    public ShopCoordinate searchAddress(String address) {
        if (!StringUtils.hasText(restApiKey)) {
            throw new IllegalStateException("KAKAO_MAP_REST_API_KEY is not configured");
        }

        // 한글 주소가 안전하게 넘어가도록 query 파라미터를 인코딩해서 보낸다.
        URI uri = UriComponentsBuilder.fromUriString(ADDRESS_SEARCH_URL)
                .queryParam("query", address)
                .encode()
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        // 카카오 Local REST API는 Authorization 헤더에 KakaoAK 형식을 요구한다.
        headers.set(HttpHeaders.AUTHORIZATION, "KakaoAK " + restApiKey);

        ResponseEntity<KakaoAddressSearchResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                KakaoAddressSearchResponse.class
        );

        KakaoAddressSearchResponse body = response.getBody();
        if (body == null || body.documents() == null || body.documents().isEmpty()) {
            log.warn("Kakao geocoding returned no result for address={}", address);
            return null;
        }

        // 카카오 응답은 x=경도, y=위도라서 순서를 바꿔서 서비스 쪽에 넘긴다.
        KakaoAddressSearchResponse.Document document = body.documents().get(0);
        return new ShopCoordinate(
                Double.parseDouble(document.y()),
                Double.parseDouble(document.x())
        );
    }
}
