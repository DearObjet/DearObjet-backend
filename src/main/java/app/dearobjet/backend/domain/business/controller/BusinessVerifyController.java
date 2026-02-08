package app.dearobjet.backend.domain.business.controller;

import app.dearobjet.backend.domain.business.dto.BusinessVerificationResponse;
import app.dearobjet.backend.domain.business.dto.BusinessVerifyRequest;
import app.dearobjet.backend.domain.business.service.BusinessVerifyService;
import app.dearobjet.backend.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/business")
public class BusinessVerifyController {

    private final BusinessVerifyService businessVerifyService;

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<BusinessVerificationResponse>> validate(
            @RequestBody BusinessVerifyRequest request
    ) {
        boolean result = businessVerifyService.verify(request);

        BusinessVerificationResponse response = new BusinessVerificationResponse(result);

        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
