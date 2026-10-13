# Expense Tracker API — Personal Finance Backend

An interview-ready, beginner-friendly **Personal Finance & Expense Tracker REST API** built with **Java 17, Spring Boot 3.3.x, PostgreSQL, Hibernate/JPA, Spring Security, JWT, Maven, and Docker**.

This project provides a clean, well-tested monolithic backend demonstrating idiomatic Spring Boot engineering patterns that can be confidently explained in software engineering interviews.

---

## What This Project Implements

* **Core Java (Java 17)**: Encapsulated domain entities, financial calculations using `BigDecimal`, Stream API aggregations, records/DTOs, custom exceptions, and type-safe enums.
* **Spring Boot 3.3.x**: Auto-configuration, dependency injection, layered architecture (Controller → Service → Repository), application lifecycle, and Actuator health endpoints.
* **Spring Data JPA & Hibernate**: Relational data modeling (`@Entity`, `@ManyToOne`, `@ManyToMany`), derived queries, custom JPQL queries, transaction boundaries (`@Transactional`), and optimistic locking via `@Version`.
* **Spring Security & Stateless JWT**: Token-based authentication filter (`JwtAuthFilter`), cryptographic signature verification (`JwtUtil`), `BCryptPasswordEncoder`, and custom 401 Unauthorized handling.
* **DTO & Mapper Pattern**: Clean separation of API request/response contracts from persistence entities with dedicated `@Component` mappers and Jakarta Bean Validation (`@Valid`, `@NotNull`, `@DecimalMin`, `@NotBlank`, `@Size`).
* **Automated Scheduling**: Nightly background execution of scheduled recurring transactions with automatic balance updates via `@Scheduled` and Spring Task Scheduling.
* **In-App Notifications & Budget Alerts**: Automated budget monitoring preventing overspending, with duplicate alert suppression per month/category.
* **Robust Error Handling**: Centralized exception handler (`@RestControllerAdvice`) translating application and validation exceptions into standardized error payloads.
* **Automated Testing Suite**: 56 unit, repository, and controller tests covering slice controller tests (`MockMvc`), service unit tests (`Mockito`), and repository integration tests (`@DataJpaTest` with H2).
* **Containerization**: Multi-stage `Dockerfile` (Java 17) and Docker Compose configuration for local and containerized development.

---

## Project Architecture

```text
HTTP Request (JSON + Bearer JWT)
     ↓
Controller Layer (`com.expensetracker.controller`)
     ↓ [Validates Request Payload via Jakarta Validation]
Request DTO (`com.expensetracker.dto.request`)
     ↓
Mapper Component (`com.expensetracker.mapper`)
     ↓ [Converts DTO -> Entity]
Service Layer (`com.expensetracker.service`)
     ↓ [Applies Business Logic & @Transactional Boundaries]
Repository Layer (`com.expensetracker.repository`)
     ↓ [Spring Data JPA / Hibernate JPQL]
PostgreSQL Database (or H2 in Tests)
     ↓ [Returns Entity]
Mapper Component (`com.expensetracker.mapper`)
     ↓ [Converts Entity -> Response DTO]
Response DTO (`com.expensetracker.dto.response`)
     ↓
HTTP Response (JSON)
```

---

## How to Run the Application

### Option A: Local Maven + Dockerized PostgreSQL (Recommended for Development)

1. **Start PostgreSQL**:
   ```bash
   docker compose up -d postgres
   ```
   Starts PostgreSQL on `localhost:5432` (database: `expense_tracker`, user: `postgres`, password: `postgres`).

2. **Run the Spring Boot Application**:
   ```bash
   mvn spring-boot:run
   ```
   The application starts on `http://localhost:8080`. Seeding data initializes 10 default standard categories automatically.

### Option B: Run Entire Stack via Docker Compose

```bash
docker compose up --build
```
Builds the Java 17 multi-stage Docker image and runs both PostgreSQL and the API container.

### Option C: Run Automated Tests

```bash
# Run tests
mvn clean test

# Run full build verification including packaging
mvn clean verify
```
Executes all 56 unit, repository, and slice controller tests using the in-memory H2 test database.

---

## API Endpoints Reference

All secured endpoints require the HTTP Header: `Authorization: Bearer <token>`

### 1. Authentication
* `POST /api/auth/register` — Register a new user account (returns JWT).
* `POST /api/auth/login` — Authenticate existing user (returns JWT).

### 2. Users
* `GET /api/users/me` — Retrieve authenticated user profile.
* `PUT /api/users/me` — Update user name.

### 3. Accounts
* `POST /api/accounts` — Create financial account (`BANK`, `CASH`, `CREDIT_CARD`, `WALLET`, `SAVINGS`). Defaults balance to 0.00 if omitted.
* `GET /api/accounts` — List all accounts owned by authenticated user.
* `GET /api/accounts/{id}` — Get single account details.
* `PUT /api/accounts/{id}` — Update account name, type, or balance (omitting balance preserves existing balance).
* `DELETE /api/accounts/{id}` — Delete account (safely rejected if referenced by transactions or recurring transaction templates).

### 4. Categories
* `POST /api/categories` — Create custom category (`EXPENSE` or `INCOME`, icon max 50 chars).
* `GET /api/categories` — List accessible categories (default system categories + user's custom categories).
* `GET /api/categories/{id}` — Get category by ID.
* `PUT /api/categories/{id}` — Update custom category.
* `DELETE /api/categories/{id}` — Delete custom category (safely rejected if referenced by transactions, budgets, or recurring templates).

### 5. Transactions
* `POST /api/transactions` — Record new income or expense (atomically adjusts account balance, deduplicates tags case-insensitively, and checks budget).
* `GET /api/transactions` — Filter transactions by type, categoryId, accountId, startDate, and endDate (validates date range).
* `GET /api/transactions/{id}` — Get transaction details.
* `PUT /api/transactions/{id}` — Update transaction (reverses previous account balance effect and applies new balance).
* `DELETE /api/transactions/{id}` — Delete transaction (reverses account balance effect).

### 6. Budgets
* `POST /api/budgets` — Create monthly spending limit for an expense category.
* `GET /api/budgets` — List user budgets with live spent amount, remaining amount, and % used.
* `GET /api/budgets/{id}` — Get budget details.
* `PUT /api/budgets/{id}` — Update budget monthly limit or period.
* `DELETE /api/budgets/{id}` — Delete budget.

### 7. Recurring Transactions
* `POST /api/recurring-transactions` — Create scheduled recurring transaction template (DAILY, WEEKLY, MONTHLY, YEARLY).
* `GET /api/recurring-transactions` — List user recurring transaction templates.
* `GET /api/recurring-transactions/{id}` — Get recurring transaction by ID.
* `PUT /api/recurring-transactions/{id}` — Update recurring transaction details (realigns next due date if start date changes).
* `PATCH /api/recurring-transactions/{id}/toggle-active` — Pause or resume scheduled execution.
* `DELETE /api/recurring-transactions/{id}` — Delete recurring transaction template.

### 8. Notifications
* `GET /api/notifications` — List notifications in reverse chronological order.
* `PATCH /api/notifications/{id}/read` — Mark notification as read.

### 9. Analytics & Reports
* `GET /api/analytics/dashboard` — Dashboard metrics: monthly income, expense, net savings, total accounts balance, active budget alerts, and 5 recent transactions.
* `GET /api/analytics/monthly-report?month=10&year=2026` — Monthly report with income, expense, net savings, and category spending breakdown.
* `GET /api/analytics/spending-by-category?startDate=2026-10-01&endDate=2026-10-31` — Category spending aggregation within date range.

---

## Documentation Index (`docs/`)

The `docs/` folder contains technical guides organized into two categories:

### A. Implemented Codebase Guides (Directly Reflects Application Code)
* [`docs/architecture.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/architecture.md) — Layered architecture, request flow, and transaction boundaries.
* [`docs/database.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/database.md) — Database ERD, JPA entity relationships, and constraints.
* [`docs/dto-and-mapper.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/dto-and-mapper.md) — DTO pattern, separation of concern, and mapping layer.
* [`docs/rest-api.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/rest-api.md) — REST semantics, status codes, and exception handling.
* [`docs/spring-security.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/spring-security.md) — Stateless security filter chain and authentication provider.
* [`docs/jwt.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/jwt.md) — JWT claims extraction, expiration checking, and token validation.
* [`docs/spring-data-jpa.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/spring-data-jpa.md) — Query derivation, custom JPQL queries, and `@Transactional`.
* [`docs/hibernate.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/hibernate.md) — JPA annotations, dirty checking, and optimistic locking.
* [`docs/testing.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/testing.md) — Mockito unit tests, `@DataJpaTest` repository tests, and `MockMvc`.
* [`docs/docker.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/docker.md) — Multi-stage Dockerfile and Docker Compose orchestration.
* [`docs/maven.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/maven.md) — Maven lifecycle, build phases, and dependency management.

### B. Educational & Architectural Reference Guides (Conceptual Interview Prep)
*These documents provide conceptual comparisons and study notes for interview discussions, rather than features in the active backend:*
* [`docs/gradle.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/gradle.md) — Maven vs. Gradle build system comparison.
* [`docs/jdbc.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/jdbc.md) — Low-level JDBC vs. Spring Data JPA comparison.
* [`docs/servlets-and-jsp.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/servlets-and-jsp.md) — Traditional Servlet/JSP architecture vs. modern REST APIs.
* [`docs/mongodb.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/mongodb.md) — Relational (PostgreSQL) vs. Document NoSQL (MongoDB) trade-offs.
* [`docs/microservices.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/microservices.md) — Monolith vs. Microservices decomposition roadmap.
* [`docs/oauth2.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/oauth2.md) — JWT vs. OAuth2 authorization framework patterns.
* [`docs/spring-ai.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/spring-ai.md) — Concept guide for integrating LLMs into finance backends.
