package app.dearobjet.backend.domain.order;

import app.dearobjet.backend.domain.order.entity.Order;
import app.dearobjet.backend.domain.order.entity.OrderStatus;
import app.dearobjet.backend.domain.user.entity.User;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUser(User user, Pageable pageable);
    Page<Order> findByUserAndStatus(User user, OrderStatus status, Pageable pageable);

    // 1) 일별 매출
    @Query(value = """
    SELECT DATE(o.paid_at) AS d,
           COUNT(*) AS cnt,
           COALESCE(SUM(o.total_amount), 0) AS amt
    FROM orders o
    WHERE o.user_id = :userId
      AND o.paid_at IS NOT NULL
      AND o.paid_at BETWEEN :fromDt AND :toDt
      AND o.status IN ('PAID', 'SHIPPING', 'DELIVERED', 'COMPLETED')
    GROUP BY DATE(o.paid_at)
    ORDER BY d ASC
    """, nativeQuery = true)
    List<Object[]> findSalesDaily(
            @Param("userId") Long userId,
            @Param("fromDt") LocalDateTime fromDt,
            @Param("toDt") LocalDateTime toDt
    );

    // 2) 인기 상품 Top 10
    @Query(value = """
    SELECT oi.item_id AS itemId,
           COALESCE(SUM(oi.quantity), 0) AS qty,
           COALESCE(SUM(oi.line_amount), 0) AS sales
    FROM orders o
    JOIN order_item oi ON oi.orders_id = o.orders_id
    WHERE o.user_id = :userId
      AND o.paid_at IS NOT NULL
      AND o.paid_at BETWEEN :fromDt AND :toDt
      AND o.status IN ('PAID', 'SHIPPING', 'DELIVERED', 'COMPLETED')
    GROUP BY oi.item_id
    ORDER BY sales DESC
    LIMIT 10
    """, nativeQuery = true)
    List<Object[]> findPopularItemsTop10(
            @Param("userId") Long userId,
            @Param("fromDt") LocalDateTime fromDt,
            @Param("toDt") LocalDateTime toDt
    );

    // 3) 시간대별 주문수
    @Query(value = """
    SELECT CAST(EXTRACT(HOUR FROM o.paid_at) AS INT) AS h,
           COUNT(*) AS cnt
    FROM orders o
    WHERE o.user_id = :userId
      AND o.paid_at IS NOT NULL
      AND o.paid_at BETWEEN :fromDt AND :toDt
      AND o.status IN ('PAID', 'SHIPPING', 'DELIVERED', 'COMPLETED')
    GROUP BY CAST(EXTRACT(HOUR FROM o.paid_at) AS INT)
    ORDER BY h ASC
    """, nativeQuery = true)
    List<Object[]> findOrdersByHour(
            @Param("userId") Long userId,
            @Param("fromDt") LocalDateTime fromDt,
            @Param("toDt") LocalDateTime toDt
    );
}