package com.example.orders.model;

public class OrderItem {
    private String itemId;
    private int qty;

    public OrderItem() {}

    public OrderItem(String itemId, int qty) {
        this.itemId = itemId;
        this.qty = qty;
    }

    public String getItemId() {
        return itemId;
    }

    public int getQty() {
        return qty;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }
}
