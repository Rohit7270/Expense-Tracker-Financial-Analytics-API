# Data Structures & Algorithms (DSA) in Expense Tracker

This document details practical data structures and algorithms applied to expense tracking operations, complete with Big-O time and space complexity.

---

## 1. Problem-to-Algorithm Mapping Matrix

| Problem | Data Structure / Algorithm | Time Complexity | Space Complexity | Practical Use Case |
| :--- | :--- | :--- | :--- | :--- |
| **Monthly Accumulation** | Array / List Iteration | $O(N)$ | $O(1)$ | Summing monthly transaction amounts |
| **Category Breakdown** | `HashMap` Aggregation | $O(N)$ | $O(K)$ ($K$ = unique categories) | Grouping expenses by category |
| **Tag Deduplication** | `HashSet` Storage | $O(N)$ | $O(T)$ ($T$ = unique tags) | Extracting unique tags across transactions |
| **Transaction Ranking** | `Comparator` / `Comparable` Sort | $O(N \log N)$ | $O(N)$ | Sorting transactions by date/amount |
| **Transaction Lookup** | Binary Search (Sorted List) | $O(\log N)$ | $O(1)$ | Fast ID lookup in indexed in-memory data |
| **Audit History** | `ArrayDeque` (LIFO Stack) | $O(1)$ per op | $O(S)$ ($S$ = stack size) | Undo/Redo recent user actions |
| **Alert Queue** | `LinkedList` (FIFO Queue) | $O(1)$ per op | $O(Q)$ ($Q$ = pending queue) | Background notification batch delivery |

---

## 2. Production vs In-Memory Implementation Note

> [!IMPORTANT]
> **Production Database Queries**: In the primary Spring Boot REST API, database filtering, sorting, pagination, and aggregation are delegated to **PostgreSQL SQL Engine** via Spring Data JPA JPQL / SQL queries (which leverage database indexes for maximum efficiency).
> 
> **Service Layer Usage**: In-memory Java collections (`List`, `Set`, `Map`) are used inside services such as [`AnalyticsService.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/service/AnalyticsService.java) and [`TransactionService.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/service/TransactionService.java) to calculate totals, map DTOs, and deduplicate tags.
