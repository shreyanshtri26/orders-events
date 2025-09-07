package com.example.orders.processing;

import com.example.orders.events.OrderCancelledEvent;
import com.example.orders.events.OrderCreatedEvent;
import com.example.orders.events.PaymentReceivedEvent;
import com.example.orders.events.ShippingScheduledEvent;
import com.example.orders.events.Event;
import com.example.orders.model.Order;
import com.example.orders.model.OrderItem;
import com.example.orders.model.OrderStatus;
import com.example.orders.observers.OrderObserver;
import com.example.orders.repository.OrderRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EventProcessor {
    private static final Logger log = LoggerFactory.getLogger(EventProcessor.class);

    private final OrderRepository repository;
    private final List<OrderObserver> observers;

    public EventProcessor(OrderRepository repository, List<OrderObserver> observers) {
        this.repository = repository;
        this.observers = observers;
    }

    public void process(Event event) {
        if (event == null) {
            log.warn("Received null event, skipping");
            return;
        }
        String type = event.getEventType();
        try {
            switch (type) {
                case "OrderCreated" -> handle((OrderCreatedEvent) event);
                case "PaymentReceived" -> handle((PaymentReceivedEvent) event);
                case "ShippingScheduled" -> handle((ShippingScheduledEvent) event);
                case "OrderCancelled" -> handle((OrderCancelledEvent) event);
                default -> log.warn("Unknown event type: {}", type);
            }
        } catch (ClassCastException cce) {
            log.warn("Event payload did not match expected type for eventType={}: {}", type, cce.getMessage());
        } finally {
            // Notify observers that an event has been processed (if order exists)
            if (event != null) {
                repository.findById(extractOrderId(event)).ifPresent(order ->
                        observers.forEach(o -> o.onEventProcessed(event, order)));
            }
        }
    }

    private String extractOrderId(Event event) {
        if (event instanceof OrderCreatedEvent oce) return oce.getOrderId();
        if (event instanceof PaymentReceivedEvent pre) return pre.getOrderId();
        if (event instanceof ShippingScheduledEvent sse) return sse.getOrderId();
        if (event instanceof OrderCancelledEvent oce) return oce.getOrderId();
        return null;
    }

    private void handle(OrderCreatedEvent e) {
        if (repository.existsById(e.getOrderId())) {
            log.info("Order {} already exists, ignoring duplicate creation", e.getOrderId());
            return;
        }
        Order order = new Order(e.getOrderId(), e.getCustomerId(), e.getItems(), e.getTotalAmount());
        order.setStatus(OrderStatus.PENDING);
        order.appendHistory(historyLine(e, "Order created"));
        repository.save(order);
    }

    private void handle(PaymentReceivedEvent e) {
        repository.findById(e.getOrderId()).ifPresentOrElse(order -> {
            OrderStatus prev = order.getStatus();
            BigDecimal paid = e.getAmountPaid() == null ? BigDecimal.ZERO : e.getAmountPaid();
            BigDecimal total = order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount();

            if (paid.compareTo(total) >= 0) {
                order.setStatus(OrderStatus.PAID);
                order.appendHistory(historyLine(e, "Payment received in full: " + paid));
            } else if (paid.compareTo(BigDecimal.ZERO) > 0) {
                order.setStatus(OrderStatus.PARTIALLY_PAID);
                order.appendHistory(historyLine(e, "Partial payment received: " + paid));
            } else {
                order.appendHistory(historyLine(e, "Payment event with zero/invalid amount"));
            }

            repository.save(order);
            notifyIfStatusChanged(order.getOrderId(), prev, order.getStatus());
        }, () -> log.warn("Payment for unknown order {}, ignoring", e.getOrderId()));
    }

    private void handle(ShippingScheduledEvent e) {
        repository.findById(e.getOrderId()).ifPresentOrElse(order -> {
            OrderStatus prev = order.getStatus();
            order.setStatus(OrderStatus.SHIPPED);
            order.appendHistory(historyLine(e, "Shipping scheduled for " + e.getShippingDate()));
            repository.save(order);
            notifyIfStatusChanged(order.getOrderId(), prev, order.getStatus());
        }, () -> log.warn("Shipping scheduled for unknown order {}, ignoring", e.getOrderId()));
    }

    private void handle(OrderCancelledEvent e) {
        repository.findById(e.getOrderId()).ifPresentOrElse(order -> {
            OrderStatus prev = order.getStatus();
            order.setStatus(OrderStatus.CANCELLED);
            order.appendHistory(historyLine(e, "Order cancelled: " + e.getReason()));
            repository.save(order);
            notifyIfStatusChanged(order.getOrderId(), prev, order.getStatus());
        }, () -> log.warn("Cancellation for unknown order {}, ignoring", e.getOrderId()));
    }

    private void notifyIfStatusChanged(String orderId, OrderStatus prev, OrderStatus next) {
        if (prev != next) {
            for (OrderObserver o : observers) {
                o.onStatusChanged(orderId, prev, next);
            }
        }
    }

    private String historyLine(Event e, String note) {
        return e.getTimestamp() + " - " + e.getEventType() + " - " + note;
    }
}
