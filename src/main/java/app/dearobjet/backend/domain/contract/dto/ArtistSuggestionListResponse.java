package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.dto.projection.ArtistSuggestionRow;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ArtistSuggestionListResponse {

    private final List<ArtistSuggestionItemResponse> items;

    public static ArtistSuggestionListResponse from(List<ArtistSuggestionRow> rows) {
        return new ArtistSuggestionListResponse(
                rows.stream()
                        .map(ArtistSuggestionItemResponse::from)
                        .toList()
        );
    }
}
