package app.dearobjet.backend.domain.artist.dto;

import app.dearobjet.backend.domain.shop.entity.Product;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ArtistProductMemoResponse {

    private final Long productId;
    private final String memo;

    public static ArtistProductMemoResponse from(Product product) {
        return new ArtistProductMemoResponse(
                product.getProductsId(),
                product.getMemo() == null ? "" : product.getMemo()
        );
    }
}
