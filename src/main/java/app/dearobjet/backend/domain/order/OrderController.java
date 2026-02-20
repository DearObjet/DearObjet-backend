package app.dearobjet.backend.domain.order;

import app.dearobjet.backend.domain.order.dto.*;
import app.dearobjet.backend.domain.order.entity.OrderStatus;
import app.dearobjet.backend.global.api.ApiResponse;
import app.dearobjet.backend.global.auth.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    public ApiResponse<CreateOrderResponse> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateOrderRequest request
    ) {
        return ApiResponse.of(orderService.createOrder(userDetails.getUserId(), request));
    }

    @GetMapping("/orders")
    public ApiResponse<OrderListResponse> getOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.of(orderService.getOrders(userDetails.getUserId(), status, page, size));
    }

    @GetMapping("/orders/{orderId}")
    public ApiResponse<OrderResponse> getOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId
    ) {
        return ApiResponse.of(orderService.getOrder(userDetails.getUserId(), orderId));
    }

    @PostMapping("/orders/{orderId}/cancel")
    public ApiResponse<Void> cancelOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId,
            @RequestBody CancelOrderRequest request
    ) {
        orderService.cancelOrder(userDetails.getUserId(), orderId, request.getReason());
        return ApiResponse.of(null);
    }

    @PatchMapping("/orders/{orderId}/status")
    public ApiResponse<Void> updateStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId,
            @RequestBody UpdateOrderStatusRequest request
    ) {
        orderService.updateStatus(userDetails.getUserId(), orderId, request.getStatus());
        return ApiResponse.of(null);
    }

    @PutMapping("/orders/{orderId}/shipping")
    public ApiResponse<Void> updateShipping(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId,
            @RequestBody UpdateShippingRequest request
    ) {
        orderService.updateShipping(userDetails.getUserId(), orderId, request.getCarrier(), request.getTrackingNo());
        return ApiResponse.of(null);
    }

    @PostMapping("/orders/{orderId}/confirm")
    public ApiResponse<Void> confirmPurchase(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId
    ) {
        orderService.confirmPurchase(userDetails.getUserId(), orderId);
        return ApiResponse.of(null);
    }
}
