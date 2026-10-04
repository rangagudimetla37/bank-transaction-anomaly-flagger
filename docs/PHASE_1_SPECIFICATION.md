# SECUREBANK: BANK TRANSACTION ANOMALY FLAGGER
## Phase 1: Foundation and Authentication System Specification

### 1. Overview
SecureBank is a localhost-only banking security web application designed to demonstrate secure identity verification, robust password hashing, and session authentication for a banking anomaly detection platform.

### 2. Architecture
```
BANK PROJECT/
├── frontend/             # React + Vite + TypeScript (Port 5173)
├── backend/              # Java 17 + Spring Boot 3.2.4 REST API (Port 8080)
├── database/             # SQLite Embedded DB (database/securebank.db)
├── python/               # Placeholder for Phase 2 Anomaly Detection
└── docs/                 # Architectural specifications
```

### 3. Demo Credentials
* **Bank ID:** `SB100001`
* **Default Demo Password:** `Demo@12345`
* **Account Role:** `CUSTOMER`
* **Demo Identity Verification PIN:** `778899`

> **Note on Security:** The database contains **ONLY** salted BCrypt password hashes. Plaintext passwords are never stored, logged, or sent to the client.

### 4. Database Schema (SQLite)
File: `database/securebank.db`
Table: `users`
- `id` INTEGER PRIMARY KEY AUTOINCREMENT
- `bank_id` TEXT UNIQUE NOT NULL
- `password_hash` TEXT NOT NULL (BCrypt hash)
- `role` TEXT NOT NULL DEFAULT 'CUSTOMER'
- `security_question` TEXT NOT NULL
- `security_answer_hash` TEXT NOT NULL (BCrypt hash)
- `created_at` TEXT DEFAULT CURRENT_TIMESTAMP
- `updated_at` TEXT DEFAULT CURRENT_TIMESTAMP
- `last_login` TEXT

### 5. API Endpoints
| Method | Endpoint | Description | Protected |
|---|---|---|---|
| `POST` | `/api/auth/login` | Authenticate Bank ID and Password | No |
| `POST` | `/api/auth/logout` | Terminate session token | Yes |
| `GET`  | `/api/auth/me` | Fetch authenticated user profile | Yes |
| `POST` | `/api/auth/forgot-password/verify` | Verify identity with Bank ID + Demo PIN | No |
| `POST` | `/api/auth/reset-password` | Set new password and replace hash | No |

### 6. Security Safeguards Implemented
1. **Password Hashing:** Salted BCrypt (strength 12) prevents rainbow table and brute-force attacks.
2. **Timing/Enumeration Defense:** Generic error message `Invalid Bank ID or password.` on failed login prevents account enumeration.
3. **Password Replacement Only:** Strictly no retrieval or exposure of old passwords.
4. **Session Token Expiry:** Cryptographic Bearer tokens expire automatically and are invalidated upon logout or password reset.
5. **No Password Storage in Browser:** Browser never stores passwords in `localStorage`.
6. **Password Policy Enforcement:** Min 8 chars, 1 uppercase, 1 lowercase, 1 numeric digit validated on both frontend and backend.
