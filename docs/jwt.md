# JSON Web Token (JWT) Implementation

## 1. Structure of a JWT Token
A JWT token consists of 3 Base64URL-encoded strings separated by dots (`.`):

```text
Header . Payload . Signature
```

1. **Header**: Specifies algorithm (`HS256`) and token type (`JWT`).
2. **Payload (Claims)**: Contains statement data:
   - `sub` (Subject / User Email)
   - `userId` (User ID)
   - `role` (User Role)
   - `iat` (Issued At)
   - `exp` (Expiration Time)
3. **Signature**: Cryptographic signature calculated using `HMAC-SHA256` secret key to guarantee token integrity and prevent tampering.

---

## 2. JWT Request Interception Lifecycle

```text
HTTP Request with `Authorization: Bearer <token>`
       ↓
JwtAuthFilter (`com.expensetracker.security.JwtAuthFilter`)
       ↓ [Extracts Token]
JwtUtil (`com.expensetracker.security.JwtUtil.validateToken`)
       ↓ [Validates Expiration & Signature]
CustomUserDetailsService (`loadUserByUsername`)
       ↓
Set `SecurityContextHolder.getContext().setAuthentication(...)`
       ↓
Controller Processing
```

---

## 3. Security Credentials Rule

> [!CAUTION]
> JWT secret keys must NEVER be hardcoded in production source code.
> The key is externalized via `application.properties`:
> ```properties
> jwt.secret=${JWT_SECRET:default_base64_secret_key_for_development}
> ```
