package app.dearobjet.backend.domain.contract.dto;

import app.dearobjet.backend.domain.contract.dto.projection.ShopSuggestionRow;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ShopSuggestionListResponse {

    private final List<ShopSuggestionItemResponse> items;

    public static ShopSuggestionListResponse from(List<ShopSuggestionRow> rows) {
        return new ShopSuggestionListResponse(
                rows.stream()
                        .map(ShopSuggestionItemResponse::from)
                        .toList()
        );
    }
}
