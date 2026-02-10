package app.dearobjet.backend.domain.business.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NtsValidateRequest {
    private List<NtsBusiness> businesses;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NtsBusiness {
        @JsonProperty("b_no")
        private String bNo;      // 사업자번호 (숫자 10자리)

        @JsonProperty("start_dt")
        private String startDt;  // 개업일자 (YYYYMMDD)

        @JsonProperty("p_nm")
        private String pNm;      // 대표자성명

        @JsonProperty("p_nm2")
        private String pNm2;     // 외국인일 때만 사용 (그 외 "" 필수)

        @JsonProperty("b_nm")
        private String bNm;      // 상호

        @JsonProperty("corp_no")
        private String corpNo;   // 법인번호

        @JsonProperty("b_sector")
        private String bSector;  // 업태

        @JsonProperty("b_type")
        private String bType;    // 종목

        @JsonProperty("b_adr")
        private String bAdr;     // 주소
    }
}