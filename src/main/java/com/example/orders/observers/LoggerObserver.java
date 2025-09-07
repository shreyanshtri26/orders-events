package com.example.orders.observers;

import com.example.orders.events.Event;
import com.example.orders.model.Order;
import com.example.orders.model.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggerObserver implements OrderObserver {
    private static final Logger log = LoggerFactory.getLogger(LoggerObserver.class);

    @Override
    public void onEventProcessed(Event event, Order order) {
        String orderId = order != null ? order.getOrderId() : "N/A";
        log.info("Event processed: type={}, id={}, orderId={}", 
                event.getEventType(), 
                event.getEventId(),
                orderId);
    }

    @Override
    public void onStatusChanged(String orderId, OrderStatus oldStatus, OrderStatus newStatus) {
        log.info("Order {} status changed: {} -> {}", orderId, oldStatus, newStatus);
    }
}
