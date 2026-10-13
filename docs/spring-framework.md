# Spring Framework Core Concepts

## 1. Inversion of Control (IoC) & Dependency Injection (DI)
* **IoC (Inversion of Control)**: The Spring Container manages object lifecycles and instantiation instead of manual `new` operations.
* **DI (Dependency Injection)**: Objects receive their required dependencies via constructor parameter injection.

### Why Constructor Injection is Preferred Over Field Injection (`@Autowired` on fields):
1. **Immutability**: Allows fields to be declared `private final`.
2. **Ease of Unit Testing**: Enables instantiation in test classes without launching Spring Context.
3. **Circular Dependency Detection**: Fails fast at startup if circular dependencies exist.

---

## 2. Core Spring Stereotype Annotations

* `@Component`: Generic Spring-managed bean candidate.
  - Example: Mappers ([`TransactionMapper.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/mapper/TransactionMapper.java))
* `@Service`: Indicates business logic layer bean.
  - Example: [`TransactionService.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/service/TransactionService.java)
* `@Repository`: Indicates persistence layer bean with automatic exception translation.
  - Example: Custom repositories
* `@RestController`: Specialization of `@Controller` returning serialized JSON response bodies (`@ResponseBody`).
  - Example: [`TransactionController.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/controller/TransactionController.java)
* `@Configuration`: Defines Spring configuration classes providing `@Bean` definitions.
  - Example: [`SecurityConfig.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/config/SecurityConfig.java)
