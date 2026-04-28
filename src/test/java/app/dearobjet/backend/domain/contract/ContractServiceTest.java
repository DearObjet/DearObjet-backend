package app.dearobjet.backend.domain.contract;

import app.dearobjet.backend.domain.contract.dto.AdjustContractInventoryRequest;
import app.dearobjet.backend.domain.contract.dto.AdjustContractInventoryResponse;
import app.dearobjet.backend.domain.contract.dto.ContractApplicationCountResponse;
import app.dearobjet.backend.domain.contract.dto.ContractInboundConfirmResponse;
import app.dearobjet.backend.domain.contract.dto.ContractInventoryListResponse;
import app.dearobjet.backend.domain.contract.dto.ContractMemoResponse;
import app.dearobjet.backend.domain.contract.dto.UpdateContractMemoRequest;
import app.dearobjet.backend.domain.contract.dto.projection.ContractInventoryRow;
import app.dearobjet.backend.domain.contract.entity.ContractProduct;
import app.dearobjet.backend.domain.contract.entity.ContractProductStockMovement;
import app.dearobjet.backend.domain.contract.entity.ShopArtistContract;
import app.dearobjet.backend.domain.contract.enums.CommissionType;
import app.dearobjet.backend.domain.contract.enums.ContractProductListingStatus;
import app.dearobjet.backend.domain.contract.enums.ContractProductStockMovementType;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import app.dearobjet.backend.domain.contract.repository.ContractProductRepository;
import app.dearobjet.backend.domain.contract.repository.ContractProductStockMovementRepository;
import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.repository.ShopRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DisplayName("ContractService 테스트")
@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long SHOP_ID = 10L;
    private static final Long CONTRACT_ID = 50L;
    private static final Long CONTRACT_PRODUCT_ID = 100L;
    private static final LocalDateTime OCCURRED_AT = LocalDateTime.of(2026, 4, 27, 12, 30);

    @InjectMocks
    private ContractService contractService;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ContractProductRepository contractProductRepository;

    @Mock
    private ContractProductStockMovementRepository contractProductStockMovementRepository;

    @Mock
    private ShopRepository shopRepository;

    @Nested
    @DisplayName("getInventory")
    class GetInventoryTest {

        @Test
        @DisplayName("샵이 없으면 curl에서 본 것과 같은 메시지로 예외가 발생한다")
        void givenMissingShop_whenGetInventory_thenThrowIllegalArgumentException() {
            given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> contractService.getInventory(USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Shop not found for user: 1");
        }

        @Test
        @DisplayName("샵이 있으면 승인 계약 작가 목록을 반환한다")
        void givenShop_whenGetInventory_thenReturnInventoryList() {
            Shop shop = shopWithId(SHOP_ID);
            ContractInventoryRow row = new ContractInventoryRow() {
                @Override
                public Long getContractId() {
                    return CONTRACT_ID;
                }

                @Override
                public String getArtistImageUrl() {
                    return "https://image.test/a.png";
                }

                @Override
                public String getArtistName() {
                    return "작가";
                }

                @Override
                public app.dearobjet.backend.domain.user.enums.Specialty getSpecialty() {
                    return app.dearobjet.backend.domain.user.enums.Specialty.HANDMADE_CRAFT;
                }

                @Override
                public LocalDateTime getRecentStockedAt() {
                    return OCCURRED_AT;
                }

                @Override
                public Boolean getInboundConfirmed() {
                    return true;
                }
            };

            given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
            given(contractRepository.findInventoryRowsByShopId(
                    SHOP_ID,
                    ContractStatus.APPROVED,
                    ContractProductListingStatus.ACTIVE
            )).willReturn(List.of(row));

            ContractInventoryListResponse response = contractService.getInventory(USER_ID);

            assertThat(response.getItems()).hasSize(1);
            assertThat(response.getItems().get(0).getContractId()).isEqualTo(CONTRACT_ID);
            assertThat(response.getItems().get(0).getInboundConfirmed()).isTrue();
        }
    }

    @Nested
    @DisplayName("updateInventoryMemo")
    class UpdateInventoryMemoTest {

        @Test
        @DisplayName("승인 계약이면 메모를 공백 포함 그대로 저장한다")
        void givenApprovedContract_whenUpdateMemo_thenReturnUpdatedMemo() {
            Shop shop = shopWithId(SHOP_ID);
            ShopArtistContract contract = contractWithId(CONTRACT_ID);
            UpdateContractMemoRequest request = new UpdateContractMemoRequest();
            setField(request, "memo", "다음 입고 때 진열대 위치 조정");

            given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
            given(contractRepository.findApprovedByIdAndShopId(CONTRACT_ID, SHOP_ID, ContractStatus.APPROVED))
                    .willReturn(Optional.of(contract));

            ContractMemoResponse response = contractService.updateInventoryMemo(USER_ID, CONTRACT_ID, request);

            assertThat(response.getContractId()).isEqualTo(CONTRACT_ID);
            assertThat(response.getMemo()).isEqualTo("다음 입고 때 진열대 위치 조정");
        }
    }

    @Nested
    @DisplayName("confirmRecentInbound")
    class ConfirmRecentInboundTest {

        @Test
        @DisplayName("승인 계약이면 최근 입고확인 상태를 저장한다")
        void givenApprovedContract_whenConfirmRecentInbound_thenReturnConfirmedStatus() {
            Shop shop = shopWithId(SHOP_ID);
            ShopArtistContract contract = contractWithId(CONTRACT_ID);

            given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
            given(contractRepository.findApprovedByIdAndShopId(CONTRACT_ID, SHOP_ID, ContractStatus.APPROVED))
                    .willReturn(Optional.of(contract));

            ContractInboundConfirmResponse response = contractService.confirmRecentInbound(USER_ID, CONTRACT_ID);

            assertThat(response.getContractId()).isEqualTo(CONTRACT_ID);
            assertThat(response.getInboundConfirmed()).isTrue();
            assertThat(response.getConfirmedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getPendingApplicationCount")
    class GetPendingApplicationCountTest {

        @Test
        @DisplayName("샵이 없으면 대기 계약 건수 조회도 같은 메시지로 실패한다")
        void givenMissingShop_whenGetPendingApplicationCount_thenThrowIllegalArgumentException() {
            given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> contractService.getPendingApplicationCount(USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Shop not found for user: 1");
            verify(contractRepository, never()).countByShopUserIdAndContractStatus(any(), any());
        }

        @Test
        @DisplayName("샵이 있으면 대기 계약 건수와 작가 이름을 반환한다")
        void givenShop_whenGetPendingApplicationCount_thenReturnCountAndNames() {
            Shop shop = shopWithId(SHOP_ID);
            given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
            given(contractRepository.countByShopUserIdAndContractStatus(USER_ID, ContractStatus.PENDING))
                    .willReturn(2L);
            given(contractRepository.findPendingApplicationArtistNamesByShopUserId(USER_ID, ContractStatus.PENDING))
                    .willReturn(List.of("작가 A", "작가 B"));

            ContractApplicationCountResponse response = contractService.getPendingApplicationCount(USER_ID);

            assertThat(response.getApplicationCount()).isEqualTo(2L);
            assertThat(response.getArtistNames()).containsExactly("작가 A", "작가 B");
        }
    }

    @Nested
    @DisplayName("adjustInventory")
    class AdjustInventoryTest {

        @Test
        @DisplayName("샵 소유 재고 품목이면 movement를 저장하고 갱신된 스냅샷을 반환한다")
        void givenOwnedInventory_whenAdjustInventory_thenSaveMovementAndReturnSnapshot() {
            Shop shop = shopWithId(SHOP_ID);
            ContractProduct contractProduct = contractProductWithId(CONTRACT_PRODUCT_ID);
            AdjustContractInventoryRequest request = request(
                    ContractProductStockMovementType.INBOUND,
                    4,
                    OCCURRED_AT,
                    "추가 입고"
            );

            given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
            given(contractProductRepository.findOwnedInventoryById(
                    CONTRACT_PRODUCT_ID,
                    SHOP_ID,
                    ContractStatus.APPROVED,
                    ContractProductListingStatus.ACTIVE
            ))
                    .willReturn(Optional.of(contractProduct));

            AdjustContractInventoryResponse response = contractService.adjustInventory(
                    USER_ID,
                    CONTRACT_PRODUCT_ID,
                    request
            );

            ArgumentCaptor<ContractProductStockMovement> movementCaptor =
                    ArgumentCaptor.forClass(ContractProductStockMovement.class);
            verify(contractProductStockMovementRepository).save(movementCaptor.capture());

            assertThat(response.getContractProductId()).isEqualTo(CONTRACT_PRODUCT_ID);
            assertThat(response.getMovementType()).isEqualTo(ContractProductStockMovementType.INBOUND);
            assertThat(response.getQuantityDelta()).isEqualTo(4);
            assertThat(response.getStockQuantity()).isEqualTo(4);
            assertThat(response.getRecentStockedAt()).isEqualTo(OCCURRED_AT);
            assertThat(contractProduct.getShopArtistContract().getRecentInboundConfirmed()).isFalse();
            assertThat(movementCaptor.getValue().getResultStockQuantity()).isEqualTo(4);
        }

        @Test
        @DisplayName("샵 소유 재고 품목이 아니면 찾을 수 없다고 응답한다")
        void givenNotOwnedInventory_whenAdjustInventory_thenThrowEntityNotFoundException() {
            Shop shop = shopWithId(SHOP_ID);
            AdjustContractInventoryRequest request = request(
                    ContractProductStockMovementType.INBOUND,
                    4,
                    OCCURRED_AT,
                    "추가 입고"
            );

            given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
            given(contractProductRepository.findOwnedInventoryById(
                    CONTRACT_PRODUCT_ID,
                    SHOP_ID,
                    ContractStatus.APPROVED,
                    ContractProductListingStatus.ACTIVE
            ))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> contractService.adjustInventory(USER_ID, CONTRACT_PRODUCT_ID, request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("재고 품목을 찾을 수 없습니다.");
        }
    }

    private Shop shopWithId(Long shopId) {
        User user = User.builder()
                .id(USER_ID)
                .build();
        return Shop.builder()
                .shopId(shopId)
                .user(user)
                .build();
    }

    private ContractProduct contractProductWithId(Long contractProductId) {
        ShopArtistContract contract = contractWithId(CONTRACT_ID);
        contract.confirmRecentInbound(USER_ID, OCCURRED_AT.minusDays(1));
        ContractProduct contractProduct = ContractProduct.builder()
                .contractProductsId(contractProductId)
                .shopArtistContract(contract)
                .sellingPrice(new BigDecimal("12000.00"))
                .build();
        setField(contractProduct, "stockQuantity", 0);
        setField(contractProduct, "soldQuantity", 0);
        return contractProduct;
    }

    private ShopArtistContract contractWithId(Long contractId) {
        return ShopArtistContract.builder()
                .shopArtistContractsId(contractId)
                .contractStatus(ContractStatus.APPROVED)
                .commissionType(CommissionType.RATE)
                .commissionValue(new BigDecimal("20.0000"))
                .build();
    }

    private AdjustContractInventoryRequest request(
            ContractProductStockMovementType movementType,
            int quantity,
            LocalDateTime occurredAt,
            String memo
    ) {
        AdjustContractInventoryRequest request = new AdjustContractInventoryRequest();
        setField(request, "movementType", movementType);
        setField(request, "quantity", quantity);
        setField(request, "occurredAt", occurredAt);
        setField(request, "memo", memo);
        return request;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("테스트 필드 주입에 실패했습니다: " + fieldName, exception);
        }
    }
}
