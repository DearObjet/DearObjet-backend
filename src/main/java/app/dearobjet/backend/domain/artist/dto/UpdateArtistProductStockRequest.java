package app.dearobjet.backend.domain.artist.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateArtistProductStockRequest {

    @NotEmpty(message = "재고를 변경할 상품은 1개 이상이어야 합니다.")
    private List<@Valid Item> items;

    @Getter
    @NoArgsConstructor
    public static class Item {

        @NotNull(message = "상품 ID는 필수입니다.")
        private Long productId;

        @NotNull(message = "재고 수량은 필수입니다.")
        @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
        private Integer stockQuantity;

        @NotNull(message = "상품 버전은 필수입니다.")
        private Long version;
    }
}
