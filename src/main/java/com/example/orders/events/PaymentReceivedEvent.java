package com.example.orders.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentReceivedEvent extends Event {
    private String orderId;
    private BigDecimal amountPaid;

    public PaymentReceivedEvent() {}

    public String getOrderId() {
        return orderId;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }
}
