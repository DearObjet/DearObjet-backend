package app.dearobjet.backend.domain.artist;

import app.dearobjet.backend.domain.artist.dto.ArtistShipmentProductListResponse;
import app.dearobjet.backend.domain.artist.dto.ArtistShipmentShopListResponse;
import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.artist.service.ArtistShipmentService;
import app.dearobjet.backend.domain.contract.ContractRepository;
import app.dearobjet.backend.domain.contract.dto.projection.ArtistShipmentProductRow;
import app.dearobjet.backend.domain.contract.dto.projection.ArtistShipmentShopRow;
import app.dearobjet.backend.domain.contract.entity.ShopArtistContract;
import app.dearobjet.backend.domain.contract.enums.CommissionType;
import app.dearobjet.backend.domain.contract.enums.ContractProductListingStatus;
import app.dearobjet.backend.domain.contract.enums.ContractProductStockMovementType;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import app.dearobjet.backend.domain.contract.repository.ContractProductRepository;
import app.dearobjet.backend.domain.user.enums.Specialty;
import app.dearobjet.backend.domain.user.repository.ArtistRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@DisplayName("ArtistShipmentService 출고관리 테스트")
@ExtendWith(MockitoExtension.class)
class ArtistShipmentServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long ARTIST_ID = 10L;
    private static final Long CONTRACT_ID = 50L;
    private static final Long CONTRACT_PRODUCT_ID = 70L;
    private static final Long SHOP_ID = 100L;
    private static final BigDecimal SELLING_PRICE = new BigDecimal("15000.00");
    private static final BigDecimal COMMISSION_VALUE = new BigDecimal("20.0000");
    private static final BigDecimal UNIT_SETTLEMENT_AMOUNT = new BigDecimal("12000.00");
    private static final LocalDate CONTRACT_START_DATE = LocalDate.of(2026, 5, 1);
    private static final LocalDate CONTRACT_END_DATE = LocalDate.of(2026, 12, 31);
    private static final LocalDateTime RECENT_STOCKED_AT = LocalDateTime.of(2026, 5, 3, 10, 0);

    @InjectMocks
    private ArtistShipmentService artistShipmentService;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ContractProductRepository contractProductRepository;

    @Test
    @DisplayName("출고관리 입점 매장 목록에서 입고확인 상태를 함께 반환한다")
    void givenArtist_whenGetShipmentShops_thenReturnInboundStatuses() {
        given(artistRepository.findByUserId(USER_ID)).willReturn(Optional.of(artist()));
        given(contractRepository.findShipmentShopRowsByArtistId(
                eq(ARTIST_ID),
                eq(ContractStatus.APPROVED),
                eq(ContractProductListingStatus.ACTIVE),
                any(LocalDate.class)
        )).willReturn(List.of(
                shipmentShopRow(CONTRACT_ID, SHOP_ID, "확인 소품샵", RECENT_STOCKED_AT, true),
                shipmentShopRow(CONTRACT_ID + 1, SHOP_ID + 1, "미확인 소품샵", RECENT_STOCKED_AT, false),
                shipmentShopRow(CONTRACT_ID + 2, SHOP_ID + 2, "보류 소품샵", null, false)
        ));

        ArtistShipmentShopListResponse response = artistShipmentService.getShipmentShops(USER_ID);

        assertThat(response.getItems()).hasSize(3);
        assertThat(response.getItems()).extracting("inboundStatusCode")
                .containsExactly("CONFIRMED", "UNCONFIRMED", "HOLD");
        assertThat(response.getItems()).extracting("inboundStatusLabel")
                .containsExactly("확인", "미확인", "보류");
        assertThat(response.getItems().get(0).getSpecialty()).isEqualTo(Specialty.LIVING_GOODS);
        assertThat(response.getItems().get(0).getContractStartDate()).isEqualTo(CONTRACT_START_DATE);
        assertThat(response.getItems().get(0).getContractEndDate()).isEqualTo(CONTRACT_END_DATE);
    }

    @Test
    @DisplayName("작가 정보가 없으면 출고관리 목록을 조회할 수 없다")
    void givenMissingArtist_whenGetShipmentShops_thenThrowNotFound() {
        given(artistRepository.findByUserId(USER_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> artistShipmentService.getShipmentShops(USER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("작가 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("출고리스트는 소품샵의 계약 이력별 출고상품 상세를 반환한다")
    void givenShipmentContracts_whenGetShipmentProducts_thenReturnProductsGroupedByContract() {
        given(artistRepository.findByUserId(USER_ID)).willReturn(Optional.of(artist()));
        given(contractRepository.findShipmentProductContractsByArtistIdAndShopId(
                ARTIST_ID,
                SHOP_ID,
                List.of(ContractStatus.APPROVED, ContractStatus.ENDED, ContractStatus.TERMINATED)
        )).willReturn(List.of(
                contract(CONTRACT_ID + 1, ContractStatus.ENDED),
                contract(CONTRACT_ID, ContractStatus.APPROVED)
        ));
        given(contractProductRepository.findShipmentProductRowsByContractIds(
                List.of(CONTRACT_ID + 1, CONTRACT_ID),
                ARTIST_ID,
                ContractProductStockMovementType.INBOUND
        )).willReturn(List.of(
                shipmentProductRow(CONTRACT_ID + 1, CONTRACT_PRODUCT_ID + 1, "과거 계약 접시", 3L),
                shipmentProductRow(CONTRACT_ID, CONTRACT_PRODUCT_ID, "세라믹 컵", 10L)
        ));

        ArtistShipmentProductListResponse response = artistShipmentService.getShipmentProducts(USER_ID, SHOP_ID);

        assertThat(response.getShopId()).isEqualTo(SHOP_ID);
        assertThat(response.getContracts()).hasSize(2);
        assertThat(response.getContracts()).extracting("contractId")
                .containsExactly(CONTRACT_ID + 1, CONTRACT_ID);
        assertThat(response.getContracts().get(0).getContractStatus()).isEqualTo(ContractStatus.ENDED);
        assertThat(response.getContracts().get(0).getItems()).hasSize(1);
        assertThat(response.getContracts().get(0).getItems().get(0).getContractProductId())
                .isEqualTo(CONTRACT_PRODUCT_ID + 1);
        assertThat(response.getContracts().get(0).getItems().get(0).getProductName())
                .isEqualTo("과거 계약 접시");
        assertThat(response.getContracts().get(1).getItems().get(0).getProductName()).isEqualTo("세라믹 컵");
        assertThat(response.getContracts().get(1).getItems().get(0).getTotalShipmentQuantity()).isEqualTo(10L);
        assertThat(response.getContracts().get(1).getItems().get(0).getSellingPrice())
                .isEqualByComparingTo(SELLING_PRICE);
        assertThat(response.getContracts().get(1).getItems().get(0).getCommissionType()).isEqualTo(CommissionType.RATE);
        assertThat(response.getContracts().get(1).getItems().get(0).getCommissionValue())
                .isEqualByComparingTo(COMMISSION_VALUE);
        assertThat(response.getContracts().get(1).getItems().get(0).getUnitSettlementAmount())
                .isEqualByComparingTo(UNIT_SETTLEMENT_AMOUNT);
    }

    @Test
    @DisplayName("계약 이력이 없는 소품샵이면 출고리스트를 조회할 수 없다")
    void givenNoShipmentContract_whenGetShipmentProducts_thenThrowNotFound() {
        given(artistRepository.findByUserId(USER_ID)).willReturn(Optional.of(artist()));
        given(contractRepository.findShipmentProductContractsByArtistIdAndShopId(
                ARTIST_ID,
                SHOP_ID,
                List.of(ContractStatus.APPROVED, ContractStatus.ENDED, ContractStatus.TERMINATED)
        ))
                .willReturn(List.of());

        assertThatThrownBy(() -> artistShipmentService.getShipmentProducts(USER_ID, SHOP_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("계약서 정보를 찾을 수 없습니다.");
    }

    private Artist artist() {
        return Artist.builder()
                .id(ARTIST_ID)
                .build();
    }

    private ShopArtistContract contract(ContractStatus contractStatus) {
        return contract(CONTRACT_ID, contractStatus);
    }

    private ShopArtistContract contract(Long contractId, ContractStatus contractStatus) {
        return ShopArtistContract.builder()
                .shopArtistContractsId(contractId)
                .contractStatus(contractStatus)
                .contractStartDate(CONTRACT_START_DATE)
                .contractEndDate(CONTRACT_END_DATE)
                .build();
    }

    private ArtistShipmentProductRow shipmentProductRow(
            Long contractId,
            Long contractProductId,
            String productName,
            Long totalShipmentQuantity
    ) {
        return new ArtistShipmentProductRow() {
            @Override
            public Long getContractId() {
                return contractId;
            }

            @Override
            public Long getContractProductId() {
                return contractProductId;
            }

            @Override
            public String getProductImageUrl() {
                return "https://image.test/products/cup.png";
            }

            @Override
            public String getProductName() {
                return productName;
            }

            @Override
            public Long getTotalShipmentQuantity() {
                return totalShipmentQuantity;
            }

            @Override
            public BigDecimal getSellingPrice() {
                return SELLING_PRICE;
            }

            @Override
            public CommissionType getCommissionType() {
                return CommissionType.RATE;
            }

            @Override
            public BigDecimal getCommissionValue() {
                return COMMISSION_VALUE;
            }

            @Override
            public BigDecimal getUnitSettlementAmount() {
                return UNIT_SETTLEMENT_AMOUNT;
            }
        };
    }

    private ArtistShipmentShopRow shipmentShopRow(
            Long contractId,
            Long shopId,
            String shopName,
            LocalDateTime recentStockedAt,
            Boolean inboundConfirmed
    ) {
        return new ArtistShipmentShopRow() {
            @Override
            public Long getContractId() {
                return contractId;
            }

            @Override
            public Long getShopId() {
                return shopId;
            }

            @Override
            public String getShopName() {
                return shopName;
            }

            @Override
            public Specialty getSpecialty() {
                return Specialty.LIVING_GOODS;
            }

            @Override
            public LocalDate getContractStartDate() {
                return CONTRACT_START_DATE;
            }

            @Override
            public LocalDate getContractEndDate() {
                return CONTRACT_END_DATE;
            }

            @Override
            public LocalDateTime getRecentStockedAt() {
                return recentStockedAt;
            }

            @Override
            public Boolean getInboundConfirmed() {
                return inboundConfirmed;
            }
        };
    }
}
