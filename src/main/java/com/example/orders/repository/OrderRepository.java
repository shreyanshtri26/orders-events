package com.example.orders.repository;

import com.example.orders.model.Order;

import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class OrderRepository {
    private final Map<String, Order> store = new ConcurrentHashMap<>();

    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(store.get(orderId));
    }

    public boolean existsById(String orderId) {
        return store.containsKey(orderId);
    }

    public Order save(Order order) {
        store.put(order.getOrderId(), order);
        return order;
    }

    public Collection<Order> findAll() {
        return store.values();
    }
}
