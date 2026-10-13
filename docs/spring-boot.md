# Spring Boot Fundamentals

## 1. What Spring Boot Solves
Spring Boot simplifies Spring application development by providing:
1. **Opinionated Starter Dependencies** (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`).
2. **Auto-Configuration**: Automatically configures DataSource, EntityManager, Jackson serializers based on classpath dependencies.
3. **Embedded Web Server**: Embedded Tomcat/Jetty running directly inside Java JAR execution without external WAR deployment.

---

## 2. Application Entry Point

```java
@SpringBootApplication
public class ExpenseTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExpenseTrackerApplication.class, args);
    }
}
```

`@SpringBootApplication` is a meta-annotation combining:
- `@SpringBootConfiguration`
- `@EnableAutoConfiguration`
- `@ComponentScan("com.expensetracker")`

See [`ExpenseTrackerApplication.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/ExpenseTrackerApplication.java).

---

## 3. Configuration Properties & Profiles

Properties are configured in [`application.properties`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/resources/application.properties).
Active profile switching (e.g. `test` profile using H2 database during test execution):
```properties
spring.profiles.active=test
```
See test configuration in [`AuthControllerTest.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/test/java/com/expensetracker/controller/AuthControllerTest.java#L26).
