package app.dearobjet.backend.domain.artist.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateArtistProductMemoRequest {

    @NotNull(message = "상품 메모는 필수입니다.")
    @Size(max = 1000, message = "상품 메모는 1000자 이하여야 합니다.")
    private String memo;
}
