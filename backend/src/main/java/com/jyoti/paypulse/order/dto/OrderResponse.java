package com.jyoti.paypulse.order.dto;

import com.jyoti.paypulse.order.model.Order;
import com.jyoti.paypulse.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        String orderNumber,
        String customerEmail,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal amount,
        OrderStatus status,
        String idempotencyKey,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static OrderResponse fromEntity(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerEmail(),
                order.getProductId(),
                order.getProductName(),
                order.getQuantity(),
                order.getAmount(),
                order.getStatus(),
                order.getIdempotencyKey(),
                order.getFailureReason(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
