# Servlets and JSP Architecture

## 1. Traditional Servlet / JSP Flow

```text
Browser
   ↓ [HTTP Request]
Servlet (`HttpServlet`)
   ↓ [Parses Params & Calls Business Service]
Service Layer
   ↓ [Interacts with Data Access Objects (DAO)]
Database
   ↓ [Returns Data Model]
Servlet (`request.setAttribute("expenses", data)`)
   ↓ [RequestDispatcher.forward(request, response)]
JSP View (`/WEB-INF/views/expenses.jsp`)
   ↓ [Renders HTML on Server]
Browser (Receives Full HTML Page)
```

---

## 2. Why Modern Spring Boot Applications Use REST APIs Instead of JSP

1. **Decoupled Architecture**: REST APIs output lightweight JSON objects, allowing single-page web applications (React, Vue, Angular) and mobile apps (iOS, Android) to consume the same backend service.
2. **Performance & Scalability**: Server-side HTML rendering consumes server CPU/memory. REST APIs offload UI rendering to the client browser/device.
3. **Stateless Scalability**: REST APIs combined with JWT tokens allow stateless scaling across multiple container replicas without needing sticky sessions.
