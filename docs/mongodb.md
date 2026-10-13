# NoSQL & MongoDB Integration

## 1. Relational (PostgreSQL) vs Document (MongoDB)

| Criteria | PostgreSQL (Relational) | MongoDB (Document NoSQL) |
| :--- | :--- | :--- |
| **Data Model** | Tables, Rows, Columns | JSON-like Documents (BSON) |
| **Schema** | Strict fixed schema | Dynamic flexible schema |
| **Transactions** | Full ACID transactions | Single-document atomicity (multi-doc transactions available) |
| **Use Case in App** | Core financial ledger (Transactions, Accounts, Budgets) | Dynamic notes, receipt attachments, OCR text logs |

---

## 2. Spring Data MongoDB Annotations

* `@Document(collection = "expense_notes")`: Identifies class as a MongoDB document.
* `@Id`: Marks field as primary key string (ObjectId).
* `MongoRepository<ExpenseNoteDocument, String>`: Interface providing document CRUD operations.
