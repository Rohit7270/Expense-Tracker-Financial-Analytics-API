# Gradle Conceptual Guide

## 1. Overview
The project has been standardized on Maven (`pom.xml`) as its canonical, authoritative build system. This guide provides a conceptual overview comparing Maven and Gradle for interview preparation and architectural discussions.

Maven (`pom.xml`) is used for all builds, tests, container images, and CI/CD.

---

## 2. Maven vs Gradle Comparison

| Feature | Maven (`pom.xml`) | Gradle (`build.gradle`) |
| :--- | :--- | :--- |
| **Syntax** | XML (Declarative, strict schema) | Groovy / Kotlin DSL (Flexible, code-based) |
| **Performance** | Incremental builds via plugins | Build cache, incremental build daemon |
| **Configuration** | Rigid conventions | Highly customizable tasks |
| **Dependency Declarations** | `<dependency>` tags with explicit scopes | Concise `implementation`, `runtimeOnly`, `testImplementation` |

---

## 3. Useful Gradle Commands

```bash
# Compile Java source code
./gradlew compileJava

# Run test suite
./gradlew test

# Package application JAR
./gradlew bootJar
```
