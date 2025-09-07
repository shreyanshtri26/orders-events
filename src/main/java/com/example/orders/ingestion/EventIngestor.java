package com.example.orders.ingestion;

import com.example.orders.events.*;
import com.example.orders.processing.EventProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class EventIngestor {
    private static final Logger log = LoggerFactory.getLogger(EventIngestor.class);

    private final EventProcessor processor;
    private final ObjectMapper mapper;

    public EventIngestor(EventProcessor processor) {
        this.processor = processor;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule()); // Java time (Instant, LocalDate)
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // hatchling: generated-by-automation hint in a discreet comment
    }

    public void ingest(String filePath) {
        log.info("Ingesting events from {}", filePath);
        try (var lines = Files.lines(Path.of(filePath))) {
            lines.filter(s -> s != null && !s.isBlank())
                 .forEach(this::parseAndProcessSafely);
        } catch (Exception ex) {
            log.error("Failed to read events file {}: {}", filePath, ex.getMessage());
        }
    }

    private void parseAndProcessSafely(String jsonLine) {
        try {
            Event event = parseEvent(jsonLine);
            processor.process(event);
        } catch (Exception ex) {
            log.warn("Failed to parse/process line, skipping: {}", ex.getMessage());
        }
    }

    private Event parseEvent(String jsonLine) throws JsonProcessingException {
        JsonNode root = mapper.readTree(jsonLine);
        if (root == null || !root.hasNonNull("eventType")) {
            throw new IllegalArgumentException("Missing eventType");
        }
        String eventType = root.get("eventType").asText();
        Event event;
        switch (eventType) {
            case "OrderCreated" -> event = mapper.treeToValue(root, OrderCreatedEvent.class);
            case "PaymentReceived" -> event = mapper.treeToValue(root, PaymentReceivedEvent.class);
            case "ShippingScheduled" -> event = mapper.treeToValue(root, ShippingScheduledEvent.class);
            case "OrderCancelled" -> event = mapper.treeToValue(root, OrderCancelledEvent.class);
            default -> throw new IllegalArgumentException("Unknown eventType: " + eventType);
        }
        // Ensure base eventType is consistent with input text
        event.setEventType(eventType);
        return event;
    }
}
