package app.dearobjet.backend.domain.business.controller;

import app.dearobjet.backend.domain.business.dto.BusinessVerifyRequest;
import app.dearobjet.backend.domain.business.service.BusinessVerifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/business")
public class BusinessVerifyController {

    private final BusinessVerifyService businessVerifyService;

    @PostMapping("/validate")
    public ResponseEntity<Boolean> validate(
            @RequestBody BusinessVerifyRequest request
    ) {
        return ResponseEntity.ok(
                businessVerifyService.verify(request)
        );
    }
}
