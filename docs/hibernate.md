# ORM and Hibernate

## 1. Specification vs Implementation vs Abstraction

```text
Java Application
      ↓
JPA (Java Persistence API / Jakarta Persistence) → Standard Specification (Annotations & Interfaces)
      ↓
Hibernate ORM → Underlying Provider/Implementation (SQL Generation, Dirty Checking, L1/L2 Cache)
      ↓
Spring Data JPA → Spring High-Level Abstraction (Repository Interfaces & Query Generation)
      ↓
PostgreSQL Database
```

---

## 2. Key Annotations in Expense Tracker Entities

* `@Entity`: Marks a Java class as a JPA entity mapped to a database table.
* `@Table(name = "transactions")`: Maps entity to exact table name.
* `@Id` & `@GeneratedValue(strategy = GenerationType.IDENTITY)`: Primary key definition.
* `@ManyToOne(fetch = FetchType.LAZY)`: Defines many-to-one relationship (e.g. Transaction belongs to User).
* `@ManyToMany`: Many-to-many relationship with `@JoinTable` (e.g. Transaction to Tag).
* `@Enumerated(EnumType.STRING)`: Stores Enum values as human-readable Strings rather than ordinals.

See entity implementation: [`Transaction.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/entity/Transaction.java).
