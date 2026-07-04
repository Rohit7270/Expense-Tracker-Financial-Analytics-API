# Expense Tracker & Financial Analytics API

An enterprise-grade, production-ready Spring Boot backend application for personal expense tracking, automated budget management, and financial analytics. Built with **Java 21, Spring Boot, Spring Security, JWT, JPA/Hibernate, PostgreSQL, and Maven**.

This project demonstrates clean architecture patterns, SOLID principles, comprehensive integration testing, and resume-ready backend engineering practices.

---

## Technical Features

* **JWT Authentication & Security**: Secure login/registration, stateless session management, role-based access control, password encoding with BCrypt, and secure password reset tokens.
* **JPA Auditing**: Automatic creation and update timestamp logging (`createdAt`, `updatedAt`) across all transactional entities.
* **Ledger Synchronization**: Whenever an expense or income is created, modified, or deleted, a consolidated `Transaction` record is updated automatically under a single database transaction.
* **Budget Module & Alerts**: Configure monthly spending limits globally or category-specifically. The system tracks category spending and triggers `BUDGET_WARNING` (at 90% utilization) and `BUDGET_EXCEEDED` (at 100%) alerts in the activity logs.
* **Financial Analytics Engine**: Fetch category-wise spending distribution (with percentage ratios), identify highest spending categories, track monthly aggregate income/expenses, and view savings.
* **User Dashboard Stats**: Retrieve aggregated monthly stats including total income, total expenses, all-time current balance, active budget limits, and a list of the 5 most recent transactions.
* **REST Documentation**: Clean OpenAPI/Swagger documentation configuration supporting Bearer token authorization in the browser.
* **Global Exception Handling**: Exception mapping translation into clean, structured JSON API responses with bean validation details.

---

## System Architecture

The application follows clean architecture separating layers:

```
[Client / Swagger UI] 
       │
       ▼
  [Controller] (REST Endpoints, JSON input validation)
       │
       ▼
   [Service]  (Transactional business logic & budget alerting rules)
       │
       ▼
 [Repository] (JPA interfaces, custom JPQL queries & group summaries)
       │
       ▼
   [Entity]   (9 Relational Models mapped via Hibernate)
```

---

## Database Design (9 Entities)

The PostgreSQL database is fully normalized with the following entities:

1. **User**: Credentials, profiles, default currency, reset tokens, and timestamps.
2. **Role**: Access levels (`ROLE_USER`, `ROLE_ADMIN`).
3. **Category**: Expense/income classification types (Global or User-Custom).
4. **PaymentMethod**: Payment mode labels (Global or User-Custom).
5. **Expense**: Tracked expenses referencing Category, PaymentMethod, and User.
6. **Income**: Tracked incomes referencing Category (optional) and User.
7. **Budget**: Target limits for a given month and year (Global or Category-Specific).
8. **Transaction**: Consolidated log of all financial movements (referencing Expense/Income IDs).
9. **ActivityLog**: Security and administrative auditing trail (e.g. logins, exceeded budgets).

---

## REST Endpoints Summary

### Authentication & Profile
- `POST /api/auth/register` - Register a new account
- `POST /api/auth/login` - Authenticate and return JWT token
- `POST /api/auth/forgot-password` - Request a password reset token
- `POST /api/auth/reset-password` - Reset password using the token
- `GET /api/users/profile` - Fetch current user profile
- `PUT /api/users/profile` - Update profile name and currency
- `PUT /api/users/change-password` - Change password

### Categories & Payment Methods
- `GET /api/categories` - List default and custom categories
- `POST /api/categories` - Create a custom category
- `GET /api/payment-methods` - List default and custom payment methods
- `POST /api/payment-methods` - Create a custom payment method

### Expenses & Income (Paginated, Sorted, Filtered)
- `GET /api/expenses` - Search and page expenses (filter by date, category, method, search text)
- `POST /api/expenses` - Log a new expense (triggers budget checks)
- `PUT /api/expenses/{id}` - Update expense details
- `DELETE /api/expenses/{id}` - Delete expense and sync ledger
- `GET /api/income` - Search and page incomes
- `POST /api/income` - Log a new income
- `PUT /api/income/{id}` - Update income details
- `DELETE /api/income/{id}` - Delete income and sync ledger

### Budgeting & Analytics
- `POST /api/budgets` - Set monthly/category budget
- `GET /api/budgets/status` - Check remaining budgets & alerts
- `GET /api/dashboard/stats` - Fetch aggregate totals and recent ledger list
- `GET /api/analytics/category-distribution` - Percentage breakdown of spending
- `GET /api/analytics/highest-spending` - Highest spending category hierarchy
- `GET /api/analytics/monthly-savings` - Net savings totals

---

## Setup and Running

### Prerequisites
- Java 21 (JDK)
- Apache Maven
- PostgreSQL (for `prod` profile)

### Run with Local Dev Profile (H2 In-Memory)
By default, the application runs on H2 database.
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
Access the Swagger API documentation in your browser at:
`http://localhost:8080/api/v1/swagger-ui.html`

### Run Tests
```bash
mvn clean test
```
