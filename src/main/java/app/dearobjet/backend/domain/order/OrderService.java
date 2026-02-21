package app.dearobjet.backend.domain.order;

import app.dearobjet.backend.domain.order.dto.*;
import app.dearobjet.backend.domain.order.entity.Order;
import app.dearobjet.backend.domain.order.entity.OrderItem;
import app.dearobjet.backend.domain.order.entity.OrderStatus;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository; // 추가

    @Transactional
    public CreateOrderResponse createOrder(Long userId, CreateOrderRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Order order = Order.builder()
                .user(user)
                .orderNumber(generateOrderNo())
                .status(OrderStatus.CREATED)
                .totalAmount(0L)
                .buyerName(req.getBuyer().getName())
                .buyerPhone(req.getBuyer().getPhone())
                .receiverName(req.getBuyer().getName())
                .receiverPhone(req.getBuyer().getPhone())
                .build();

        long total = 0L;

        if (req.getItems() != null) {
            for (CreateOrderRequest.ItemLine line : req.getItems()) {
                long unitPrice = (line.getUnitPrice() == null) ? 0L : line.getUnitPrice();
                long lineAmount = unitPrice * Math.max(line.getQuantity(), 0);

                total += lineAmount;

                OrderItem item = OrderItem.builder()
                        .itemId(line.getItemId())
                        .quantity(line.getQuantity())
                        .unitPrice(unitPrice)
                        .lineAmount(lineAmount)
                        .build();

                order.addItem(item);
            }
        }

        order.updateTotalAmount(total);
        Order saved = orderRepository.save(order);
        return new CreateOrderResponse(saved.getOrdersId(), saved.getOrderNumber(), saved.getStatus());
    }

    @Transactional(readOnly = true)
    public OrderListResponse getOrders(Long userId, OrderStatus status, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "ordersId")
        );

        Page<Order> orderPage = (status == null)
                ? orderRepository.findByUser(user, pageable)
                : orderRepository.findByUserAndStatus(user, status, pageable);

        List<OrderResponse> responses = new ArrayList<>();
        for (Order order : orderPage.getContent()) {
            responses.add(toResponse(order));
        }

        return new OrderListResponse(responses, page, orderPage.getTotalPages());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        validateOwner(order, userId);
        return toResponse(order);
    }

    @Transactional
    public void cancelOrder(Long userId, Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        validateOwner(order, userId);
        order.cancel(reason == null ? "USER_CANCEL" : reason);
    }

    @Transactional
    public void updateStatus(Long userId, Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        validateOwner(order, userId);
        order.updateStatus(status);
    }

    @Transactional
    public void updateShipping(Long userId, Long orderId, String carrier, String trackingNo) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        validateOwner(order, userId);
        order.updateShipping(carrier, trackingNo);
    }

    @Transactional
    public void confirmPurchase(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        validateOwner(order, userId);
        order.updateStatus(OrderStatus.COMPLETED);
    }

    @Transactional(readOnly = true)
    public OrderStatsResponse getOrderStats(Long userId, LocalDate from, LocalDate to) {

        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = LocalDateTime.of(to, LocalTime.of(23, 59, 59));

        // 1) 일별 매출
        List<Object[]> salesRows = orderRepository.findSalesDaily(userId, fromDt, toDt);
        List<OrderStatsResponse.SalesPoint> sales = new ArrayList<>();
        for (Object[] row : salesRows) {
            String date = String.valueOf(row[0]);
            long cnt = ((Number) row[1]).longValue();
            long amt = ((Number) row[2]).longValue();
            sales.add(new OrderStatsResponse.SalesPoint(date, cnt, amt));
        }

        // 2) 인기 상품 Top 10
        List<Object[]> popularRows = orderRepository.findPopularItemsTop10(userId, fromDt, toDt);
        List<OrderStatsResponse.PopularItemPoint> popularItems = new ArrayList<>();
        for (Object[] row : popularRows) {
            Long itemId = ((Number) row[0]).longValue();
            long qty = ((Number) row[1]).longValue();
            long salesAmt = ((Number) row[2]).longValue();
            popularItems.add(new OrderStatsResponse.PopularItemPoint(itemId, qty, salesAmt));
        }

        // 3) 시간대별 주문수
        List<Object[]> hourRows = orderRepository.findOrdersByHour(userId, fromDt, toDt);
        List<OrderStatsResponse.OrdersByHourPoint> ordersByHour = new ArrayList<>();
        for (Object[] row : hourRows) {
            int hour = ((Number) row[0]).intValue();
            long cnt = ((Number) row[1]).longValue();
            ordersByHour.add(new OrderStatsResponse.OrdersByHourPoint(hour, cnt));
        }

        return new OrderStatsResponse(sales, popularItems, ordersByHour);
    }

    private void validateOwner(Order order, Long userId) {
        // 토큰 유저가 이 주문의 주인인지
        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new OrderAccessDeniedException(order.getOrdersId());
        }
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = new ArrayList<>();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                itemResponses.add(new OrderItemResponse(
                        item.getItemId(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getLineAmount()
                ));
            }
        }

        return new OrderResponse(
                order.getOrdersId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getBuyerName(),
                order.getBuyerPhone(),
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getCarrier(),
                order.getTrackingNo(),
                order.getPaidAt(),
                order.getShippedAt(),
                order.getDeliveredAt(),
                order.getCompletedAt(),
                order.getCancelledAt(),
                order.getCancelReason(),
                itemResponses
        );
    }

    private String generateOrderNo() {
        // 날짜 + 랜덤 6자리
        String yyyymmdd = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        int rnd = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "ORD-" + yyyymmdd + "-" + rnd;
    }
}
