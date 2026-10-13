# Testing Strategy (JUnit 5 & Mockito)

## 1. Testing Concepts Summary

* **Unit Test**: Tests a single class/method in isolation with all external dependencies mocked. Fast and lightweight.
  - Example: [`TransactionServiceTest.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/test/java/com/expensetracker/service/TransactionServiceTest.java)
* **Controller Test (`@WebMvcTest` / `MockMvc`)**: Tests HTTP request mapping, status codes, request validation, and JSON serialization without launching full web server.
  - Example: [`AuthControllerTest.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/test/java/com/expensetracker/controller/AuthControllerTest.java)
* **Integration Test (`@SpringBootTest`)**: Boots full Spring ApplicationContext and tests end-to-end component interaction (Service + Repository + Database).
* **Mock / Mockito (`@Mock`, `@InjectMocks`, `@Spy`)**: Creates dummy implementations of dependencies to isolate the class under test.

---

## 2. Test Execution Command

```bash
mvn clean test
```
