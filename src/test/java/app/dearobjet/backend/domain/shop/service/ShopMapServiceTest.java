package app.dearobjet.backend.domain.shop.service;

import app.dearobjet.backend.domain.contract.ContractRepository;
import app.dearobjet.backend.domain.contract.dto.projection.ShopContractedArtistRow;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import app.dearobjet.backend.domain.shop.client.KakaoLocalClient;
import app.dearobjet.backend.domain.shop.dto.ShopContractedArtistListResponse;
import app.dearobjet.backend.domain.user.repository.ShopRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@DisplayName("ShopMapService 테스트")
@ExtendWith(MockitoExtension.class)
class ShopMapServiceTest {

    private static final Long SHOP_ID = 10L;
    private static final Long ARTIST_ID = 20L;

    @InjectMocks
    private ShopMapService shopMapService;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private KakaoLocalClient kakaoLocalClient;

    @Mock
    private ShopService shopService;

    @Test
    @DisplayName("소품샵 상세의 입점작가 목록은 현재 유효한 승인 계약 작가를 반환한다")
    void givenShop_whenGetContractedArtists_thenReturnCurrentApprovedArtists() {
        given(shopRepository.existsById(SHOP_ID)).willReturn(true);
        given(contractRepository.findCurrentContractedArtistRowsByShopId(
                eq(SHOP_ID),
                eq(ContractStatus.APPROVED),
                any(LocalDate.class)
        )).willReturn(List.of(contractedArtistRow()));

        ShopContractedArtistListResponse response = shopMapService.getContractedArtists(SHOP_ID);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).artistId()).isEqualTo(ARTIST_ID);
        assertThat(response.items().get(0).artistName()).isEqualTo("Postman 도자기 작가");
        assertThat(response.items().get(0).artistImageUrl()).isEqualTo("https://image.test/artist.png");
    }

    @Test
    @DisplayName("존재하지 않는 소품샵의 입점작가 목록은 조회할 수 없다")
    void givenMissingShop_whenGetContractedArtists_thenThrowNotFound() {
        given(shopRepository.existsById(SHOP_ID)).willReturn(false);

        assertThatThrownBy(() -> shopMapService.getContractedArtists(SHOP_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private ShopContractedArtistRow contractedArtistRow() {
        return new ShopContractedArtistRow() {
            @Override
            public Long getArtistId() {
                return ARTIST_ID;
            }

            @Override
            public String getArtistName() {
                return "Postman 도자기 작가";
            }

            @Override
            public String getArtistImageUrl() {
                return "https://image.test/artist.png";
            }
        };
    }
}
