package app.dearobjet.backend.domain.payment;

import app.dearobjet.backend.domain.payment.dto.*;
import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.auth.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/payments")
    public ApiResponse<CreatePaymentResponse> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreatePaymentRequest req
    ) {
        return ApiResponse.of(paymentService.createPayment(userDetails.getUserId(), req));
    }

    @PostMapping("/payments/{paymentId}/confirm")
    public ApiResponse<Void> confirm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long paymentId,
            @RequestBody ConfirmPaymentRequest req
    ) {
        paymentService.confirmPayment(userDetails.getUserId(), paymentId, req);
        return ApiResponse.of(null);
    }

    @PostMapping("/payments/{paymentId}/cancel")
    public ApiResponse<Void> cancel(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long paymentId,
            @RequestBody CancelPaymentRequest req
    ) {
        paymentService.cancelPayment(userDetails.getUserId(), paymentId, req);
        return ApiResponse.of(null);
    }
}