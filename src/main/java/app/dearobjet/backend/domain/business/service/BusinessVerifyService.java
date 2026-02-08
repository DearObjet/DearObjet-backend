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

        NtsValidateRequest.NtsBusiness business =
                new NtsValidateRequest.NtsBusiness(
                        req.getBusinessNumber(),                 // b_no
                        req.getOpeningDate().replace("-", ""),   // start_dt (yyyyMMdd)
                        req.getOwnerName(),                      // p_nm
                        "",                                      // p_nm2 (내국인)
                        "",                                      // b_nm
                        "",                                      // corp_no
                        "",                                      // b_sector
                        "",                                      // b_type
                        ""                                       // b_adr
                );

        NtsValidateRequest ntsRequest =
                new NtsValidateRequest(List.of(business));

        NtsValidateResponse response =
                ntsRestClient.validate(ntsRequest);

        return response != null
                && response.getData() != null
                && !response.getData().isEmpty()
                && "01".equals(response.getData().get(0).getValid());
    }
}