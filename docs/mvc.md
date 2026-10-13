# Model-View-Controller (MVC) Pattern

## 1. Traditional MVC vs Spring REST Architecture

### Traditional Spring MVC (Server-Side HTML Rendering)
```text
Client (Browser)
       ↓
Controller (`@Controller`)
       ↓
Service / Repository
       ↓
Model (`ModelAndView`)
       ↓
View Template (JSP / Thymeleaf HTML rendering)
```

### Modern Spring REST Architecture (JSON-based)
```text
Client (React / Angular / Mobile)
       ↓ [HTTP JSON Request]
REST Controller (`@RestController`)
       ↓
Service / Repository
       ↓
Entity / DTO
       ↓
JSON Response Payload
```

---

## 2. Structural Layer Alignment in Expense Tracker

* **Model**: Entities (`com.expensetracker.entity.*`) & DTOs (`com.expensetracker.dto.*`).
* **Controller**: REST Controllers (`com.expensetracker.controller.*`).
* **View**: JSON Payload format over HTTP response stream.
