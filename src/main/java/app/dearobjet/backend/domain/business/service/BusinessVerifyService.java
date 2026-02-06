package app.dearobjet.backend.domain.business.service;

import app.dearobjet.backend.domain.business.NtsRestClient;
import app.dearobjet.backend.domain.business.dto.BusinessVerifyRequest;
import app.dearobjet.backend.domain.business.dto.NtsValidateRequest;
import app.dearobjet.backend.domain.business.dto.NtsValidateResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BusinessVerifyService {
    private final NtsRestClient ntsRestClient;

    public boolean verify(BusinessVerifyRequest req) {
        NtsValidateRequest.NtsBusiness business = new NtsValidateRequest.NtsBusiness(
                req.getBNo(),
                req.getStartDt(),
                req.getPNm(),
                "", // p_nm2: 일반 내국인은 무조건 빈 문자열
                nv(req.getBNm()),
                nv(req.getCorpNo()),
                nv(req.getBSector()),
                nv(req.getBType()),
                nv(req.getBAdr())
        );

        NtsValidateRequest ntsRequest = new NtsValidateRequest(List.of(business));
        NtsValidateResponse response = ntsRestClient.validate(ntsRequest);

        return response != null && !response.getData().isEmpty()
                && "01".equals(response.getData().get(0).getValid());
    }

    // 필수 아닌 항목은 null 대신 ""으로 전송
    private String nv(String v) {
        return (v == null) ? "" : v;
    }
}