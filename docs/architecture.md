# Target Layered Architecture

## 1. Request/Response Execution Flow

```text
HTTP Request
     ↓
Controller Layer (`com.expensetracker.controller`)
     ↓ [Extracts JSON & Validates via @Valid]
Request DTO (`com.expensetracker.dto.request`)
     ↓ [Passes Request DTO]
Mapper (`com.expensetracker.mapper`)
     ↓ [Converts Request DTO → Entity]
Service Layer (`com.expensetracker.service`)
     ↓ [Applies Business Rules & Transactions]
Repository Layer (`com.expensetracker.repository`)
     ↓ [Spring Data JPA / Hibernate JPQL Query]
Database (PostgreSQL / H2)
     ↓ [Returns JPA Entity]
Service Layer (`com.expensetracker.service`)
     ↓ [Passes Entity]
Mapper (`com.expensetracker.mapper`)
     ↓ [Converts Entity → Response DTO]
Response DTO (`com.expensetracker.dto.response`)
     ↓ [Wrapped in ResponseEntity<T>]
Controller Layer (`com.expensetracker.controller`)
     ↓ [Serializes to JSON & returns HTTP Status]
HTTP Response
```

---

## 2. Authenticated Endpoints Request Flow

```text
HTTP Request with Header `Authorization: Bearer <token>`
     ↓
JwtAuthFilter (`com.expensetracker.security.JwtAuthFilter`)
     ↓ [Validates Signature & Expiration via JwtUtil]
SecurityContextHolder (`Set SecurityContext UserDetails`)
     ↓
Spring Security Authorization Check
     ↓
Controller (`com.expensetracker.controller`)
     ↓
SecurityUtils (`securityUtils.getCurrentUser()`)
```

---

## 3. Package Responsibilities

* `config`: Spring Configuration beans (Security, Data Initializer).
* `controller`: REST endpoints, request validation (`@Valid`), status code mapping.
* `dto/request`: Incoming request payloads with Bean Validation annotations (`@NotNull`, `@NotBlank`, `@DecimalMin`).
* `dto/response`: Outgoing response payloads (immutable data containers returned to clients).
* `mapper`: Structural conversion between Entity and DTO (stateless, pure mapping).
* `entity`: JPA persistence models annotated with `@Entity`, `@Table`, `@Id`, `@ManyToOne`.
* `repository`: Data access interfaces extending `JpaRepository` with custom derived queries and `@Query`.
* `service`: Core business rules, financial calculations, transactional boundaries (`@Transactional`).
* `security`: Authentication filter (`JwtAuthFilter`), JWT tokens (`JwtUtil`), `UserDetailsService`.
* `exception`: Global exception handler (`@RestControllerAdvice`), standard `ErrorResponse`.
* `enums`: Domain constants (`AccountType`, `TransactionType`, `Frequency`, `NotificationType`, `Role`).
* `util`: Cross-cutting utility helpers.
* `learning`: Educational comparative modules (Core Java, DSA, Plain JDBC, Servlets/JSP, Spring JDBC, MongoDB, AI).
