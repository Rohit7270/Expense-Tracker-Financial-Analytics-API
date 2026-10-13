# Current Architecture (Before Refactoring)

## Overview
The **Expense Tracker API** is a monolithic Spring Boot 3.3.x application providing secure financial management REST APIs. It supports multi-user transaction logging, account balance tracking, category management, monthly budget tracking, recurring transaction automation, financial analytics, and user notifications.

---

## 1. Domain Entities & Database Schema
The database schema consists of 8 core JPA entities backed by PostgreSQL:

* **User**: Core security principal (`id`, `email`, `password`, `fullName`, `role`, `createdAt`).
* **Account**: Financial accounts owned by users (`id`, `name`, `type`, `balance`, `currency`, `user`).
* **Category**: Expense/income classification (`id`, `name`, `type`, `isSystemStandard`, `user`).
* **Transaction**: Individual financial records (`id`, `amount`, `type`, `description`, `transactionDate`, `category`, `account`, `user`, `tags`).
* **Budget**: Monthly category spending limit (`id`, `monthlyLimit`, `month`, `year`, `category`, `user`).
* **RecurringTransaction**: Scheduled transaction templates (`id`, `amount`, `type`, `frequency`, `startDate`, `nextDueDate`, `account`, `category`, `user`).
* **Tag**: Labels attached to transactions (`id`, `name`, `user`).
* **Notification**: In-app user alerts (`id`, `message`, `type`, `isRead`, `createdAt`, `user`).

---

## 2. Core Architecture Layers

```text
HTTP Request
     ↓
Controller Layer (`com.expensetracker.controller`)
     ↓
DTOs (`com.expensetracker.dto`)
     ↓
Service Layer (`com.expensetracker.service`)
     ↓
Repository Layer (`com.expensetracker.repository`)
     ↓
Database (PostgreSQL / H2 for tests)
```

### Key Findings & Areas for Refactoring
1. **DTO Organization**: Both Request and Response DTOs were located flat inside `com.expensetracker.dto`.
   * *Refactor Target*: Separate into `com.expensetracker.dto.request` and `com.expensetracker.dto.response`.
2. **Entity-to-DTO Mapping**: Services manually mapped entities to response DTOs using internal `toResponse(...)` helper methods.
   * *Refactor Target*: Extract mapping responsibilities into dedicated `@Component` classes inside `com.expensetracker.mapper`.
3. **Core Java & DSA Demonstrations**: Lacking isolated educational modules for Core Java features (Streams, Collections, Optional, Generics) and Data Structures/Algorithms (Arrays, HashMaps, HashSets, Sorting, Searching).
   * *Refactor Target*: Add `com.expensetracker.learning.corejava` and `com.expensetracker.learning.dsa`.
4. **JDBC, Servlets/JSP, MongoDB, Spring AI**: Not present as explicit educational comparative modules.
   * *Refactor Target*: Add isolated modules inside `com.expensetracker.learning.*` without destabilizing the core Spring Boot application.

---

## 3. Security Architecture
* **Framework**: Spring Security 6 with stateless JWT authentication.
* **Authentication**: `AuthController` handles `/api/auth/register` and `/api/auth/login`.
* **JWT Filter**: `JwtAuthFilter` intercepts incoming HTTP requests, verifies the `Authorization: Bearer <token>` header, extracts claims via `JwtUtil`, and sets `SecurityContextHolder`.

---

## 4. Test Suite Strategy
* **Framework**: JUnit 5, Mockito, Spring Security Test, H2 in-memory database for testing.
* **Coverage**: Auth controller tests, service unit tests for transactions, budgets, analytics, and recurring transaction scheduling.
