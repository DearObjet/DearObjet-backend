package app.dearobjet.backend.domain.contract;

import app.dearobjet.backend.domain.contract.dto.AdjustContractInventoryRequest;
import app.dearobjet.backend.domain.contract.dto.AdjustContractInventoryResponse;
import app.dearobjet.backend.domain.contract.dto.ArtistAccountSearchResponse;
import app.dearobjet.backend.domain.contract.dto.ArtistSuggestionListResponse;
import app.dearobjet.backend.domain.contract.dto.CompletedContractDocumentListResponse;
import app.dearobjet.backend.domain.contract.dto.ContractInboundConfirmResponse;
import app.dearobjet.backend.domain.contract.dto.ContractInventoryListResponse;
import app.dearobjet.backend.domain.contract.dto.ContractMemoResponse;
import app.dearobjet.backend.domain.contract.dto.ContractDocumentResponse;
import app.dearobjet.backend.domain.contract.dto.ContractTemplateResponse;
import app.dearobjet.backend.domain.contract.dto.ContractTerminationResponse;
import app.dearobjet.backend.domain.contract.dto.DeleteCompletedContractDocumentsRequest;
import app.dearobjet.backend.domain.contract.dto.DeleteCompletedContractDocumentsResponse;
import app.dearobjet.backend.domain.contract.dto.InProgressContractDocumentListResponse;
import app.dearobjet.backend.domain.contract.dto.ManagedArtistContractDetailResponse;
import app.dearobjet.backend.domain.contract.dto.ManagedArtistContractListResponse;
import app.dearobjet.backend.domain.contract.dto.ManagedShopContractActionResultResponse;
import app.dearobjet.backend.domain.contract.dto.ManagedShopContractDetailResponse;
import app.dearobjet.backend.domain.contract.dto.ManagedShopContractListResponse;
import app.dearobjet.backend.domain.contract.dto.SendContractRequest;
import app.dearobjet.backend.domain.contract.dto.SubmitArtistContractRequest;
import app.dearobjet.backend.domain.contract.dto.UpdateContractMemoRequest;
import app.dearobjet.backend.domain.contract.dto.projection.ContractInventoryRow;
import app.dearobjet.backend.domain.contract.dto.projection.ArtistAccountSearchRow;
import app.dearobjet.backend.domain.contract.dto.projection.ArtistSuggestionRow;
import app.dearobjet.backend.domain.contract.dto.projection.CompletedContractDocumentRow;
import app.dearobjet.backend.domain.contract.dto.projection.InProgressContractDocumentRow;
import app.dearobjet.backend.domain.contract.dto.projection.ManagedArtistContractRow;
import app.dearobjet.backend.domain.contract.dto.projection.ManagedShopContractRow;
import app.dearobjet.backend.domain.contract.entity.ContractProduct;
import app.dearobjet.backend.domain.contract.entity.ContractProductStockMovement;
import app.dearobjet.backend.domain.contract.entity.ShopArtistContract;
import app.dearobjet.backend.domain.contract.enums.CommissionType;
import app.dearobjet.backend.domain.contract.enums.ContractDocumentStatus;
import app.dearobjet.backend.domain.contract.enums.ContractRequestType;
import app.dearobjet.backend.domain.contract.enums.ContractProductListingStatus;
import app.dearobjet.backend.domain.contract.enums.ContractProductStockMovementType;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import app.dearobjet.backend.domain.contract.repository.ContractProductRepository;
import app.dearobjet.backend.domain.contract.repository.ContractProductStockMovementRepository;
import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.user.entity.BusinessProfile;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.enums.Specialty;
import app.dearobjet.backend.domain.user.enums.Role;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import app.dearobjet.backend.domain.user.repository.ArtistRepository;
import app.dearobjet.backend.domain.user.repository.ShopRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.DuplicateEntityException;
import app.dearobjet.backend.global.exception.InvalidInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
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
import static org.mockito.Mockito.verify;

@DisplayName("ContractService 재고관리 API 흐름 테스트")
@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long SHOP_ID = 10L;
    private static final Long CONTRACT_ID = 50L;
    private static final Long CONTRACT_PRODUCT_ID = 100L;
    private static final Long ARTIST_ID = 20L;
    private static final LocalDate CONTRACT_START_DATE = LocalDate.of(2026, 5, 1);
    private static final LocalDate CONTRACT_END_DATE = LocalDate.of(2026, 12, 31);
    private static final LocalDate CONTRACT_DATE = LocalDate.of(2026, 4, 30);
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

    @Mock
    private ArtistRepository artistRepository;

    @Test
    @DisplayName("계약서 템플릿은 소품샵 사업자 정보로 기본값을 채운다")
    void givenShopProfile_whenGetContractTemplate_thenReturnPrefilledShopFields() {
        Shop shop = shopWithContractProfile(SHOP_ID);

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));

        ContractTemplateResponse response = contractService.getContractTemplate(USER_ID);

        assertThat(response.getShopBusinessName()).isEqualTo("템플릿 소품샵");
        assertThat(response.getShopOwnerName()).isEqualTo("대표자 A");
        assertThat(response.getShopBusinessNumber()).isEqualTo("123-45-67890");
        assertThat(response.getShopAddress()).isEqualTo("서울시 마포구");
        assertThat(response.getShopContact()).isEqualTo("01011112222");
        assertThat(response.getShopSignatureBusinessName()).isEqualTo("템플릿 소품샵");
        assertThat(response.getShopSignatureOwnerName()).isEqualTo("대표자 A");
        assertThat(response.getArtistName()).isEmpty();
    }

    @Test
    @DisplayName("소품샵 완료 계약서 목록을 조회한다")
    void givenCompletedDocuments_whenGetCompletedContractDocuments_thenReturnDocumentRows() {
        Shop shop = shopWithId(SHOP_ID);

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
        given(contractRepository.findCompletedDocumentRowsByShopId(
                SHOP_ID,
                ContractStatus.APPROVED,
                ContractDocumentStatus.APPROVED
        )).willReturn(List.of(completedContractDocumentRow(CONTRACT_ID)));

        CompletedContractDocumentListResponse response =
                contractService.getCompletedContractDocuments(USER_ID);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getContractId()).isEqualTo(CONTRACT_ID);
        assertThat(response.getItems().get(0).getTitle()).isEqualTo("입점 계약서");
        assertThat(response.getItems().get(0).getContractDate()).isEqualTo(CONTRACT_DATE);
        assertThat(response.getItems().get(0).getArtistId()).isEqualTo(ARTIST_ID);
        assertThat(response.getItems().get(0).getArtistName()).isEqualTo("Postman 도자기 작가");
    }

    @Test
    @DisplayName("소품샵은 작성중 계약서 목록에서 요청한 작가 계약서를 조회한다")
    void givenShopUser_whenGetInProgressContractDocuments_thenReturnArtistCounterpartyRows() {
        Shop shop = shopWithId(SHOP_ID);

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
        given(contractRepository.findInProgressDocumentRowsByShopId(
                SHOP_ID,
                ContractStatus.PENDING,
                List.of(ContractDocumentStatus.SHOP_SENT, ContractDocumentStatus.ARTIST_SUBMITTED)
        )).willReturn(List.of(inProgressContractDocumentRow(
                CONTRACT_ID,
                ARTIST_ID,
                "Postman 도자기 작가",
                ContractDocumentStatus.SHOP_SENT
        )));

        InProgressContractDocumentListResponse response =
                contractService.getInProgressContractDocuments(USER_ID);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getContractId()).isEqualTo(CONTRACT_ID);
        assertThat(response.getItems().get(0).getViewerType()).isEqualTo("SHOP");
        assertThat(response.getItems().get(0).getCounterpartyId()).isEqualTo(ARTIST_ID);
        assertThat(response.getItems().get(0).getCounterpartyName()).isEqualTo("Postman 도자기 작가");
        assertThat(response.getItems().get(0).getContractDocumentStatus()).isEqualTo(ContractDocumentStatus.SHOP_SENT);
        assertThat(response.getItems().get(0).getDetailApiPath())
                .isEqualTo("/api/v1/contracts/artists/" + CONTRACT_ID);
    }

    @Test
    @DisplayName("작가는 작성중 계약서 목록에서 요청한 소품샵 계약서를 조회한다")
    void givenArtistUser_whenGetInProgressContractDocuments_thenReturnShopCounterpartyRows() {
        Artist artist = artistWithId(ARTIST_ID);

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.empty());
        given(artistRepository.findByUserId(USER_ID)).willReturn(Optional.of(artist));
        given(contractRepository.findInProgressDocumentRowsByArtistId(
                ARTIST_ID,
                ContractStatus.PENDING,
                List.of(ContractDocumentStatus.SHOP_SENT, ContractDocumentStatus.ARTIST_SUBMITTED)
        )).willReturn(List.of(inProgressContractDocumentRow(
                CONTRACT_ID,
                SHOP_ID,
                "Postman 테스트 소품샵",
                ContractDocumentStatus.ARTIST_SUBMITTED
        )));

        InProgressContractDocumentListResponse response =
                contractService.getInProgressContractDocuments(USER_ID);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getContractId()).isEqualTo(CONTRACT_ID);
        assertThat(response.getItems().get(0).getViewerType()).isEqualTo("ARTIST");
        assertThat(response.getItems().get(0).getCounterpartyId()).isEqualTo(SHOP_ID);
        assertThat(response.getItems().get(0).getCounterpartyName()).isEqualTo("Postman 테스트 소품샵");
        assertThat(response.getItems().get(0).getContractDocumentStatus())
                .isEqualTo(ContractDocumentStatus.ARTIST_SUBMITTED);
        assertThat(response.getItems().get(0).getDetailApiPath())
                .isEqualTo("/api/v1/contracts/shops/" + CONTRACT_ID);
    }

    @Test
    @DisplayName("완료 계약서를 소품샵 계약서 관리 목록에서 숨김 처리한다")
    void givenCompletedDocuments_whenDeleteCompletedContractDocuments_thenHideDocumentsForShop() {
        Shop shop = shopWithId(SHOP_ID);
        ShopArtistContract contract = approvedDocumentContract();
        DeleteCompletedContractDocumentsRequest request = deleteCompletedDocumentsRequest(CONTRACT_ID);

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
        given(contractRepository.findAllByIdsAndShopId(List.of(CONTRACT_ID), SHOP_ID))
                .willReturn(List.of(contract));

        DeleteCompletedContractDocumentsResponse response =
                contractService.deleteCompletedContractDocuments(USER_ID, request);

        assertThat(response.getDeletedCount()).isEqualTo(1);
        assertThat(response.getDeletedContractIds()).containsExactly(CONTRACT_ID);
        assertThat(contract.getShopDocumentDeletedAt()).isNotNull();
        assertThat(contract.getShopDocumentDeletedByUserId()).isEqualTo(USER_ID);
        assertThat(contract.getContractStatus()).isEqualTo(ContractStatus.APPROVED);
        assertThat(contract.getContractDocumentStatus()).isEqualTo(ContractDocumentStatus.APPROVED);
    }

    @Test
    @DisplayName("소품샵 소유가 아닌 계약서가 포함되면 삭제할 수 없다")
    void givenNotOwnedDocument_whenDeleteCompletedContractDocuments_thenThrowException() {
        Shop shop = shopWithId(SHOP_ID);
        DeleteCompletedContractDocumentsRequest request = deleteCompletedDocumentsRequest(CONTRACT_ID);

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
        given(contractRepository.findAllByIdsAndShopId(List.of(CONTRACT_ID), SHOP_ID))
                .willReturn(List.of());

        assertThatThrownBy(() -> contractService.deleteCompletedContractDocuments(USER_ID, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("삭제할 계약서 중 소품샵 소유가 아니거나 존재하지 않는 계약서가 있습니다.");
    }

    @Test
    @DisplayName("미완료 계약서는 삭제할 수 없다")
    void givenIncompleteDocument_whenDeleteCompletedContractDocuments_thenThrowException() {
        Shop shop = shopWithId(SHOP_ID);
        ShopArtistContract contract = shopSentContract();
        DeleteCompletedContractDocumentsRequest request = deleteCompletedDocumentsRequest(CONTRACT_ID);

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
        given(contractRepository.findAllByIdsAndShopId(List.of(CONTRACT_ID), SHOP_ID))
                .willReturn(List.of(contract));

        assertThatThrownBy(() -> contractService.deleteCompletedContractDocuments(USER_ID, request))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("완료된 계약서만 삭제할 수 있습니다.");
    }

    @Test
    @DisplayName("소품샵이 계약서를 작성해 작가에게 발송한다")
    void givenShopContractRequest_whenSendContractToArtist_thenCreatePendingContractDocument() {
        Shop shop = shopWithId(SHOP_ID);
        Artist artist = artistWithId(ARTIST_ID);
        SendContractRequest request = sendContractRequest();

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
        given(artistRepository.findById(ARTIST_ID)).willReturn(Optional.of(artist));
        given(contractRepository.existsPendingOrCurrentApprovedContract(
                SHOP_ID,
                ARTIST_ID,
                ContractStatus.PENDING,
                ContractStatus.APPROVED,
                LocalDate.now()
        )).willReturn(false);
        given(contractRepository.save(any(ShopArtistContract.class))).willAnswer(invocation -> invocation.getArgument(0));

        ContractDocumentResponse response = contractService.sendContractToArtist(USER_ID, ARTIST_ID, request);

        assertThat(response.getContractStatus()).isEqualTo(ContractStatus.PENDING);
        assertThat(response.getContractDocumentStatus()).isEqualTo(ContractDocumentStatus.SHOP_SENT);
        assertThat(response.getShopBusinessName()).isEqualTo("Postman 테스트 소품샵");
        assertThat(response.getCommissionRate()).isEqualTo(new BigDecimal("20.0000"));
        assertThat(response.getSettlementDay()).isEqualTo(15);
        assertThat(response.getPaymentDay()).isEqualTo(10);
        assertThat(response.getArtistName()).isEmpty();
        verify(contractRepository).save(any(ShopArtistContract.class));
    }

    @Test
    @DisplayName("이미 진행 중인 계약이 있으면 계약서를 발송할 수 없다")
    void givenExistingActiveContract_whenSendContractToArtist_thenThrowException() {
        Shop shop = shopWithId(SHOP_ID);
        Artist artist = artistWithId(ARTIST_ID);

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
        given(artistRepository.findById(ARTIST_ID)).willReturn(Optional.of(artist));
        given(contractRepository.existsPendingOrCurrentApprovedContract(
                SHOP_ID,
                ARTIST_ID,
                ContractStatus.PENDING,
                ContractStatus.APPROVED,
                LocalDate.now()
        )).willReturn(true);

        assertThatThrownBy(() -> contractService.sendContractToArtist(USER_ID, ARTIST_ID, sendContractRequest()))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 진행 중이거나 유효한 계약이 있는 작가입니다.");
    }

    @Test
    @DisplayName("작가가 계약서 나머지 내용을 작성해 제출한다")
    void givenShopSentContract_whenSubmitArtistContract_thenChangeToArtistSubmitted() {
        ShopArtistContract contract = shopSentContract();
        givenArtistOwnedContract(contract);

        ContractDocumentResponse response =
                contractService.submitArtistContract(USER_ID, CONTRACT_ID, submitArtistContractRequest());

        assertThat(response.getContractDocumentStatus()).isEqualTo(ContractDocumentStatus.ARTIST_SUBMITTED);
        assertThat(response.getArtistName()).isEqualTo("Postman 도자기 작가");
        assertThat(response.getArtistBusinessNumber()).isEmpty();
        assertThat(response.getArtistBankName()).isEqualTo("국민은행");
        assertThat(response.getArtistAccountNumber()).isEqualTo("1234567890");
    }

    @Test
    @DisplayName("작가 작성 완료 계약서만 소품샵이 승인할 수 있다")
    void givenArtistSubmittedContract_whenApproveContract_thenChangeToApproved() {
        Shop shop = shopWithId(SHOP_ID);
        ShopArtistContract contract = artistSubmittedContract();

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
        given(contractRepository.findManagedArtistContractByIdAndShopId(
                CONTRACT_ID,
                SHOP_ID,
                List.of(ContractStatus.PENDING, ContractStatus.APPROVED, ContractStatus.ENDED)
        )).willReturn(Optional.of(contract));

        ContractDocumentResponse response = contractService.approveContract(USER_ID, CONTRACT_ID);

        assertThat(response.getContractStatus()).isEqualTo(ContractStatus.APPROVED);
        assertThat(response.getContractDocumentStatus()).isEqualTo(ContractDocumentStatus.APPROVED);
        assertThat(contract.getContractStatus()).isEqualTo(ContractStatus.APPROVED);
    }

    @Test
    @DisplayName("작가 작성 전 계약서는 승인할 수 없다")
    void givenShopSentContract_whenApproveContract_thenThrowException() {
        Shop shop = shopWithId(SHOP_ID);
        ShopArtistContract contract = shopSentContract();

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
        given(contractRepository.findManagedArtistContractByIdAndShopId(
                CONTRACT_ID,
                SHOP_ID,
                List.of(ContractStatus.PENDING, ContractStatus.APPROVED, ContractStatus.ENDED)
        )).willReturn(Optional.of(contract));

        assertThatThrownBy(() -> contractService.approveContract(USER_ID, CONTRACT_ID))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("작가 작성 완료 후에만 계약 승인할 수 있습니다.");
    }

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
    @DisplayName("입점 작가 제안 목록에서 랜덤 최대 10명을 조회한다")
    void givenEligibleArtists_whenGetArtistSuggestions_thenReturnUpToTenArtists() {
        Shop shop = shopWithId(SHOP_ID);
        List<ArtistSuggestionRow> rows = java.util.stream.LongStream.rangeClosed(1, 12)
                .mapToObj(index -> artistSuggestionRow(
                        ARTIST_ID + index,
                        USER_ID + index,
                        "추천 작가 " + index
                ))
                .toList();

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
        given(contractRepository.findArtistSuggestionRows(
                eq(SHOP_ID),
                eq(Role.ARTIST),
                eq(UserStatus.ACTIVE),
                eq(ContractStatus.PENDING),
                eq(ContractStatus.APPROVED),
                any(LocalDate.class)
        )).willReturn(rows);

        ArtistSuggestionListResponse response = contractService.getArtistSuggestions(USER_ID);

        assertThat(response.getItems()).hasSize(10);
        assertThat(response.getItems())
                .allSatisfy(item -> {
                    assertThat(item.getArtistId()).isNotNull();
                    assertThat(item.getUserId()).isNotNull();
                    assertThat(item.getArtistName()).startsWith("추천 작가 ");
                    assertThat(item.getSpecialty()).isEqualTo(Specialty.CERAMIC);
                });
    }

    @Test
    @DisplayName("입점 작가 제안 목록에 채팅방 생성용 사용자 ID를 포함한다")
    void givenEligibleArtist_whenGetArtistSuggestions_thenReturnArtistAndUserIdentifiers() {
        Shop shop = shopWithId(SHOP_ID);

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
        given(contractRepository.findArtistSuggestionRows(
                eq(SHOP_ID),
                eq(Role.ARTIST),
                eq(UserStatus.ACTIVE),
                eq(ContractStatus.PENDING),
                eq(ContractStatus.APPROVED),
                any(LocalDate.class)
        )).willReturn(List.of(artistSuggestionRow(ARTIST_ID, USER_ID + 100, "Postman 추천 작가")));

        ArtistSuggestionListResponse response = contractService.getArtistSuggestions(USER_ID);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getArtistId()).isEqualTo(ARTIST_ID);
        assertThat(response.getItems().get(0).getUserId()).isEqualTo(USER_ID + 100);
        assertThat(response.getItems().get(0).getArtistName()).isEqualTo("Postman 추천 작가");
        assertThat(response.getItems().get(0).getArtistImageUrl()).isEqualTo("https://image.test/suggested.png");
        assertThat(response.getItems().get(0).getInstagramId()).isEqualTo("suggested_artist");
    }

    @Test
    @DisplayName("계약서 발송용 작가 계정을 검색한다")
    void givenKeyword_whenSearchArtistAccounts_thenReturnMatchedArtistAccounts() {
        Shop shop = shopWithId(SHOP_ID);

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
        given(contractRepository.searchArtistAccountRows(
                eq(SHOP_ID),
                eq(Role.ARTIST),
                eq(UserStatus.ACTIVE),
                eq(ContractStatus.PENDING),
                eq(ContractStatus.APPROVED),
                any(LocalDate.class),
                eq("%postman%"),
                eq(org.springframework.data.domain.PageRequest.of(0, 10))
        )).willReturn(List.of(artistAccountSearchRow(ARTIST_ID, USER_ID + 100, "Postman 검색 작가")));

        ArtistAccountSearchResponse response = contractService.searchArtistAccounts(USER_ID, " Postman ");

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getArtistId()).isEqualTo(ARTIST_ID);
        assertThat(response.getItems().get(0).getUserId()).isEqualTo(USER_ID + 100);
        assertThat(response.getItems().get(0).getUserName()).isEqualTo("Postman 검색 사용자");
        assertThat(response.getItems().get(0).getArtistName()).isEqualTo("Postman 검색 작가");
        assertThat(response.getItems().get(0).getEmail()).isEqualTo("searched-artist@test.com");
    }

    @Test
    @DisplayName("계약서 발송용 작가 계정 검색어가 2자 미만이면 빈 목록을 반환한다")
    void givenShortKeyword_whenSearchArtistAccounts_thenReturnEmptyItems() {
        Shop shop = shopWithId(SHOP_ID);

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));

        ArtistAccountSearchResponse response = contractService.searchArtistAccounts(USER_ID, "a");

        assertThat(response.getItems()).isEmpty();
    }

    @Test
    @DisplayName("입점관리 작가 목록에서 계약 대기, 계약중, 계약 만료 작가를 조회한다")
    void givenManagedContracts_whenGetManagedArtists_thenReturnArtistRowsWithNextAction() {
        Shop shop = shopWithId(SHOP_ID);

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
        given(contractRepository.findManagedArtistRowsByShopId(
                SHOP_ID,
                List.of(ContractStatus.PENDING, ContractStatus.APPROVED, ContractStatus.ENDED),
                ContractStatus.PENDING,
                ContractStatus.APPROVED,
                ContractStatus.ENDED
        )).willReturn(List.of(
                managedArtistRow(1L, ContractStatus.PENDING, ContractRequestType.EXTENSION, "연장 대기 작가"),
                managedArtistRow(2L, ContractStatus.PENDING, ContractRequestType.RELEASE, "해제 대기 작가"),
                managedArtistRow(3L, ContractStatus.APPROVED, ContractRequestType.NONE, "계약 작가"),
                managedArtistRow(
                        4L,
                        ContractStatus.APPROVED,
                        ContractRequestType.NONE,
                        "해지 가능 작가",
                        LocalDate.now().minusDays(14)
                )
        ));

        ManagedArtistContractListResponse response = contractService.getManagedArtists(USER_ID);

        assertThat(response.getItems()).hasSize(4);
        assertThat(response.getItems().get(0).getContractStatusLabel()).isEqualTo("계약 대기");
        assertThat(response.getItems().get(0).getNextAction().getCode()).isEqualTo("CONTRACT_APPROVE");
        assertThat(response.getItems().get(1).getContractStatusLabel()).isEqualTo("계약 대기");
        assertThat(response.getItems().get(1).getNextAction().getCode()).isEqualTo("RELEASE_APPROVE");
        assertThat(response.getItems().get(2).getContractStatusLabel()).isEqualTo("계약중");
        assertThat(response.getItems().get(2).getNextAction().getCode()).isEqualTo("NONE");
        assertThat(response.getItems().get(3).getContractStatus()).isEqualTo(ContractStatus.ENDED);
        assertThat(response.getItems().get(3).getContractStatusLabel()).isEqualTo("계약 만료");
        assertThat(response.getItems().get(3).getNextAction().getCode()).isEqualTo("TERMINATE_CONTRACT");
        assertThat(response.getItems().get(3).getDetailAvailable()).isTrue();
    }

    @Test
    @DisplayName("입점처 목록에서 계약 완료, 계약 대기, 계약 연장, 계약만료 상태를 조회한다")
    void givenArtistContracts_whenGetManagedShops_thenReturnShopRowsWithActions() {
        Artist artist = artistWithId(ARTIST_ID);

        given(artistRepository.findByUserId(USER_ID)).willReturn(Optional.of(artist));
        given(contractRepository.findManagedShopRowsByArtistId(
                org.mockito.ArgumentMatchers.eq(ARTIST_ID),
                org.mockito.ArgumentMatchers.eq(List.of(ContractStatus.PENDING, ContractStatus.APPROVED, ContractStatus.ENDED)),
                org.mockito.ArgumentMatchers.eq(ContractStatus.TERMINATED),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class),
                org.mockito.ArgumentMatchers.eq(ContractStatus.PENDING),
                org.mockito.ArgumentMatchers.eq(ContractStatus.APPROVED),
                org.mockito.ArgumentMatchers.eq(ContractStatus.ENDED)
        )).willReturn(List.of(
                managedShopRow(1L, ContractStatus.APPROVED, ContractRequestType.NONE, "계약 완료 소품샵"),
                managedShopRow(2L, ContractStatus.PENDING, ContractRequestType.EXTENSION, "계약 대기 소품샵"),
                managedShopRow(
                        3L,
                        ContractStatus.APPROVED,
                        ContractRequestType.NONE,
                        "계약 연장 소품샵",
                        LocalDate.now().minusDays(1),
                        null
                ),
                managedShopRow(4L, ContractStatus.PENDING, ContractRequestType.RELEASE, "해제 신청 소품샵"),
                managedShopRow(5L, ContractStatus.TERMINATED, ContractRequestType.NONE, "계약만료 소품샵")
        ));

        ManagedShopContractListResponse response = contractService.getManagedShops(USER_ID);

        assertThat(response.getItems()).hasSize(5);
        assertThat(response.getItems().get(0).getStatusLabel()).isEqualTo("계약 완료");
        assertThat(response.getItems().get(0).getActions().get(0).getLabel()).isEqualTo("계약승인");
        assertThat(response.getItems().get(0).getActions().get(0).getEnabled()).isFalse();
        assertThat(response.getItems().get(1).getStatusLabel()).isEqualTo("계약 대기");
        assertThat(response.getItems().get(2).getStatusLabel()).isEqualTo("계약 연장");
        assertThat(response.getItems().get(2).getActions()).extracting("code")
                .containsExactly("EXTENSION_REQUEST", "RELEASE_REQUEST");
        assertThat(response.getItems().get(3).getStatusLabel()).isEqualTo("계약 연장");
        assertThat(response.getItems().get(3).getStatusDisabled()).isTrue();
        assertThat(response.getItems().get(3).getActions().get(0).getLabel()).isEqualTo("해제취소");
        assertThat(response.getItems().get(4).getStatusLabel()).isEqualTo("계약만료");
    }

    @Test
    @DisplayName("입점처 계약서 상세를 조회한다")
    void givenArtistOwnedContract_whenGetManagedShopContract_thenReturnDetail() {
        Artist artist = artistWithId(ARTIST_ID);
        ShopArtistContract contract = contractWithArtistAndShop(CONTRACT_ID, ContractStatus.APPROVED);

        given(artistRepository.findByUserId(USER_ID)).willReturn(Optional.of(artist));
        given(contractRepository.findManagedShopContractByIdAndArtistId(
                org.mockito.ArgumentMatchers.eq(CONTRACT_ID),
                org.mockito.ArgumentMatchers.eq(ARTIST_ID),
                org.mockito.ArgumentMatchers.eq(List.of(ContractStatus.PENDING, ContractStatus.APPROVED, ContractStatus.ENDED)),
                org.mockito.ArgumentMatchers.eq(ContractStatus.TERMINATED),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)
        )).willReturn(Optional.of(contract));

        ManagedShopContractDetailResponse response = contractService.getManagedShopContract(USER_ID, CONTRACT_ID);

        assertThat(response.getContractId()).isEqualTo(CONTRACT_ID);
        assertThat(response.getShopId()).isEqualTo(SHOP_ID);
        assertThat(response.getShopName()).isEqualTo("Postman 테스트 소품샵");
        assertThat(response.getArtistId()).isEqualTo(ARTIST_ID);
        assertThat(response.getArtistName()).isEqualTo("Postman 도자기 작가");
        assertThat(response.getContractStatus()).isEqualTo(ContractStatus.APPROVED);
    }

    @Test
    @DisplayName("작가가 계약연장을 신청한다")
    void givenRenewableContract_whenRequestManagedShopContractExtension_thenChangeToPendingExtension() {
        ShopArtistContract contract = contractWithArtistAndShop(
                CONTRACT_ID,
                ContractStatus.APPROVED,
                ContractRequestType.NONE,
                CONTRACT_START_DATE.minusYears(1),
                LocalDate.now().minusDays(1)
        );
        givenArtistOwnedContract(contract);

        ManagedShopContractActionResultResponse response =
                contractService.requestManagedShopContractExtension(USER_ID, CONTRACT_ID);

        assertThat(response.getContractStatus()).isEqualTo(ContractStatus.PENDING);
        assertThat(response.getContractRequestType()).isEqualTo(ContractRequestType.EXTENSION);
        assertThat(contract.getContractStatus()).isEqualTo(ContractStatus.PENDING);
        assertThat(contract.getContractRequestType()).isEqualTo(ContractRequestType.EXTENSION);
    }

    @Test
    @DisplayName("작가가 해제신청을 한다")
    void givenRenewableContract_whenRequestManagedShopContractRelease_thenChangeToPendingRelease() {
        ShopArtistContract contract = contractWithArtistAndShop(
                CONTRACT_ID,
                ContractStatus.ENDED,
                ContractRequestType.NONE,
                CONTRACT_START_DATE.minusYears(1),
                CONTRACT_END_DATE.minusYears(1)
        );
        givenArtistOwnedContract(contract);

        ManagedShopContractActionResultResponse response =
                contractService.requestManagedShopContractRelease(USER_ID, CONTRACT_ID);

        assertThat(response.getContractStatus()).isEqualTo(ContractStatus.PENDING);
        assertThat(response.getContractRequestType()).isEqualTo(ContractRequestType.RELEASE);
    }

    @Test
    @DisplayName("작가가 해제신청을 취소한다")
    void givenReleaseRequestedContract_whenCancelManagedShopContractRelease_thenReturnEnded() {
        ShopArtistContract contract = contractWithArtistAndShop(
                CONTRACT_ID,
                ContractStatus.PENDING,
                ContractRequestType.RELEASE,
                CONTRACT_START_DATE.minusYears(1),
                CONTRACT_END_DATE.minusYears(1)
        );
        givenArtistOwnedContract(contract);

        ManagedShopContractActionResultResponse response =
                contractService.cancelManagedShopContractRelease(USER_ID, CONTRACT_ID);

        assertThat(response.getContractStatus()).isEqualTo(ContractStatus.ENDED);
        assertThat(response.getContractRequestType()).isEqualTo(ContractRequestType.NONE);
    }

    @Test
    @DisplayName("계약 완료 상태에서는 작가가 해제신청할 수 없다")
    void givenActiveApprovedContract_whenRequestManagedShopContractRelease_thenThrowException() {
        ShopArtistContract contract = contractWithArtistAndShop(CONTRACT_ID, ContractStatus.APPROVED);
        givenArtistOwnedContract(contract);

        assertThatThrownBy(() -> contractService.requestManagedShopContractRelease(USER_ID, CONTRACT_ID))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("해제신청이 가능한 계약이 아닙니다.");
    }

    @Test
    @DisplayName("입점관리 계약서 상세를 조회한다")
    void givenOwnedManagedContract_whenGetManagedArtistContract_thenReturnDetail() {
        Shop shop = shopWithId(SHOP_ID);
        ShopArtistContract contract = contractWithArtistAndShop(CONTRACT_ID, ContractStatus.APPROVED);

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
        given(contractRepository.findManagedArtistContractByIdAndShopId(
                CONTRACT_ID,
                SHOP_ID,
                List.of(ContractStatus.PENDING, ContractStatus.APPROVED, ContractStatus.ENDED)
        )).willReturn(Optional.of(contract));

        ManagedArtistContractDetailResponse response =
                contractService.getManagedArtistContract(USER_ID, CONTRACT_ID);

        assertThat(response.getContractId()).isEqualTo(CONTRACT_ID);
        assertThat(response.getShopId()).isEqualTo(SHOP_ID);
        assertThat(response.getShopName()).isEqualTo("Postman 테스트 소품샵");
        assertThat(response.getArtistId()).isEqualTo(ARTIST_ID);
        assertThat(response.getArtistName()).isEqualTo("Postman 도자기 작가");
        assertThat(response.getContractStartDate()).isEqualTo(CONTRACT_START_DATE);
        assertThat(response.getContractEndDate()).isEqualTo(CONTRACT_END_DATE);
        assertThat(response.getContractStatus()).isEqualTo(ContractStatus.APPROVED);
        assertThat(response.getContractRequestType()).isEqualTo(ContractRequestType.NONE);
    }

    @Test
    @DisplayName("입점관리 계약서 상세가 해당 소품샵 소유가 아니면 예외를 던진다")
    void givenNotOwnedManagedContract_whenGetManagedArtistContract_thenThrowException() {
        Shop shop = shopWithId(SHOP_ID);

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
        given(contractRepository.findManagedArtistContractByIdAndShopId(
                CONTRACT_ID,
                SHOP_ID,
                List.of(ContractStatus.PENDING, ContractStatus.APPROVED, ContractStatus.ENDED)
        )).willReturn(Optional.empty());

        assertThatThrownBy(() -> contractService.getManagedArtistContract(USER_ID, CONTRACT_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("계약서 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("작가 해제 신청 계약을 해지 처리한다")
    void givenReleaseRequestedContract_whenTerminateManagedArtistContract_thenChangeStatusToTerminated() {
        Shop shop = shopWithId(SHOP_ID);
        ShopArtistContract contract = contractWithArtistAndShop(
                CONTRACT_ID,
                ContractStatus.PENDING,
                ContractRequestType.RELEASE,
                CONTRACT_START_DATE,
                CONTRACT_END_DATE
        );

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
        given(contractRepository.findManagedArtistContractByIdAndShopId(
                CONTRACT_ID,
                SHOP_ID,
                List.of(ContractStatus.PENDING, ContractStatus.APPROVED, ContractStatus.ENDED)
        )).willReturn(Optional.of(contract));

        ContractTerminationResponse response =
                contractService.terminateManagedArtistContract(USER_ID, CONTRACT_ID);

        assertThat(response.getContractId()).isEqualTo(CONTRACT_ID);
        assertThat(response.getContractStatus()).isEqualTo(ContractStatus.TERMINATED);
        assertThat(contract.getContractStatus()).isEqualTo(ContractStatus.TERMINATED);
        assertThat(contract.getContractRequestType()).isEqualTo(ContractRequestType.NONE);
    }

    @Test
    @DisplayName("계약 종료일 14일 이후 계약을 해지 처리한다")
    void givenExpiredContractAfterGracePeriod_whenTerminateManagedArtistContract_thenChangeStatusToTerminated() {
        Shop shop = shopWithId(SHOP_ID);
        ShopArtistContract contract = contractWithArtistAndShop(
                CONTRACT_ID,
                ContractStatus.APPROVED,
                ContractRequestType.NONE,
                CONTRACT_START_DATE,
                LocalDate.now().minusDays(14)
        );

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
        given(contractRepository.findManagedArtistContractByIdAndShopId(
                CONTRACT_ID,
                SHOP_ID,
                List.of(ContractStatus.PENDING, ContractStatus.APPROVED, ContractStatus.ENDED)
        )).willReturn(Optional.of(contract));

        ContractTerminationResponse response =
                contractService.terminateManagedArtistContract(USER_ID, CONTRACT_ID);

        assertThat(response.getContractStatus()).isEqualTo(ContractStatus.TERMINATED);
        assertThat(contract.getContractStatus()).isEqualTo(ContractStatus.TERMINATED);
    }

    @Test
    @DisplayName("해지 조건이 아닌 계약은 해지 처리할 수 없다")
    void givenNotTerminableContract_whenTerminateManagedArtistContract_thenThrowException() {
        Shop shop = shopWithId(SHOP_ID);
        ShopArtistContract contract = contractWithArtistAndShop(
                CONTRACT_ID,
                ContractStatus.PENDING,
                ContractRequestType.EXTENSION,
                CONTRACT_START_DATE,
                CONTRACT_END_DATE
        );

        given(shopRepository.findByUser_Id(USER_ID)).willReturn(Optional.of(shop));
        given(contractRepository.findManagedArtistContractByIdAndShopId(
                CONTRACT_ID,
                SHOP_ID,
                List.of(ContractStatus.PENDING, ContractStatus.APPROVED, ContractStatus.ENDED)
        )).willReturn(Optional.of(contract));

        assertThatThrownBy(() -> contractService.terminateManagedArtistContract(USER_ID, CONTRACT_ID))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("해지 승인 또는 계약해지가 가능한 계약이 아닙니다.");
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

    private ArtistSuggestionRow artistSuggestionRow(Long artistId, Long userId, String artistName) {
        return new ArtistSuggestionRow() {
            @Override
            public Long getArtistId() {
                return artistId;
            }

            @Override
            public Long getUserId() {
                return userId;
            }

            @Override
            public String getArtistName() {
                return artistName;
            }

            @Override
            public String getArtistImageUrl() {
                return "https://image.test/suggested.png";
            }

            @Override
            public Specialty getSpecialty() {
                return Specialty.CERAMIC;
            }

            @Override
            public String getInstagramId() {
                return "suggested_artist";
            }
        };
    }

    private CompletedContractDocumentRow completedContractDocumentRow(Long contractId) {
        return new CompletedContractDocumentRow() {
            @Override
            public Long getContractId() {
                return contractId;
            }

            @Override
            public Long getArtistId() {
                return ARTIST_ID;
            }

            @Override
            public String getArtistName() {
                return "Postman 도자기 작가";
            }

            @Override
            public String getArtistBusinessNumber() {
                return "222-33-44444";
            }

            @Override
            public String getArtistContact() {
                return "01022223333";
            }

            @Override
            public LocalDate getContractDate() {
                return CONTRACT_DATE;
            }

            @Override
            public LocalDate getContractStartDate() {
                return CONTRACT_START_DATE;
            }

            @Override
            public LocalDate getContractEndDate() {
                return CONTRACT_END_DATE;
            }
        };
    }

    private InProgressContractDocumentRow inProgressContractDocumentRow(
            Long contractId,
            Long counterpartyId,
            String counterpartyName,
            ContractDocumentStatus documentStatus
    ) {
        return new InProgressContractDocumentRow() {
            @Override
            public Long getContractId() {
                return contractId;
            }

            @Override
            public Long getCounterpartyId() {
                return counterpartyId;
            }

            @Override
            public String getCounterpartyName() {
                return counterpartyName;
            }

            @Override
            public LocalDate getContractDate() {
                return CONTRACT_DATE;
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
            public ContractDocumentStatus getContractDocumentStatus() {
                return documentStatus;
            }
        };
    }

    private ArtistAccountSearchRow artistAccountSearchRow(Long artistId, Long userId, String artistName) {
        return new ArtistAccountSearchRow() {
            @Override
            public Long getArtistId() {
                return artistId;
            }

            @Override
            public Long getUserId() {
                return userId;
            }

            @Override
            public String getName() {
                return "Postman 검색 사용자";
            }

            @Override
            public String getArtistName() {
                return artistName;
            }

            @Override
            public String getArtistImageUrl() {
                return "https://image.test/searched.png";
            }

            @Override
            public Specialty getSpecialty() {
                return Specialty.CERAMIC;
            }

            @Override
            public String getInstagramId() {
                return "searched_artist";
            }

            @Override
            public String getEmail() {
                return "searched-artist@test.com";
            }
        };
    }

    private ManagedArtistContractRow managedArtistRow(
            Long contractId,
            ContractStatus contractStatus,
            ContractRequestType requestType,
            String artistName
    ) {
        return managedArtistRow(contractId, contractStatus, requestType, artistName, CONTRACT_END_DATE);
    }

    private ManagedArtistContractRow managedArtistRow(
            Long contractId,
            ContractStatus contractStatus,
            ContractRequestType requestType,
            String artistName,
            LocalDate contractEndDate
    ) {
        return new ManagedArtistContractRow() {
            @Override
            public Long getContractId() {
                return contractId;
            }

            @Override
            public Long getArtistId() {
                return ARTIST_ID + contractId;
            }

            @Override
            public String getArtistName() {
                return artistName;
            }

            @Override
            public LocalDate getContractStartDate() {
                return CONTRACT_START_DATE;
            }

            @Override
            public LocalDate getContractEndDate() {
                return contractEndDate;
            }

            @Override
            public ContractStatus getContractStatus() {
                return contractStatus;
            }

            @Override
            public ContractRequestType getContractRequestType() {
                return requestType;
            }

            @Override
            public ContractDocumentStatus getContractDocumentStatus() {
                return ContractDocumentStatus.ARTIST_SUBMITTED;
            }
        };
    }

    private ManagedShopContractRow managedShopRow(
            Long contractId,
            ContractStatus contractStatus,
            ContractRequestType requestType,
            String shopName
    ) {
        return managedShopRow(contractId, contractStatus, requestType, shopName, CONTRACT_END_DATE, null);
    }

    private ManagedShopContractRow managedShopRow(
            Long contractId,
            ContractStatus contractStatus,
            ContractRequestType requestType,
            String shopName,
            LocalDate contractEndDate,
            LocalDateTime terminatedAt
    ) {
        return new ManagedShopContractRow() {
            @Override
            public Long getContractId() {
                return contractId;
            }

            @Override
            public Long getShopId() {
                return SHOP_ID + contractId;
            }

            @Override
            public String getShopName() {
                return shopName;
            }

            @Override
            public LocalDate getContractStartDate() {
                return CONTRACT_START_DATE;
            }

            @Override
            public LocalDate getContractEndDate() {
                return contractEndDate;
            }

            @Override
            public ContractStatus getContractStatus() {
                return contractStatus;
            }

            @Override
            public ContractRequestType getContractRequestType() {
                return requestType;
            }

            @Override
            public LocalDateTime getTerminatedAt() {
                return terminatedAt;
            }
        };
    }

    private Artist artistWithId(Long artistId) {
        User artistUser = User.builder()
                .id(USER_ID)
                .role(Role.ARTIST)
                .userStatus(UserStatus.ACTIVE)
                .build();
        BusinessProfile businessProfile = BusinessProfile.builder()
                .businessName("Postman 도자기 작가")
                .build();
        return Artist.builder()
                .id(artistId)
                .user(artistUser)
                .businessProfile(businessProfile)
                .build();
    }

    private void givenArtistOwnedContract(ShopArtistContract contract) {
        given(artistRepository.findByUserId(USER_ID)).willReturn(Optional.of(artistWithId(ARTIST_ID)));
        given(contractRepository.findContractByIdAndArtistId(CONTRACT_ID, ARTIST_ID))
                .willReturn(Optional.of(contract));
    }

    private Shop shopWithId(Long shopId) {
        User user = User.builder()
                .id(USER_ID)
                .build();
        BusinessProfile businessProfile = BusinessProfile.builder()
                .businessName("Postman 테스트 소품샵")
                .build();
        return Shop.builder()
                .shopId(shopId)
                .user(user)
                .businessProfile(businessProfile)
                .build();
    }

    private Shop shopWithContractProfile(Long shopId) {
        User user = User.builder()
                .id(USER_ID)
                .phoneNumber("01099998888")
                .build();
        BusinessProfile businessProfile = BusinessProfile.builder()
                .businessName("템플릿 소품샵")
                .ownerName("대표자 A")
                .businessNumber("123-45-67890")
                .businessAddress("서울시 마포구")
                .businessPhoneNumber("01011112222")
                .build();
        return Shop.builder()
                .shopId(shopId)
                .user(user)
                .businessProfile(businessProfile)
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

    private ShopArtistContract contractWithArtistAndShop(Long contractId, ContractStatus contractStatus) {
        return contractWithArtistAndShop(
                contractId,
                contractStatus,
                ContractRequestType.NONE,
                CONTRACT_START_DATE,
                CONTRACT_END_DATE
        );
    }

    private ShopArtistContract contractWithArtistAndShop(
            Long contractId,
            ContractStatus contractStatus,
            ContractRequestType contractRequestType,
            LocalDate contractStartDate,
            LocalDate contractEndDate
    ) {
        User artistUser = User.builder()
                .id(ARTIST_ID)
                .name("Postman 작가 A")
                .build();
        BusinessProfile artistBusinessProfile = BusinessProfile.builder()
                .businessName("Postman 도자기 작가")
                .build();
        Artist artist = Artist.builder()
                .id(ARTIST_ID)
                .user(artistUser)
                .businessProfile(artistBusinessProfile)
                .build();

        return ShopArtistContract.builder()
                .shopArtistContractsId(contractId)
                .artist(artist)
                .shop(shopWithId(SHOP_ID))
                .contractStatus(contractStatus)
                .contractRequestType(contractRequestType)
                .contractStartDate(contractStartDate)
                .contractEndDate(contractEndDate)
                .commissionType(CommissionType.RATE)
                .commissionValue(new BigDecimal("20.0000"))
                .memo("계약서 테스트 메모")
                .build();
    }

    private ShopArtistContract shopSentContract() {
        ShopArtistContract contract = contractWithArtistAndShop(
                CONTRACT_ID,
                ContractStatus.PENDING,
                ContractRequestType.NONE,
                CONTRACT_START_DATE,
                CONTRACT_END_DATE
        );
        contract.fillShopContract(
                "Postman 테스트 소품샵",
                "Postman 대표",
                "111-22-33333",
                "서울시 성동구",
                "01012345678",
                CONTRACT_START_DATE,
                CONTRACT_END_DATE,
                new BigDecimal("20.0000"),
                15,
                10,
                CONTRACT_DATE,
                "Postman 테스트 소품샵",
                "Postman 대표"
        );
        return contract;
    }

    private ShopArtistContract artistSubmittedContract() {
        ShopArtistContract contract = shopSentContract();
        contract.fillArtistContract(
                "Postman 도자기 작가",
                null,
                "부산시 해운대구",
                "01022223333",
                "국민은행",
                "Postman 작가",
                "1234567890",
                "Postman 도자기 작가"
        );
        return contract;
    }

    private ShopArtistContract approvedDocumentContract() {
        ShopArtistContract contract = artistSubmittedContract();
        contract.approveDocument();
        return contract;
    }

    private AdjustContractInventoryRequest inventoryRequest() {
        AdjustContractInventoryRequest request = new AdjustContractInventoryRequest();
        setField(request, "movementType", ContractProductStockMovementType.INBOUND);
        setField(request, "quantity", 3);
        setField(request, "occurredAt", OCCURRED_AT);
        setField(request, "memo", "Postman 추가 입고");
        return request;
    }

    private SendContractRequest sendContractRequest() {
        SendContractRequest request = new SendContractRequest();
        setField(request, "shopBusinessName", "Postman 테스트 소품샵");
        setField(request, "shopOwnerName", "Postman 대표");
        setField(request, "shopBusinessNumber", "111-22-33333");
        setField(request, "shopAddress", "서울시 성동구");
        setField(request, "shopContact", "01012345678");
        setField(request, "contractStartDate", CONTRACT_START_DATE);
        setField(request, "contractEndDate", CONTRACT_END_DATE);
        setField(request, "commissionRate", new BigDecimal("20.0000"));
        setField(request, "settlementDay", 15);
        setField(request, "paymentDay", 10);
        setField(request, "contractDate", CONTRACT_DATE);
        setField(request, "shopSignatureBusinessName", "Postman 테스트 소품샵");
        setField(request, "shopSignatureOwnerName", "Postman 대표");
        return request;
    }

    private SubmitArtistContractRequest submitArtistContractRequest() {
        SubmitArtistContractRequest request = new SubmitArtistContractRequest();
        setField(request, "artistName", "Postman 도자기 작가");
        setField(request, "artistBusinessNumber", " ");
        setField(request, "artistAddress", "부산시 해운대구");
        setField(request, "artistContact", "01022223333");
        setField(request, "artistBankName", "국민은행");
        setField(request, "artistAccountHolder", "Postman 작가");
        setField(request, "artistAccountNumber", "1234567890");
        setField(request, "artistSignatureName", "Postman 도자기 작가");
        return request;
    }

    private DeleteCompletedContractDocumentsRequest deleteCompletedDocumentsRequest(Long... contractIds) {
        DeleteCompletedContractDocumentsRequest request = new DeleteCompletedContractDocumentsRequest();
        setField(request, "contractIds", List.of(contractIds));
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
