package com.jyoti.paypulse.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        String transactionId,
        Long orderId,
        String idempotencyKey,
        BigDecimal amount,
        boolean success,
        String status,
        String failureReason,
        String circuitBreakerState,
        boolean idempotentReplay,
        LocalDateTime timestamp
) {}
