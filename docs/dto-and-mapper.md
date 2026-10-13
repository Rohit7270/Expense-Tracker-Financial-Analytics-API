# DTO (Data Transfer Object) and Mapper Pattern

## 1. What is a DTO and Why Do We Need It?
A **Data Transfer Object (DTO)** is an object that carries data between processes or layers without containing business logic.

### Why Entities Should NOT Be Returned Directly From Controllers:
1. **Security & Information Leakage**: Entities contain internal fields such as password hashes, internal DB version fields, or private metadata that must never be exposed over REST APIs.
2. **Circular Dependency & StackOverflowError**: Bi-directional JPA entity relationships (`@OneToMany` / `@ManyToOne`) cause infinite loops during Jackson JSON serialization.
3. **Decoupling Contracts**: Database schemas change over time. Returning DTOs ensures client-facing REST APIs remain stable even when database tables are refactored.
4. **Validation Separation**: Input validation rules (`@NotBlank`, `@DecimalMin`) belong on Request DTOs, not on database entities.

---

## 2. Request DTO vs Response DTO

* **Request DTO** (`com.expensetracker.dto.request`):
  - Captures user input sent via `POST` / `PUT` HTTP bodies.
  - Enforces field constraints (`@NotNull`, `@Size`, `@Email`).
  - Example: [`TransactionRequest.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/dto/request/TransactionRequest.java).

* **Response DTO** (`com.expensetracker.dto.response`):
  - Constructs sanitized read-only JSON returned to HTTP clients.
  - Formats data cleanly (e.g. converting entity relations into simple IDs/names).
  - Example: [`TransactionResponse.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/dto/response/TransactionResponse.java).

---

## 3. Why Mapping Belongs in a Mapper (Mapper vs Service)

### Mapper Responsibilities:
- Converts `Entity -> Response DTO` and `Request DTO -> Entity`.
- Contains **pure structural transformation logic** only.
- Contains NO business rules, NO database queries, and NO security checks.

### Service Responsibilities:
- Manages business operations (e.g., verifying user permissions, enforcing budget limits, updating account balances).
- Manages database transaction boundaries (`@Transactional`).

---

## 4. Example Mapper Code in Expense Tracker

```java
@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) return null;
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .accountId(transaction.getAccount() != null ? transaction.getAccount().getId() : null)
                .accountName(transaction.getAccount() != null ? transaction.getAccount().getName() : null)
                .build();
    }
}
```
