# Database ERD & Entity Relationships

## 1. Entity Relationship Diagram (ERD) Overview

```text
               ┌──────────────┐
               │     User     │
               └──────┬───────┘
                      │ 1
       ┌──────────────┼──────────────┬──────────────┬──────────────┐
       │ *            │ *            │ *            │ *            │ *
┌──────┴──────┐ ┌─────┴──────┐ ┌─────┴──────┐ ┌─────┴──────┐ ┌─────┴──────┐
│   Account   │ │  Category  │ │   Budget   │ │  Recurring │ │Notification│
└──────┬──────┘ └─────┬──────┘ └────────────┘ └────────────┘ └────────────┘
       │ 1            │ 1
       └──────┬───────┘
              │ *
       ┌──────┴──────┐
       │ Transaction │
       └──────┬──────┘
              │ *
              │ *
       ┌──────┴──────┐
       │     Tag     │
       └─────────────┘
```

---

## 2. Key Relationships

1. **User (1) — (Many) Account**: A user can own multiple financial accounts (Cash, Bank, Savings, Credit Card).
2. **User (1) — (Many) Category**: A user can create custom categories alongside system-standard categories (`user_id = NULL`).
3. **User (1) — (Many) Budget**: A user defines monthly spending limits per category.
4. **User (1) — (Many) Transaction**: All financial ledger records belong to a specific authenticated user.
5. **Account (1) — (Many) Transaction**: Each transaction is associated with a specific payment account.
6. **Category (1) — (Many) Transaction**: Each transaction belongs to one category.
7. **Transaction (Many) — (Many) Tag**: Transactions can have multiple tags (`@JoinTable(name = "transaction_tags")`).
