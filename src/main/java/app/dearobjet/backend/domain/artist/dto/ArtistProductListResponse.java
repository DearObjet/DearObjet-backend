package app.dearobjet.backend.domain.artist.dto;

import app.dearobjet.backend.domain.shop.entity.Product;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ArtistProductListResponse {

    private final List<ArtistProductItemResponse> items;
    private final int page;
    private final int totalPages;

    public static ArtistProductListResponse from(Page<Product> productPage, int page) {
        return new ArtistProductListResponse(
                productPage.getContent().stream()
                        .map(ArtistProductItemResponse::from)
                        .toList(),
                page,
                productPage.getTotalPages()
        );
    }
}
