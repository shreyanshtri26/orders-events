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
import java.nio.file.Paths;

@Component
public class EventIngestor {
    private static final Logger log = LoggerFactory.getLogger(EventIngestor.class);

    private final EventProcessor processor;
    private final ObjectMapper mapper;

    public EventIngestor(EventProcessor processor) {
        this.processor = processor;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void ingest(String filePath) {
        log.info("Ingesting events from {}", filePath);
        try (java.util.stream.Stream<String> lines = Files.lines(Paths.get(filePath))) {
            lines.filter(s -> s != null && !s.trim().isEmpty())
                 .forEach(this::parseAndProcessSafely);
        } catch (Exception ex) {
            log.error("Failed to read events file {}: {}", filePath, ex.getMessage(), ex);
        }
    }

    private void parseAndProcessSafely(String jsonLine) {
        try {
            Event event = parseEvent(jsonLine);
            if (event != null) {
                processor.process(event);
            }
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
        
        try {
            if ("OrderCreated".equals(eventType)) {
                return mapper.treeToValue(root, OrderCreatedEvent.class);
            } else if ("PaymentReceived".equals(eventType)) {
                return mapper.treeToValue(root, PaymentReceivedEvent.class);
            } else if ("ShippingScheduled".equals(eventType)) {
                return mapper.treeToValue(root, ShippingScheduledEvent.class);
            } else if ("OrderCancelled".equals(eventType)) {
                return mapper.treeToValue(root, OrderCancelledEvent.class);
            } else {
                throw new IllegalArgumentException("Unknown eventType: " + eventType);
            }
        } catch (Exception e) {
            log.error("Error parsing event: {}", e.getMessage());
            throw e;
        }
    }
}
