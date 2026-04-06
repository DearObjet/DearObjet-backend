package app.dearobjet.backend.domain.contract.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ContractApplicationCountResponse {
    private final long applicationCount;
    private final List<String> artistNames;
}
