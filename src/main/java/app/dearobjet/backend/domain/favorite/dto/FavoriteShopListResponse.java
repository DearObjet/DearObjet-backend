package app.dearobjet.backend.domain.favorite.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FavoriteShopListResponse {

    private final List<FavoriteShopItemResponse> items;
    private final int page;
    private final int totalPages;

    public static FavoriteShopListResponse from(Page<FavoriteShopItemResponse> favoritePage, int page) {
        return new FavoriteShopListResponse(
                favoritePage.getContent(),
                page,
                favoritePage.getTotalPages()
        );
    }
}
