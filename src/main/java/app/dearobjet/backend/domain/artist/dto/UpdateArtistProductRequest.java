package app.dearobjet.backend.domain.artist.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateArtistProductRequest {

    @NotBlank(message = "상품명은 필수입니다.")
    private String productName;

    @NotNull(message = "판매가는 필수입니다.")
    @DecimalMin(value = "0.0", message = "판매가는 0 이상이어야 합니다.")
    private BigDecimal price;

    @NotNull(message = "총재고는 필수입니다.")
    @Min(value = 0, message = "총재고는 0 이상이어야 합니다.")
    private Integer stockQuantity;
}
