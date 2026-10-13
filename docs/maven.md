# Maven Build Guide

## 1. Overview
Apache Maven is the primary authoritative build and dependency management system for the Expense Tracker API.

Configuration File: [`pom.xml`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/pom.xml)

---

## 2. Standard Maven Lifecycles & Commands

```bash
# Clean target directory
mvn clean

# Compile Java source files
mvn compile

# Execute unit and integration tests
mvn test

# Package application into executable JAR
mvn package

# Install package into local ~/.m2 repository
mvn install
```

---

## 3. Dependency Organization in `pom.xml`

```xml
<!-- Web Layer -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Persistence Layer -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Security Layer -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Database Drivers -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- JWT Tokens -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>${jjwt.version}</version>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```
