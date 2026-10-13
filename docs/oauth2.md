# OAuth2 Architecture & Concepts

## 1. JWT Authentication vs OAuth2 Authorization

| Feature | JWT Authentication | OAuth2 Authorization Framework |
| :--- | :--- | :--- |
| **Primary Purpose** | Stateless user identity verification | Delegated access authorization |
| **Token Issuer** | Application's own auth service | Centralized Authorization Server (e.g. Keycloak, Auth0, Google) |
| **Architecture** | Simple monolith/microservice token | Multi-actor framework (Client, Resource Owner, Auth Server, Resource Server) |

---

## 2. OAuth2 Roles & Flow

```text
Resource Owner (User)
       ↓ [Delegates Authorization]
Client App (Frontend / Mobile)
       ↓ [Requests Access Token]
Authorization Server (OAuth2 Provider)
       ↓ [Issues Access Token (JWT format)]
Resource Server (Expense Tracker API)
       ↓ [Validates Token & Returns Resources]
Client App
```

---

## 3. Spring Security OAuth2 Resource Server Profile
In a microservices or enterprise architecture, Spring Security can be configured as an OAuth2 Resource Server:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

```java
// SecurityConfig OAuth2 Resource Server snippet
http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
```
