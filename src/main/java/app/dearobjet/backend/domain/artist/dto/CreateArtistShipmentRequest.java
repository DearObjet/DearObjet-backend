package app.dearobjet.backend.domain.artist.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CreateArtistShipmentRequest {

    @Valid
    @NotEmpty(message = "출고할 상품은 1개 이상이어야 합니다.")
    private List<Item> items;

    @Getter
    @NoArgsConstructor
    public static class Item {

        @NotNull(message = "상품 ID는 필수입니다.")
        private Long productId;

        @NotNull(message = "출고 수량은 필수입니다.")
        @Min(value = 1, message = "출고 수량은 1 이상이어야 합니다.")
        private Integer quantity;

        @NotNull(message = "상품 버전은 필수입니다.")
        private Long version;
    }
}
