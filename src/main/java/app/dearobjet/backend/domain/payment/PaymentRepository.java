package app.dearobjet.backend.domain.payment;

import app.dearobjet.backend.domain.payment.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentKey(String paymentKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.paymentKey = :paymentKey")
    Optional<Payment> findByPaymentKeyForUpdate(@Param("paymentKey") String paymentKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.paymentId = :id")
    Optional<Payment> findByIdForUpdate(@Param("id") Long id);

    Page<Payment> findByOrder_User_IdAndCreatedAtBetween(
            Long userId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );
}