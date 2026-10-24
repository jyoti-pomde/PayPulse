package com.jyoti.paypulse.events.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    @Autowired
    private ApplicationEventPublisher publisher;

    public void publish(Object event) {
        log.info(">>> [Event-Bus] Publishing event: {}", event.getClass().getSimpleName());
        publisher.publishEvent(event);
    }
}
