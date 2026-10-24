package com.jyoti.paypulse.events.listener;

import com.jyoti.paypulse.audit.service.AuditService;
import com.jyoti.paypulse.events.model.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    @Autowired
    private AuditService auditService;

    @Async("eventTaskExecutor")
    @EventListener
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info(">>> [Async-Listener] OrderPlacedEvent received for Order #{}", event.orderNumber());
        auditService.record("EventBus", "ASYNC_ORDER_EVENT_RECEIVED",
                String.format("Async Event Bus dispatched OrderPlacedEvent for #%s (₹%s)", event.orderNumber(), event.amount()), "INFO");
    }
}
