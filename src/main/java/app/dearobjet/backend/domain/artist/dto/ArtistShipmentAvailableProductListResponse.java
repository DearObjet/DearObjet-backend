package app.dearobjet.backend.domain.artist.dto;

import app.dearobjet.backend.domain.shop.entity.Product;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ArtistShipmentAvailableProductListResponse {

    private final Long shopId;
    private final Long contractId;
    private final List<ArtistShipmentAvailableProductItemResponse> items;
    private final int page;
    private final int totalPages;

    public static ArtistShipmentAvailableProductListResponse from(
            Long shopId,
            Long contractId,
            Page<Product> productPage,
            int page
    ) {
        return new ArtistShipmentAvailableProductListResponse(
                shopId,
                contractId,
                productPage.getContent().stream()
                        .map(ArtistShipmentAvailableProductItemResponse::from)
                        .toList(),
                page,
                productPage.getTotalPages()
        );
    }
}
