package com.example.orders.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String orderId;
    private String customerId;
    private List<OrderItem> items = new ArrayList<>();
    private BigDecimal totalAmount;
    private OrderStatus status = OrderStatus.PENDING;
    private List<String> eventHistory = new ArrayList<>();

    public Order() {}

    public Order(String orderId, String customerId, List<OrderItem> items, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.customerId = customerId;
        if (items != null) {
            this.items = items;
        }
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PENDING;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<String> getEventHistory() {
        return eventHistory;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void appendHistory(String entry) {
        this.eventHistory.add(entry);
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setEventHistory(List<String> eventHistory) {
        this.eventHistory = eventHistory;
    }
}
