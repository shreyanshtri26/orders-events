package com.example.orders.observers;

import com.example.orders.events.Event;
import com.example.orders.model.Order;
import com.example.orders.model.OrderStatus;
import org.springframework.stereotype.Component;

@Component
public class AlertObserver implements OrderObserver {

    @Override
    public void onEventProcessed(Event event, Order order) {
        // No-op or minimal: alerts only on critical state changes.
    }

    @Override
    public void onStatusChanged(String orderId, OrderStatus oldStatus, OrderStatus newStatus) {
        if (newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.SHIPPED) {
            System.out.printf("ALERT: Order %s changed to %s%n", orderId, newStatus);
        }
    }
}
