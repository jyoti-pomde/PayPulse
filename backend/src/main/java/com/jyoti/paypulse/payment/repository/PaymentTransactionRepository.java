package com.jyoti.paypulse.payment.repository;

import com.jyoti.paypulse.payment.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByIdempotencyKey(String idempotencyKey);
    List<PaymentTransaction> findByOrderId(Long orderId);
    List<PaymentTransaction> findAllByOrderByCreatedAtDesc();
}
