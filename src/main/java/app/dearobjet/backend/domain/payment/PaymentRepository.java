package app.dearobjet.backend.domain.payment;

import app.dearobjet.backend.domain.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentKey(String paymentKey);

    Page<Payment> findByOrder_User_IdAndCreatedAtBetween(
            Long userId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );
}