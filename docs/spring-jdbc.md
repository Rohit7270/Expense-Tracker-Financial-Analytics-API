# Spring JDBC Concepts

## 1. What is Spring JDBC?
Spring JDBC simplifies database interactions over raw JDBC by managing connection opening/closing, resource cleanup, and translating SQLExceptions into Spring's data access exception hierarchy.

```text
Plain JDBC (Manual Connection, ResultSet, Exception boilerplate)
      ↓
Spring JDBC (`JdbcTemplate`, automatic resource cleanup & exception translation)
      ↓
Spring Data JPA (High-level ORM repository interfaces)
```

---

## 2. When to Use Spring JDBC vs Spring Data JPA

* Use **Spring Data JPA** for standard CRUD operations, complex entity object graphs, and transactional domain modeling.
* Use **Spring JDBC (`JdbcTemplate`)** for high-performance reporting queries, bulk batch updates, or complex analytical SQL joins where ORM overhead is unnecessary.
