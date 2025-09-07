package com.example.orders.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderCancelledEvent extends Event {
    private String orderId;
    private String reason;

    public OrderCancelledEvent() {}

    public String getOrderId() {
        return orderId;
    }

    public String getReason() {
        return reason;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
