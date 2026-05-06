package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.dto.projection.ManagedArtistContractRow;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ManagedArtistContractListResponse {

    private final List<ManagedArtistContractItemResponse> items;

    public static ManagedArtistContractListResponse from(List<ManagedArtistContractRow> rows, LocalDate today) {
        return new ManagedArtistContractListResponse(
                rows.stream()
                        .map(row -> ManagedArtistContractItemResponse.from(row, today))
                        .toList()
        );
    }
}
