package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.dto.projection.InProgressContractDocumentRow;
import app.dearobjet.backend.domain.contract.enums.ContractDocumentStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InProgressContractDocumentItemResponse {

    private static final String CONTRACT_TITLE = "입점 계약서";
    private static final String SHOP_VIEWER_TYPE = "SHOP";
    private static final String ARTIST_VIEWER_TYPE = "ARTIST";
    private static final String SHOP_DETAIL_PATH_FORMAT = "/api/v1/contracts/artists/%d";
    private static final String ARTIST_DETAIL_PATH_FORMAT = "/api/v1/contracts/shops/%d";

    private final Long contractId;
    private final String title;
    private final String viewerType;
    private final Long counterpartyId;
    private final String counterpartyName;
    private final LocalDate contractDate;
    private final LocalDate contractStartDate;
    private final LocalDate contractEndDate;
    private final ContractDocumentStatus contractDocumentStatus;
    private final String detailApiPath;

    public static InProgressContractDocumentItemResponse forShop(InProgressContractDocumentRow row) {
        return from(row, SHOP_VIEWER_TYPE, SHOP_DETAIL_PATH_FORMAT.formatted(row.getContractId()));
    }

    public static InProgressContractDocumentItemResponse forArtist(InProgressContractDocumentRow row) {
        return from(row, ARTIST_VIEWER_TYPE, ARTIST_DETAIL_PATH_FORMAT.formatted(row.getContractId()));
    }

    private static InProgressContractDocumentItemResponse from(
            InProgressContractDocumentRow row,
            String viewerType,
            String detailApiPath
    ) {
        return new InProgressContractDocumentItemResponse(
                row.getContractId(),
                CONTRACT_TITLE,
                viewerType,
                row.getCounterpartyId(),
                valueOrEmpty(row.getCounterpartyName()),
                row.getContractDate(),
                row.getContractStartDate(),
                row.getContractEndDate(),
                row.getContractDocumentStatus(),
                detailApiPath
        );
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
