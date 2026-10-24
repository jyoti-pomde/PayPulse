package com.jyoti.paypulse.events.model;

import java.math.BigDecimal;

public record OrderPlacedEvent(
        Long orderId,
        String orderNumber,
        String customerEmail,
        BigDecimal amount,
        String idempotencyKey,
        String correlationId
) {}
