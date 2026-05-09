package app.dearobjet.backend.domain.artist.service;

import app.dearobjet.backend.domain.artist.dto.ArtistShipmentProductListResponse;
import app.dearobjet.backend.domain.artist.dto.ArtistShipmentShopListResponse;
import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.contract.ContractRepository;
import app.dearobjet.backend.domain.contract.entity.ShopArtistContract;
import app.dearobjet.backend.domain.contract.enums.ContractProductListingStatus;
import app.dearobjet.backend.domain.contract.enums.ContractProductStockMovementType;
import app.dearobjet.backend.domain.contract.enums.ContractStatus;
import app.dearobjet.backend.domain.contract.repository.ContractProductRepository;
import app.dearobjet.backend.domain.user.repository.ArtistRepository;
import app.dearobjet.backend.global.exception.EntityNotFoundException;
import app.dearobjet.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtistShipmentService {

    private static final List<ContractStatus> SHIPMENT_PRODUCT_CONTRACT_STATUSES = List.of(
            ContractStatus.APPROVED,
            ContractStatus.ENDED,
            ContractStatus.TERMINATED
    );

    private final ArtistRepository artistRepository;
    private final ContractRepository contractRepository;
    private final ContractProductRepository contractProductRepository;

    @Transactional(readOnly = true)
    public ArtistShipmentShopListResponse getShipmentShops(Long userId) {
        Artist artist = getArtistByUserId(userId);

        return ArtistShipmentShopListResponse.from(
                contractRepository.findShipmentShopRowsByArtistId(
                        artist.getId(),
                        ContractStatus.APPROVED,
                        ContractProductListingStatus.ACTIVE,
                        LocalDate.now()
                )
        );
    }

    @Transactional(readOnly = true)
    public ArtistShipmentProductListResponse getShipmentProducts(Long userId, Long shopId) {
        Artist artist = getArtistByUserId(userId);
        List<ShopArtistContract> contracts = getShipmentProductContracts(artist.getId(), shopId);
        List<Long> contractIds = contracts.stream()
                .map(ShopArtistContract::getShopArtistContractsId)
                .toList();

        return ArtistShipmentProductListResponse.from(
                shopId,
                contracts,
                contractProductRepository.findShipmentProductRowsByContractIds(
                        contractIds,
                        artist.getId(),
                        ContractProductStockMovementType.INBOUND
                )
        );
    }

    @Transactional(readOnly = true)
    public ArtistShipmentProductListResponse getRecentShipmentProducts(Long userId, Long shopId) {
        Artist artist = getArtistByUserId(userId);
        ShopArtistContract recentContract = getRecentShipmentProductContract(artist.getId(), shopId);
        List<ShopArtistContract> contracts = List.of(recentContract);

        return ArtistShipmentProductListResponse.from(
                shopId,
                contracts,
                contractProductRepository.findShipmentProductRowsByContractIds(
                        List.of(recentContract.getShopArtistContractsId()),
                        artist.getId(),
                        ContractProductStockMovementType.INBOUND
                )
        );
    }

    private Artist getArtistByUserId(Long userId) {
        return artistRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "작가 정보를 찾을 수 없습니다."
                ));
    }

    private List<ShopArtistContract> getShipmentProductContracts(Long artistId, Long shopId) {
        List<ShopArtistContract> contracts = contractRepository.findShipmentProductContractsByArtistIdAndShopId(
                        artistId,
                        shopId,
                        SHIPMENT_PRODUCT_CONTRACT_STATUSES
                );
        if (contracts.isEmpty()) {
            throw new EntityNotFoundException(
                    ErrorCode.ENTITY_NOT_FOUND,
                    "계약서 정보를 찾을 수 없습니다."
            );
        }
        return contracts;
    }

    private ShopArtistContract getRecentShipmentProductContract(Long artistId, Long shopId) {
        return getShipmentProductContracts(artistId, shopId).get(0);
    }
}
