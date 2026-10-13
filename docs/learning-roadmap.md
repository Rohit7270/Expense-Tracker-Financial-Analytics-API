# Java & Spring Master Learning Roadmap

This document serves as your guided study roadmap through the **Expense Tracker API** project. Each entry connects key theoretical concepts directly to exact source files within this project.

---

## 1. Core Java
* **Concept**: Encapsulation, Entities, DTOs, Custom Exceptions, Enums, `BigDecimal`.
* **Read**:
  - Encapsulated Entities: [`User.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/entity/User.java), [`Transaction.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/entity/Transaction.java)
  - Financial Enums: [`TransactionType.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/enums/TransactionType.java), [`AccountType.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/enums/AccountType.java)
  - Custom Exception: [`ResourceNotFoundException.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/exception/ResourceNotFoundException.java)
* **Concept**: Collections (List, Set, Map), Stream API, Lambdas, Optional, Date/Time API.
* **Read**: [`AnalyticsService.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/service/AnalyticsService.java), [`TransactionService.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/service/TransactionService.java)
* **Why**: Demonstrates clean object-oriented domain modeling, Java 17 Streams, and `BigDecimal` financial precision in production services.

---

## 2. Data Structures & Algorithms (DSA)
* **Concept**: List iteration, Map grouping, Set tag deduplication, sorting via Comparators, Big-O complexity analysis.
* **Read**: [`AnalyticsService.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/service/AnalyticsService.java), [`TransactionService.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/service/TransactionService.java)
* **Guide**: [`docs/dsa.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/dsa.md)

---

## 3. Build Systems (Maven & Gradle)
* **Concept**: Dependency management, build lifecycle (`clean`, `compile`, `test`, `package`), plugins, profiles.
* **Read**: [`pom.xml`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/pom.xml)
* **Guide**: [`docs/maven.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/maven.md), [`docs/gradle.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/gradle.md)

---

## 4. DTO & Mapper Layer
* **Concept**: Separation of API contract from domain entity, Request/Response DTO pattern, Mapper components.
* **Read**: 
  - Request DTOs: [`TransactionRequest.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/dto/request/TransactionRequest.java)
  - Response DTOs: [`TransactionResponse.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/dto/response/TransactionResponse.java)
  - Mapper: [`TransactionMapper.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/mapper/TransactionMapper.java)
* **Guide**: [`docs/dto-and-mapper.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/dto-and-mapper.md)

---

## 5. Persistence: JDBC, Spring JDBC & Spring Data JPA
* **Concept**: Evolution from low-level JDBC to `JdbcTemplate` to Spring Data JPA repository abstractions.
* **Read**: [`TransactionRepository.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/repository/TransactionRepository.java)
* **Guides**: [`docs/jdbc.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/jdbc.md), [`docs/spring-jdbc.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/spring-jdbc.md), [`docs/spring-data-jpa.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/spring-data-jpa.md)

---

## 6. ORM & Hibernate
* **Concept**: `@Entity`, `@Id`, `@ManyToOne`, `@Enumerated`, Lazy Loading, JPQL, Custom `@Query`, Spring Data Repository interface derivation.
* **Read**:
  - Entity: [`Transaction.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/entity/Transaction.java)
  - Repository: [`TransactionRepository.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/repository/TransactionRepository.java)
* **Guide**: [`docs/hibernate.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/hibernate.md)

---

## 7. Spring Framework & Spring Boot
* **Concept**: Dependency Injection, Constructor Injection, IoC Container, `@Service`, `@RestController`, `@Transactional`, Application Startup.
* **Read**:
  - Application Entry: [`ExpenseTrackerApplication.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/ExpenseTrackerApplication.java)
  - Service Layer: [`TransactionService.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/service/TransactionService.java)
  - Controller Layer: [`TransactionController.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/controller/TransactionController.java)
* **Guides**: [`docs/spring-framework.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/spring-framework.md), [`docs/spring-boot.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/spring-boot.md)

---

## 8. Security & JWT
* **Concept**: Stateless Authentication, Password Hashing (`BCryptPasswordEncoder`), JWT Generation/Validation, Security Filter Chain, `SecurityContextHolder`.
* **Read**:
  - Security Config: [`SecurityConfig.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/config/SecurityConfig.java)
  - JWT Utility: [`JwtUtil.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/security/JwtUtil.java)
  - Filter: [`JwtAuthFilter.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/security/JwtAuthFilter.java)
* **Guides**: [`docs/spring-security.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/spring-security.md), [`docs/jwt.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/jwt.md)

---

## 9. Testing & Quality Assurance
* **Concept**: Unit testing with Mockito `@Mock`/`@InjectMocks`, Controller testing with `MockMvc` & `@AutoConfigureMockMvc`, Integration testing with `@SpringBootTest`.
* **Read**:
  - Controller Test: [`AuthControllerTest.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/test/java/com/expensetracker/controller/AuthControllerTest.java)
  - Service Unit Test: [`TransactionServiceTest.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/test/java/com/expensetracker/service/TransactionServiceTest.java)
* **Guide**: [`docs/testing.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/testing.md)

---

## 10. Containerization & Deployment
* **Concept**: Docker multi-container setup with PostgreSQL.
* **Read**: [`docker-compose.yml`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docker-compose.yml), [`Dockerfile`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/Dockerfile)
* **Guides**: [`docs/docker.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/docker.md), [`docs/cloud-deployment.md`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docs/cloud-deployment.md)
