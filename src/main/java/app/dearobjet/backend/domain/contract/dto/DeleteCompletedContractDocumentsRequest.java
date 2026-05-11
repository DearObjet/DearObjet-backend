package app.dearobjet.backend.domain.contract.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class DeleteCompletedContractDocumentsRequest {

    @NotEmpty(message = "삭제할 계약서는 1개 이상 선택해야 합니다.")
    private List<@NotNull(message = "계약서 ID는 필수입니다.") Long> contractIds;
}
