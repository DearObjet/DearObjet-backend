package app.dearobjet.backend.domain.artist.dto;

import app.dearobjet.backend.domain.shop.entity.Product;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateArtistProductStockResponse {

    private final List<ArtistProductItemResponse> items;

    public static UpdateArtistProductStockResponse from(List<Product> products) {
        return new UpdateArtistProductStockResponse(
                products.stream()
                        .map(ArtistProductItemResponse::from)
                        .toList()
        );
    }
}
