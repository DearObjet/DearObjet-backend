package app.dearobjet.backend.domain.business.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessVerifyRequest {

    private String businessNumber;   // 사업자등록번호 (10자리)
    private String openingDate;      // yyyy-MM-dd
    private String ownerName;        // 대표자명
}
