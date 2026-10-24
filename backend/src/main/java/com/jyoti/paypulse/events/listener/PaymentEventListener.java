package com.jyoti.paypulse.events.listener;

import com.jyoti.paypulse.audit.service.AuditService;
import com.jyoti.paypulse.events.model.PaymentProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    @Autowired
    private AuditService auditService;

    @Async("eventTaskExecutor")
    @EventListener
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        log.info(">>> [Async-Listener] PaymentProcessedEvent: Order #{} status: {}", event.orderId(), event.success() ? "SUCCESS" : "FAILED");
        auditService.record("EventBus", "ASYNC_PAYMENT_EVENT_RECEIVED",
                String.format("Payment %s for Order #%d (Txn: %s, Circuit Breaker: %s)",
                        event.success() ? "SUCCESS" : "FAILED", event.orderId(), event.transactionId(), event.circuitBreakerStatus()),
                event.success() ? "INFO" : "WARN");
    }
}
