package app.dearobjet.backend.domain.contract.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateContractMemoRequest {

    @NotNull(message = "메모는 필수입니다.")
    private String memo;
}
