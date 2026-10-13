# Microservices Architecture Roadmap

## 1. Monolith vs Modular Monolith vs Microservices

```text
Monolith (Current Expense Tracker API):
Single Spring Boot JAR deployment containing all domains (Auth, Account, Transaction, Budget, Notification).

Decomposed Microservices Target Architecture:

                    API Gateway (Spring Cloud Gateway)
                                   │
       ┌───────────────────────────┼───────────────────────────┐
       ▼                           ▼                           ▼
Auth Service               Transaction Service             Budget Service
(User & JWT Auth)          (Ledger & Accounts)             (Monthly Limits)
       │                           │                           │
  Auth Database               Tx Database                 Budget Database
  (PostgreSQL)                (PostgreSQL)                (PostgreSQL)
                                   │
                           Event Bus (Kafka/RabbitMQ)
                                   │
                           Notification Service
                           (Alert Worker)
                                   │
                           Notification DB
                           (MongoDB)
```

---

## 2. Key Microservice Design Patterns

1. **Database-per-Service**: Each microservice owns its private database. No direct cross-service DB queries allowed.
2. **API Gateway Pattern**: Single entry point for routing, rate limiting, and centralized JWT authentication.
3. **Event-Driven Asynchronous Messaging**: Services communicate asynchronously via events (e.g. `TransactionCreatedEvent` published to Kafka -> consumed by `NotificationService`).
4. **Eventual Consistency**: Replaces immediate ACID database transactions across services with saga orchestration / event-driven consistency.

---

## 3. Why Microservices Should NOT Be Introduced Prematurely

> [!IMPORTANT]
> Splitting a simple application into microservices introduces significant operational complexity (distributed logging, service discovery, network latency, distributed transactions, deployment overhead).
> 
> Monolithic / Modular Monolith architecture remains the recommended starting point until team size or independent scaling requirements justify microservices.
