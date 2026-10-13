# Spring Data JPA

## 1. Derived Query Methods & Custom JPQL Queries

Spring Data JPA eliminates boilerplate repository implementations by generating queries from method names or `@Query` annotations.

```java
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Derived Query Method
    List<Transaction> findByUserAndType(User user, TransactionType type);

    // Custom JPQL Aggregate Query
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0.00)
        FROM Transaction t
        WHERE t.user = :user
          AND t.type = :type
          AND t.transactionDate BETWEEN :startDate AND :endDate
        """)
    BigDecimal sumByUserAndTypeAndDateRange(
        @Param("user") User user,
        @Param("type") TransactionType type,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
```

See [`TransactionRepository.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/repository/TransactionRepository.java).

---

## 2. Declarative Transactions (`@Transactional`)

* `@Transactional` decorates service methods to manage database transactions (`BEGIN`, `COMMIT`, `ROLLBACK`).
* `@Transactional(readOnly = true)` optimizes database queries by disabling Hibernate dirty checking for read-only operations.
