# Message Broker

A production-style message broker built with Java 17 and Spring Boot 4, backed by PostgreSQL. Producers publish JSON payloads to topics. Consumers subscribe via consumer groups, fetch pending messages, and acknowledge or negative-acknowledge them. The broker guarantees at-least-once delivery with retries, a dead-letter queue, and automatic visibility timeouts for crashed consumers.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Message Lifecycle](#message-lifecycle)
- [Tech Stack](#tech-stack)
- [API Endpoints](#api-endpoints)
- [Getting Started](#getting-started)
- [Design Decisions](#design-decisions)
- [Testing](#testing)
- [Future Improvements](#future-improvements)

---

## Overview

Message brokers decouple producers from consumers. A producer writes a message; consumers process it independently. If a consumer crashes, the broker handles redelivery. If a message repeatedly fails, it's set aside for inspection instead of being lost.

This project implements those core primitives from scratch:

- **Topics** — logical channels for messages
- **Consumer groups** — named namespaces so multiple groups can each receive every message
- **At-least-once delivery** — messages are redelivered until acknowledged
- **Retry with limit** — failed messages retry up to `maxRetries` times
- **Dead-letter queue** — messages that exhaust retries move to `DLQ` for inspection
- **Visibility timeout** — unacknowledged deliveries are reclaimed after 60 seconds

---

## Architecture

```
                    ┌─────────────────────┐
                    │     Producers       │
                    └──────────┬──────────┘
                               │ POST /topics/{name}/messages
                               ▼
                    ┌─────────────────────┐
                    │       API           │
                    │   (Spring Boot)     │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │    PostgreSQL       │
                    │  topics             │
                    │  messages (JSONB)   │
                    │  consumer_groups    │
                    │  deliveries         │
                    └──────────┬──────────┘
                               │
            ┌──────────────────┼──────────────────┐
            │                                     │
            ▼                                     ▼
   ┌─────────────────┐                  ┌─────────────────┐
   │   Consumers     │                  │  Scheduled Job  │
   │ (pull + ack)    │                  │ (visibility     │
   │                 │                  │  timeout)       │
   └─────────────────┘                  └─────────────────┘
```

---

## Message Lifecycle

A message transitions through a defined set of states:

```
           ┌──────────┐
           │ PENDING  │◀──── publish
           └────┬─────┘
                │ consumer fetches
                ▼
           ┌──────────┐
           │IN_FLIGHT │
           └────┬─────┘
                │
     ┌──────────┼──────────┐
     │          │          │
     ▼          ▼          ▼
┌─────────┐ ┌────────┐ ┌─────────┐
│ ACKED   │ │ PENDING│ │  DLQ    │
│(success)│ │(retry) │ │(failed) │
└─────────┘ └────────┘ └─────────┘
              via nack     after max
              or timeout   retries
```

| Status | Meaning |
|:---|:---|
| `PENDING` | Waiting to be delivered |
| `IN_FLIGHT` | Delivered to a consumer, awaiting ack |
| `ACKED` | Successfully processed — terminal state |
| `DLQ` | Failed too many times — dead-letter queue |

---

## Tech Stack

| Layer | Technology |
|:---|:---|
| Language | Java 17 |
| Framework | Spring Boot 4.1 |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| Persistence | Spring Data JPA / Hibernate |
| Scheduling | Spring `@Scheduled` |
| Testing | JUnit 5, Testcontainers |
| Build | Maven |

---

## API Endpoints

### Topics

| Method | Path | Purpose |
|:---|:---|:---|
| `POST` | `/api/v1/topics` | Create a topic |
| `GET` | `/api/v1/topics` | List all topics |
| `GET` | `/api/v1/topics/{driver_id}` | Get a topic by ID |

### Messages

| Method | Path | Purpose |
|:---|:---|:---|
| `POST` | `/api/v1/topics/{name}/messages` | Publish a message |
| `GET` | `/api/v1/topics/{name}/messages?status=X` | List messages, optionally filtered by status |

### Consumer Groups

| Method | Path | Purpose |
|:---|:---|:---|
| `POST` | `/api/v1/topics/{name}/consumer-groups` | Register a consumer group |
| `GET` | `/api/v1/topics/{name}/consumer-groups` | List groups for a topic |
| `GET` | `/api/v1/topics/{name}/consumer-groups/{group}/messages?limit=10` | Fetch pending messages (marks IN_FLIGHT) |

### Acknowledgements

| Method | Path | Purpose |
|:---|:---|:---|
| `POST` | `/api/v1/deliveries/{token}/ack` | Ack a delivery — marks message ACKED |
| `POST` | `/api/v1/deliveries/{token}/nack` | Nack a delivery — returns to PENDING or routes to DLQ |

---

## Getting Started

### Prerequisites

- Docker Desktop
- Java 17+

### Start the Database

```bash
docker compose up -d
```

### Run the Application

```bash
./mvnw spring-boot:run
```

The API is available at `http://localhost:8080`.

### Example: Full Publish → Consume → Ack Cycle

**1. Create a topic:**

```bash
curl -X POST http://localhost:8080/api/v1/topics \
  -H "Content-Type: application/json" \
  -d '{"name": "orders"}'
```

**2. Publish a message:**

```bash
curl -X POST http://localhost:8080/api/v1/topics/orders/messages \
  -H "Content-Type: application/json" \
  -d '{"payload": {"orderId": "123", "amount": 49.99}}'
```

**3. Register a consumer group:**

```bash
curl -X POST http://localhost:8080/api/v1/topics/orders/consumer-groups \
  -H "Content-Type: application/json" \
  -d '{"name": "order-processor"}'
```

**4. Fetch the message:**

```bash
curl "http://localhost:8080/api/v1/topics/orders/consumer-groups/order-processor/messages?limit=10"
```

The response includes a `deliveryToken`.

**5. Ack it:**

```bash
curl -X POST http://localhost:8080/api/v1/deliveries/{deliveryToken}/ack
```

Or nack it to trigger a retry:

```bash
curl -X POST http://localhost:8080/api/v1/deliveries/{deliveryToken}/nack
```

---

## Design Decisions

**Why PostgreSQL and not a purpose-built queue:** The goal was to understand the internals of a broker, not to reuse an existing one. Every primitive — the message lifecycle, the delivery tracking, the DLQ — is implemented from scratch so the mechanics are transparent.

**Why `jsonb` for payloads:** The broker treats payloads as opaque. Producers decide what shape their messages have. `jsonb` stores arbitrary JSON efficiently and supports future querying if needed.

**Why delivery tokens:** A message can be delivered to multiple consumers in different groups. The token identifies a specific delivery, ensuring a consumer can only ack what was actually delivered to them. This is the same pattern SQS uses with receipt handles.

**Why a scheduled visibility timeout:** Consumers can crash mid-processing and never ack. Without a timeout, the message stays IN_FLIGHT forever. The `@Scheduled` job runs every 30 seconds and reclaims expired deliveries.

**Why `fixedDelay` instead of `fixedRate`:** `fixedDelay` waits for the previous run to complete before scheduling the next. On a DB-touching job, this prevents overlapping executions that could race.

---

## Testing

The integration test suite uses Testcontainers to spin up a real Postgres for every run.

```bash
./mvnw test
```

**Coverage:**

| Test class | What it covers |
|:---|:---|
| `TopicApiTest` | Create, duplicate rejection, validation, listing |
| `MessageApiTest` | Publish, 404 for missing topics, status filtering |
| `ConsumerFlowTest` | Full lifecycle — fetch, ack, nack, retry, DLQ |
| `MessageBrokerApplicationTests` | Application context loads |

**13 tests, all passing.**

---

## Future Improvements

- Authentication and authorization
- Message TTL (time-based expiration)
- Batch fetch and batch ack
- Partitioning per topic for throughput
- Metrics endpoint (Prometheus)
- Admin dashboard for DLQ inspection
- Configurable retry backoff strategy

---
