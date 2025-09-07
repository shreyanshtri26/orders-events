package com.example.orders.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShippingScheduledEvent extends Event {
    private String orderId;
    private LocalDate shippingDate;

    public ShippingScheduledEvent() {}

    public String getOrderId() {
        return orderId;
    }

    public LocalDate getShippingDate() {
        return shippingDate;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setShippingDate(LocalDate shippingDate) {
        this.shippingDate = shippingDate;
    }
}
