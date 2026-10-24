package com.jyoti.paypulse.events.model;

import java.math.BigDecimal;

public record PaymentProcessedEvent(
        Long orderId,
        String transactionId,
        BigDecimal amount,
        boolean success,
        String failureReason,
        String circuitBreakerStatus,
        String correlationId
) {}
