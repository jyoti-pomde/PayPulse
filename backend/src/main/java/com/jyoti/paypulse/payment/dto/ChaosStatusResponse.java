package com.jyoti.paypulse.payment.dto;

public record ChaosStatusResponse(
        boolean simulatedGatewayDown,
        String circuitBreakerState,
        int failureRateThreshold,
        String message
) {}
