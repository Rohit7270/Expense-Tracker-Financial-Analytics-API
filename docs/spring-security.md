# Spring Security Architecture

## 1. Authentication vs Authorization

* **Authentication**: Verifying **who** the user is (validating email and password or JWT token signature).
* **Authorization**: Verifying **what** the user is permitted to do (checking roles such as `ROLE_USER` or `ROLE_ADMIN`).

---

## 2. Authentication Flow

```text
User Credentials
       ↓
POST /api/auth/login
       ↓
AuthenticationManager (`UsernamePasswordAuthenticationToken`)
       ↓
UserDetailsService (`CustomUserDetailsService.loadUserByUsername`)
       ↓
PasswordEncoder (`BCryptPasswordEncoder.matches`)
       ↓
Generate JWT Token
       ↓
Return AuthResponse JSON
```

---

## 3. Spring Security Configuration

Configuration is implemented in [`SecurityConfig.java`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/src/main/java/com/expensetracker/config/SecurityConfig.java).

Key Configuration Features:
- Stateless session management (`SessionCreationPolicy.STATELESS`).
- Disabling CSRF for stateless REST API.
- Public endpoint permits (`/api/auth/**`, `/actuator/**`).
- Authenticated protection for all other endpoints (`.anyRequest().authenticated()`).
- Adding `JwtAuthFilter` before `UsernamePasswordAuthenticationFilter`.
