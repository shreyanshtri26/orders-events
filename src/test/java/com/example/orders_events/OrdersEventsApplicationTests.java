package com.example.orders;

// hatchling: test scaffolding generated for demonstration

import com.example.orders.events.Event;
import com.example.orders.ingestion.EventIngestor;
import com.example.orders.model.Order;
import com.example.orders.model.OrderStatus;
import com.example.orders.observers.OrderObserver;
import com.example.orders.processing.EventProcessor;
import com.example.orders.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrdersEventsApplicationTests {

    private OrderRepository repository;
    private CapturingObserver observer;
    private EventProcessor processor;
    private EventIngestor ingestor;
    private final List<OrderObserver> observers = new ArrayList<>();
    private final List<Path> toCleanup = new ArrayList<>();

    @BeforeEach
    void setUp() {
        repository = new OrderRepository();
        observer = new CapturingObserver();
        observers.clear();
        observers.add(observer);
        processor = new EventProcessor(repository, observers);
        ingestor = new EventIngestor(processor);
    }

    @AfterEach
    void tearDown() throws Exception {
        for (Path p : toCleanup) {
            try { Files.deleteIfExists(p); } catch (Exception ignore) {}
        }
        toCleanup.clear();
    }

    @Test
    void testOrderCreationPaymentAndShippingFlow() throws Exception {
        // Create a temp NDJSON file representing the happy path (created -> paid -> shipped)
        String ndjson = ""
                + "{\"eventId\":\"e1\",\"timestamp\":\"2025-07-29T10:00:00Z\",\"eventType\":\"OrderCreated\",\"orderId\":\"ORDT1\",\"customerId\":\"C1\",\"items\":[{\"itemId\":\"P1\",\"qty\":1}],\"totalAmount\":150.00}\n"
                + "{\"eventId\":\"e2\",\"timestamp\":\"2025-07-29T10:01:00Z\",\"eventType\":\"PaymentReceived\",\"orderId\":\"ORDT1\",\"amountPaid\":150.00}\n"
                + "{\"eventId\":\"e3\",\"timestamp\":\"2025-07-29T10:02:00Z\",\"eventType\":\"ShippingScheduled\",\"orderId\":\"ORDT1\",\"shippingDate\":\"2025-07-30\"}\n";

        Path tmp = Files.createTempFile("orders-events-", ".jsonl");
        toCleanup.add(tmp);
        Files.writeString(tmp, ndjson);

        // Ingest and process
        ingestor.ingest(tmp.toString());

        // Assertions on repository state
        Order order = repository.findById("ORDT1").orElseThrow();
        assertEquals(OrderStatus.SHIPPED, order.getStatus(), "Order should be shipped at end of flow");
        assertTrue(order.getEventHistory().size() >= 2, "History should contain at least payment + shipping entries");

        // Observer assertions: expect status changes PENDING -> PAID -> SHIPPED
        assertTrue(observer.statusChanges.stream().anyMatch(sc -> sc.orderId.equals("ORDT1")
                && sc.oldStatus == OrderStatus.PENDING && sc.newStatus == OrderStatus.PAID));
        assertTrue(observer.statusChanges.stream().anyMatch(sc -> sc.orderId.equals("ORDT1")
                && sc.oldStatus == OrderStatus.PAID && sc.newStatus == OrderStatus.SHIPPED));
        assertTrue(observer.processedEvents.stream().anyMatch(ev -> "OrderCreated".equals(ev.type)));
        assertTrue(observer.processedEvents.stream().anyMatch(ev -> "PaymentReceived".equals(ev.type)));
        assertTrue(observer.processedEvents.stream().anyMatch(ev -> "ShippingScheduled".equals(ev.type)));
    }

    @Test
    void testCancellationFlowAndAlerts() throws Exception {
        String ndjson = ""
                + "{\"eventId\":\"e10\",\"timestamp\":\"2025-07-29T11:00:00Z\",\"eventType\":\"OrderCreated\",\"orderId\":\"ORDT2\",\"customerId\":\"C2\",\"items\":[{\"itemId\":\"X\",\"qty\":3}],\"totalAmount\":90.00}\n"
                + "{\"eventId\":\"e11\",\"timestamp\":\"2025-07-29T11:05:00Z\",\"eventType\":\"OrderCancelled\",\"orderId\":\"ORDT2\",\"reason\":\"Customer request\"}\n";

        Path tmp = Files.createTempFile("orders-events-", ".jsonl");
        toCleanup.add(tmp);
        Files.writeString(tmp, ndjson);

        ingestor.ingest(tmp.toString());

        Order order = repository.findById("ORDT2").orElseThrow();
        assertEquals(OrderStatus.CANCELLED, order.getStatus(), "Order should be cancelled");

        // Expect one status change to CANCELLED and event processed callbacks
        assertTrue(observer.statusChanges.stream().anyMatch(sc -> sc.orderId.equals("ORDT2")
                && sc.newStatus == OrderStatus.CANCELLED));
        assertTrue(observer.processedEvents.stream().anyMatch(ev -> "OrderCancelled".equals(ev.type)));
    }

    @Test
    void testUnknownEventTypeIsIgnoredGracefully() throws Exception {
        String ndjson = ""
                + "{\"eventId\":\"e20\",\"timestamp\":\"2025-07-29T12:00:00Z\",\"eventType\":\"OrderCreated\",\"orderId\":\"ORDT3\",\"customerId\":\"C3\",\"items\":[{\"itemId\":\"Y\",\"qty\":1}],\"totalAmount\":10.00}\n"
                + "{\"eventId\":\"e21\",\"timestamp\":\"2025-07-29T12:01:00Z\",\"eventType\":\"UnknownType\",\"orderId\":\"ORDT3\"}\n";

        Path tmp = Files.createTempFile("orders-events-", ".jsonl");
        toCleanup.add(tmp);
        Files.writeString(tmp, ndjson);

        ingestor.ingest(tmp.toString());

        // Unknown line should be skipped; order remains from created event only
        Order order = repository.findById("ORDT3").orElseThrow();
        assertEquals(OrderStatus.PENDING, order.getStatus(), "Unknown event must not change state");
        // Ensure we saw OrderCreated processed, but not UnknownType (EventIngestor rejects before dispatch)
        assertTrue(observer.processedEvents.stream().anyMatch(ev -> "OrderCreated".equals(ev.type)));
        assertFalse(observer.processedEvents.stream().anyMatch(ev -> "UnknownType".equals(ev.type)));
    }

    // Helper capturing observer for assertions
    static class CapturingObserver implements OrderObserver {
        static class StatusChange {
            final String orderId;
            final OrderStatus oldStatus;
            final OrderStatus newStatus;

            StatusChange(String orderId, OrderStatus oldStatus, OrderStatus newStatus) {
                this.orderId = orderId;
                this.oldStatus = oldStatus;
                this.newStatus = newStatus;
            }
        }

        static class Processed {
            final String orderId;
            final String type;

            Processed(String orderId, String type) {
                this.orderId = orderId;
                this.type = type;
            }
        }

        final List<StatusChange> statusChanges = new ArrayList<>();
        final List<Processed> processedEvents = new ArrayList<>();

        @Override
        public void onEventProcessed(Event event, Order order) {
            String oid = order != null ? order.getOrderId() : null;
            processedEvents.add(new Processed(oid, event.getEventType()));
        }

        @Override
        public void onStatusChanged(String orderId, OrderStatus oldStatus, OrderStatus newStatus) {
            statusChanges.add(new StatusChange(orderId, oldStatus, newStatus));
        }
    }
}
