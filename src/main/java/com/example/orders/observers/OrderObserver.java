package com.example.orders.observers;

import com.example.orders.events.Event;
import com.example.orders.model.Order;
import com.example.orders.model.OrderStatus;

public interface OrderObserver {
    void onEventProcessed(Event event, Order order);
    void onStatusChanged(String orderId, OrderStatus oldStatus, OrderStatus newStatus);
}
