package app.dearobjet.backend.domain.order;

import app.dearobjet.backend.domain.order.entity.Order;
import app.dearobjet.backend.domain.order.entity.OrderStatus;
import app.dearobjet.backend.domain.user.entity.User;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUser(User user, Pageable pageable);
    Page<Order> findByUserAndStatus(User user, OrderStatus status, Pageable pageable);
}
