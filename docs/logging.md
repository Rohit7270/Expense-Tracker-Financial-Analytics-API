# Logging Framework (Log4j2 / SLF4J)

## 1. SLF4J Abstraction & Lombok `@Slf4j`

SLF4J (Simple Logging Facade for Java) is a logging abstraction facade. Spring Boot defaults to Logback/Log4j2 underlying implementations.

Lombok `@Slf4j` injects a static logger instance into class:
```java
@Slf4j
@Service
public class TransactionService {
    public void process() {
        log.info("Processing transaction creation...");
    }
}
```

---

## 2. Log Levels & Best Practices

```text
TRACE  -> Detailed diagnostic tracing (disabled in production)
DEBUG  -> Detailed debugging information
INFO   -> General operational milestones (application start, user registration)
WARN   -> Non-fatal anomalies (exceeded budget limit, invalid attempts)
ERROR  -> System exceptions, database connection failures
```

---

## 3. Sensitive Data Masking Rule

> [!WARNING]
> Never log sensitive personal or financial information:
> - Passwords
> - JWT Secret keys or full Bearer tokens
> - Credit Card numbers / Bank Account numbers
