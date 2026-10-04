# Security — Bank Transaction Anomaly Flagger

> This document describes the security measures implemented in this college prototype.  
> This is a localhost-only demonstration. It is NOT intended for production banking use.

---

## Authentication

### Session Token Design

This project uses **custom stateless-style session tokens** (not JWT):

1. A `SecureRandom` generates 32 random bytes → formatted as a 64-character hex string
2. The raw token is returned to the client (`Authorization: Bearer <token>`)
3. The token is stored in an in-memory `ConcurrentHashMap` on the server with a 2-hour expiry
4. Every request runs through `TokenAuthenticationFilter`, which:
   - Extracts the `Authorization` header
   - Calls `TokenService.validateSession()` to check existence and expiry
   - Populates `SecurityContextHolder` with `bankId` and `role`
5. On logout, the token is removed from the map immediately (instant invalidation)

**No JWT library is used.** This avoids common JWT pitfalls (algorithm confusion, weak secrets, etc.).

---

## Authorization

### Role-Based Access Control (RBAC)

Two roles exist: `CUSTOMER` and `REVIEWER`.

Role is stored in the `users` table and embedded in the session data.

Spring Security `SecurityConfig` permits only authenticated requests for all `/api/**` paths except:
- `/api/auth/login`
- `/api/auth/forgot-password/**`
- `/api/auth/reset-password`

Role enforcement is performed at:
- **Controller level** — `@PreAuthorize` / manual role check
- **Service level** — reviewer bypass vs. customer isolation
- **Repository level** — all customer queries filter by `account_id` (owned by the authenticated `bankId`)

### Object-Level Authorization

A `CUSTOMER` can **never** access another customer's data:
- Every repository query includes a `WHERE account_id = ?` clause where `account_id` is resolved from the authenticated `bankId`
- The backend never trusts a client-supplied account ID; it always resolves from the session

A `REVIEWER` can access all alerts and review cases system-wide but has no own account row.

---

## Password Hashing

All passwords are stored using **BCrypt**:

```
stored_hash = BCryptPasswordEncoder.encode(rawPassword)
// cost factor = Spring Security default (12 rounds)
```

Password verification:
```
BCryptPasswordEncoder.matches(inputPassword, storedHash)
```

**No plaintext passwords are ever stored in the database or logged.**

Demo credentials are passed through `passwordEncoder.encode()` in `DatabaseInitializer.java` — only the resulting hash is inserted into SQLite.

---

## Password Reset

Reset flow (15-minute, single-use tokens):

```
1. User submits Bank ID
2. Server generates 32-byte SecureRandom token (raw)
3. Server computes SHA-256(raw token) → tokenHash
4. tokenHash + expiry stored in password_reset_tokens table
5. Raw token returned in API response (demo only — normally emailed)
6. User submits raw token + new password
7. Server hashes submitted token → must match stored hash + not expired + used_at IS NULL
8. Password updated with BCrypt hash
9. Token marked as used (single-use guarantee)
10. All active sessions for that user invalidated
```

---

## Password Strength Validation (Server-Side)

Change-password and reset-password enforce:
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (`!@#$%^&*()_+-=[]{}|;':\",./<>?`)

---

## Login Attempt Protection

`LoginAttemptService` tracks failed login attempts per Bank ID:
- After repeated failures, the account is temporarily locked
- Lockout is in-memory (resets on server restart — acceptable for demo)

---

## Audit Logging

Two audit tables exist:

### `security_activities`
User-facing security event log (shown in Security Center):
- `ACCOUNT_PROVISIONED`
- `SECURITY_PIN_SET`
- `DEVICE_AUTHORIZED`
- `PASSWORD_CHANGED`
- `LOGIN_SUCCESS`
- `LOGOUT`

### `security_audit_logs`
Internal audit trail:
- All login attempts (success/failure)
- Password change events
- Reset token issuance and use
- Role changes

---

## Security Headers and CORS

CORS is restricted to `http://localhost:5173` and `http://127.0.0.1:5173` only:

```properties
app.cors.allowed-origins=http://localhost:5173,http://127.0.0.1:5173
```

Configured in `SecurityConfig.java` via Spring's `CorsConfigurationSource`.

---

## What Is NOT Implemented (Intentional Limitations)

| Feature | Status | Reason |
|---------|--------|--------|
| Email sending | Not implemented | College demo — tokens returned in API response |
| HTTPS/TLS | Not implemented | Localhost only |
| Rate limiting (IP-based) | Not implemented | Out of scope for demo |
| CSRF protection | Not applicable | Token-based API, no browser form sessions |
| Content Security Policy | Not implemented | Out of scope for demo |
| Persistent session storage | Not implemented | In-memory; sessions lost on restart |
| Real 2FA | Not implemented | PIN used as simplified second factor |
