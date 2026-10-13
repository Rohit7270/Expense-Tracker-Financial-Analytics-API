# Core Java Concepts in Expense Tracker API

This document explains where Core Java concepts appear in the actual codebase.

---

## 1. Object-Oriented Programming (OOP)
* **Encapsulation**: Private entity fields with public getters/setters and domain validation methods.
  - See [`User.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/entity/User.java), [`Account.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/entity/Account.java)
* **Inheritance & Interfaces**: Custom exceptions extending `RuntimeException` / `Exception`; custom repositories extending `JpaRepository`.
  - See [`ResourceNotFoundException.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/exception/ResourceNotFoundException.java)

---

## 2. Keywords (`final`, `static`, `enums`)
* **`final`**: Constructor parameters, logger constants (`private static final`), immutable DTO fields.
* **`static`**: Factory helper methods in utilities; constant definitions in security classes.
* **`enums`**: Strongly typed domain enumerations:
  - [`TransactionType.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/enums/TransactionType.java) (`INCOME`, `EXPENSE`)
  - [`AccountType.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/enums/AccountType.java) (`CASH`, `BANK`, `CREDIT_CARD`, `SAVINGS`)
  - [`Frequency.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/enums/Frequency.java) (`DAILY`, `WEEKLY`, `MONTHLY`, `YEARLY`)

---

## 3. Financial Accuracy with `BigDecimal`
Float and double types cause binary floating-point rounding errors (e.g. `0.1 + 0.2 = 0.30000000000000004`).
Financial applications **MUST** use `java.math.BigDecimal` for monetary amounts.
* Example:
  ```java
  BigDecimal remaining = budget.getMonthlyLimit().subtract(spent);
  ```
* See [`TransactionService.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/service/TransactionService.java), [`BudgetService.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/service/BudgetService.java).

---

## 4. Java Collections, Streams & Functional Interfaces
* **List**: Collection of user transactions and budget lists.
* **Set**: Deduplicated transaction tags (`Set<Tag>`).
* **Map**: Category spending aggregation maps.
* **Stream API**: Used extensively for filtering, mapping entities to DTOs, computing sums, and grouping expenses by category.
  - See [`AnalyticsService.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/service/AnalyticsService.java)
