# Orders Events (Java + Spring Boot)

A lightweight, event-driven order processing console application that ingests newline-delimited JSON (NDJSON) events, updates in-memory order state, and notifies observers for important state changes.

![Java](https://img.shields.io/badge/Java-17+-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![Maven](https://img.shields.io/badge/Maven-3.8+-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Architecture](#-architecture)
- [Quick Start](#-quick-start)
- [Testing](#-testing)
- [Project Structure](#-project-structure)


## 🎯 Overview

This project simulates core backend behavior of an e-commerce system by processing order-related events: `OrderCreated`, `PaymentReceived`, `ShippingScheduled`, and `OrderCancelled`. It demonstrates event-driven architecture patterns using Spring Boot for dependency injection, Jackson for JSON parsing with JavaTime support, and maintains state using an in-memory repository.

## ✨ Features

- **Event Ingestion**: Process NDJSON files (one JSON per line)
- **State Management**: Track order transitions through lifecycle states
- **Observer Pattern**: Pluggable notification system for state changes
- **Console Application**: Lightweight CLI tool using CommandLineRunner
- **Type Safety**: Strongly typed event system with proper serialization
- **Thread Safe**: Concurrent processing support with thread-safe repositories

## 🏗 Architecture

The application follows a clean separation of concerns:

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   EventIngestor │───▶│  EventProcessor  │───▶│ OrderRepository │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │                        │
                                ▼                        ▼
                       ┌─────────────────┐    ┌─────────────────┐
                       │    Observers    │    │     Orders      │
                       └─────────────────┘    └─────────────────┘
```

### Flow
1. **NDJSON file** → EventIngestor reads and parses events
2. **EventIngestor** → EventProcessor handles business logic
3. **EventProcessor** → OrderRepository updates state
4. **Observers** → Receive notifications for events and status changes

## 🚀 Quick Start

### Prerequisites

- **Java 17+** - [Download here](https://adoptium.net/)
- **Maven 3.8+** - [Installation guide](https://maven.apache.org/install.html)

### Installation

```bash
# Clone the repository
git clone https://github.com/shreyanshtri26/orders-events
cd orders-events

# Build the application
mvn clean package

# Run with default sample data
java -jar target/orders-events-0.0.1-SNAPSHOT.jar

# Run with custom input file
java -jar target/orders-events-0.0.1-SNAPSHOT.jar path/to/your/events.jsonl
```

## 📊 Domain Model

### Order States
```
PENDING → PARTIALLY_PAID → PAID → SHIPPED
   ↓                                 ↑
CANCELLED ←──────────────────────────┘
```

### Event Types

| Event Type | Description | Triggers |
|------------|-------------|----------|
| `OrderCreated` | New order initialization | Status: PENDING |
| `PaymentReceived` | Payment processing | Status: PARTIALLY_PAID or PAID |
| `ShippingScheduled` | Order shipped | Status: SHIPPED |
| `OrderCancelled` | Order cancellation | Status: CANCELLED |

### Sample NDJSON Input

```json
{"eventId":"e1","timestamp":"2025-07-29T10:00:00Z","eventType":"OrderCreated","orderId":"ORD001","customerId":"CUST001","items":[{"itemId":"P001","qty":2},{"itemId":"P002","qty":1}],"totalAmount":100.00}
{"eventId":"e2","timestamp":"2025-07-29T10:01:00Z","eventType":"PaymentReceived","orderId":"ORD001","amountPaid":60.00}
{"eventId":"e3","timestamp":"2025-07-29T10:02:00Z","eventType":"PaymentReceived","orderId":"ORD001","amountPaid":40.00}
{"eventId":"e4","timestamp":"2025-07-29T10:03:00Z","eventType":"ShippingScheduled","orderId":"ORD001","shippingDate":"2025-07-30"}
{"eventId":"e5","timestamp":"2025-07-29T10:04:00Z","eventType":"OrderCreated","orderId":"ORD002","customerId":"CUST002","items":[{"itemId":"P009","qty":1}],"totalAmount":50.00}
{"eventId":"e6","timestamp":"2025-07-29T10:05:00Z","eventType":"OrderCancelled","orderId":"ORD002","reason":"Customer requested cancellation"}
```


## 🧪 Testing

```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=EventProcessorTest
```

### Test Strategy

- **Unit Tests**: Individual component testing without Spring context
- **Integration Tests**: End-to-end workflow validation
- **Temporary Files**: Generated NDJSON for isolated test scenarios

## 📁 Project Structure

```
orders-events/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/example/orders/
│   │   │   ├── Application.java
│   │   │   ├── model/
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   └── OrderStatus.java
│   │   │   ├── events/
│   │   │   │   ├── Event.java
│   │   │   │   ├── EventType.java
│   │   │   │   ├── OrderCreatedEvent.java
│   │   │   │   ├── PaymentReceivedEvent.java
│   │   │   │   ├── ShippingScheduledEvent.java
│   │   │   │   └── OrderCancelledEvent.java
│   │   │   ├── processing/
│   │   │   │   └── EventProcessor.java
│   │   │   ├── ingestion/
│   │   │   │   └── EventIngestor.java
│   │   │   ├── observers/
│   │   │   │   ├── OrderObserver.java
│   │   │   │   ├── LoggerObserver.java
│   │   │   │   └── AlertObserver.java
│   │   │   └── repository/
│   │   │       └── OrderRepository.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── events.jsonl
│   └── test/
│       └── java/com/example/orders/
│           └── OrdersEventsApplicationTests.java
```
