# REST API Architecture & Best Practices

## 1. Controller Responsibilities
In a clean Spring REST API, controllers are **thin adapters**.

### Controller MUST:
- Map HTTP endpoints (`@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`).
- Validate incoming payloads via `@Valid`.
- Delegate execution to the Service layer.
- Return appropriate HTTP status codes wrapped in `ResponseEntity<T>`.

### Controller MUST NOT contain:
- Database queries or JPA logic.
- Business calculations or financial rules.
- Entity-to-DTO conversion loops.

---

## 2. HTTP Methods & Semantics

* `GET`: Idempotent read operations (e.g. `GET /api/transactions`). Status: `200 OK`.
* `POST`: Non-idempotent resource creation (e.g. `POST /api/transactions`). Status: `201 Created`.
* `PUT`: Complete resource replacement (e.g. `PUT /api/accounts/{id}`). Status: `200 OK`.
* `PATCH`: Partial resource update (e.g. `PATCH /api/notifications/{id}/read`). Status: `200 OK`.
* `DELETE`: Resource removal (e.g. `DELETE /api/transactions/{id}`). Status: `204 No Content`.

---

## 3. Global Exception Handling

Centralized exception handling via `@RestControllerAdvice`:
- Catches domain exceptions (`ResourceNotFoundException`, `DuplicateResourceException`, `InvalidRequestException`).
- Formats consistent error JSON (`ErrorResponse`).
- See [`GlobalExceptionHandler.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/exception/GlobalExceptionHandler.java).
