# Plain JDBC Concepts

## 1. What is JDBC?
Java Database Connectivity (JDBC) is the core Java API (`java.sql`) that allows Java applications to execute SQL statements and interact with relational databases.

```text
Java Application
      ↓
JDBC API (java.sql)
      ↓
JDBC Driver (org.postgresql.Driver)
      ↓
PostgreSQL Database
```

---

## 2. Core JDBC Classes & Interfaces

* `Connection`: Represents a physical connection to the database.
* `PreparedStatement`: Precompiled SQL statement that prevents SQL Injection attacks by substituting parameters safely.
* `ResultSet`: A tabular stream of data returned by executing a SQL query.
* `SQLException`: Checked exception thrown during database access errors.

---

## 3. SQL Injection Prevention & Resource Closing

```java
// Try-with-resources automatically closes Connection, PreparedStatement, and ResultSet
try (Connection conn = DriverManager.getConnection(dbUrl, user, pass);
     PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM transactions WHERE user_id = ?")) {
    
    pstmt.setLong(1, userId); // Safe parametrization prevents SQL Injection
    
    try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
            BigDecimal amount = rs.getBigDecimal("amount");
        }
    }
}
```
