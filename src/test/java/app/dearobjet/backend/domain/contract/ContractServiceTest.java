package app.dearobjet.backend.domain.contract;

import app.dearobjet.backend.domain.contract.dto.AdjustContractInventoryRequest;
import app.dearobjet.backend.domain.contract.dto.AdjustContractInventoryResponse;
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
import app.dearobjet.backend.domain.user.enums.Specialty;
import app.dearobjet.backend.domain.user.repository.ShopRepository;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DisplayName("ContractService 재고관리 API 흐름 테스트")
@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long SHOP_ID = 10L;
    private static final Long CONTRACT_ID = 50L;
    private static final Long CONTRACT_PRODUCT_ID = 100L;
    private static final LocalDateTime OCCURRED_AT = LocalDateTime.of(2026, 4, 28, 16, 0);
    private static final String UPDATED_MEMO = "Postman에서 수정한 계약 작가 메모";

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

    @Test
    @DisplayName("재고관리 목록에서 계약 작가를 조회한다")
    void givenShop_whenGetInventory_thenReturnContractedArtists() {
        Shop shop = shopWithId(SHOP_ID);
        ContractInventoryRow row = inventoryRow();

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
        given(contractRepository.findInventoryRowsByShopId(
                SHOP_ID,
                ContractStatus.APPROVED,
                ContractProductListingStatus.ACTIVE
        )).willReturn(List.of(row));

        ContractInventoryListResponse response = contractService.getInventory(USER_ID);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getContractId()).isEqualTo(CONTRACT_ID);
        assertThat(response.getItems().get(0).getArtistName()).isEqualTo("Postman 도자기 작가");
        assertThat(response.getItems().get(0).getInboundConfirmed()).isFalse();
    }

    @Test
    @DisplayName("계약 작가 메모를 수정한다")
    void givenApprovedContract_whenUpdateMemo_thenReturnUpdatedMemo() {
        Shop shop = shopWithId(SHOP_ID);
        ShopArtistContract contract = contractWithId(CONTRACT_ID);
        UpdateContractMemoRequest request = new UpdateContractMemoRequest();
        setField(request, "memo", UPDATED_MEMO);

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
        given(contractRepository.findApprovedByIdAndShopId(CONTRACT_ID, SHOP_ID, ContractStatus.APPROVED))
                .willReturn(Optional.of(contract));

        ContractMemoResponse response = contractService.updateInventoryMemo(USER_ID, CONTRACT_ID, request);

        assertThat(response.getContractId()).isEqualTo(CONTRACT_ID);
        assertThat(response.getMemo()).isEqualTo(UPDATED_MEMO);
    }

    @Test
    @DisplayName("계약 작가의 최근 입고를 확인 처리한다")
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

    @Test
    @DisplayName("신규 입고 등록 시 재고를 증가시키고 입고확인을 미확인으로 되돌린다")
    void givenInboundRequest_whenAdjustInventory_thenIncreaseStockAndResetConfirmation() {
        Shop shop = shopWithId(SHOP_ID);
        ContractProduct contractProduct = contractProductWithConfirmedInbound(CONTRACT_PRODUCT_ID);
        AdjustContractInventoryRequest request = inventoryRequest();

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
        given(contractProductRepository.findOwnedInventoryById(
                CONTRACT_PRODUCT_ID,
                SHOP_ID,
                ContractStatus.APPROVED,
                ContractProductListingStatus.ACTIVE
        )).willReturn(Optional.of(contractProduct));

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
        assertThat(response.getQuantityDelta()).isEqualTo(3);
        assertThat(response.getStockQuantity()).isEqualTo(3);
        assertThat(response.getRecentStockedAt()).isEqualTo(OCCURRED_AT);
        assertThat(contractProduct.getShopArtistContract().getRecentInboundConfirmed()).isFalse();
        assertThat(movementCaptor.getValue().getResultStockQuantity()).isEqualTo(3);
    }

    private ContractInventoryRow inventoryRow() {
        return new ContractInventoryRow() {
            @Override
            public Long getContractId() {
                return CONTRACT_ID;
            }

            @Override
            public String getArtistImageUrl() {
                return "https://image.test/artist-a.png";
            }

            @Override
            public String getArtistName() {
                return "Postman 도자기 작가";
            }

            @Override
            public Specialty getSpecialty() {
                return Specialty.CERAMIC;
            }

            @Override
            public LocalDateTime getRecentStockedAt() {
                return OCCURRED_AT;
            }

            @Override
            public Boolean getInboundConfirmed() {
                return false;
            }
        };
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

    private ContractProduct contractProductWithConfirmedInbound(Long contractProductId) {
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

    private AdjustContractInventoryRequest inventoryRequest() {
        AdjustContractInventoryRequest request = new AdjustContractInventoryRequest();
        setField(request, "movementType", ContractProductStockMovementType.INBOUND);
        setField(request, "quantity", 3);
        setField(request, "occurredAt", OCCURRED_AT);
        setField(request, "memo", "Postman 추가 입고");
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
