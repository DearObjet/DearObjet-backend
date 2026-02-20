package app.dearobjet.backend.domain.business.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NtsValidateResponse {

    private String status_code;
    private List<NtsValidateData> data;

    @Getter
    @NoArgsConstructor
    public static class NtsValidateData {
        private String b_no;
        private String valid;
        private String valid_msg;
    }
}
