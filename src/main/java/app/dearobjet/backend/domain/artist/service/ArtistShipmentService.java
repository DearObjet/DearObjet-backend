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

@Service
@RequiredArgsConstructor
public class ArtistShipmentService {

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
                        ContractProductListingStatus.ACTIVE
                )
        );
    }

    @Transactional(readOnly = true)
    public ArtistShipmentProductListResponse getShipmentProducts(Long userId, Long contractId) {
        Artist artist = getArtistByUserId(userId);
        ShopArtistContract contract = getApprovedContractByArtist(contractId, artist.getId());

        return ArtistShipmentProductListResponse.from(
                contract.getShopArtistContractsId(),
                contractProductRepository.findShipmentProductRowsByContractId(
                        contractId,
                        artist.getId(),
                        ContractStatus.APPROVED,
                        ContractProductListingStatus.ACTIVE,
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

    private ShopArtistContract getApprovedContractByArtist(Long contractId, Long artistId) {
        ShopArtistContract contract = contractRepository.findContractByIdAndArtistId(contractId, artistId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "계약서 정보를 찾을 수 없습니다."
                ));
        if (contract.getContractStatus() != ContractStatus.APPROVED) {
            throw new EntityNotFoundException(
                    ErrorCode.ENTITY_NOT_FOUND,
                    "계약서 정보를 찾을 수 없습니다."
            );
        }
        return contract;
    }
}
