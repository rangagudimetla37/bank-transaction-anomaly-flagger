# SECUREBANK: BANK TRANSACTION ANOMALY FLAGGER
## Phase 2: Authenticated Customer Banking Experience Specification

### 1. Overview
Phase 2 builds the complete authenticated customer banking portal for SecureBank, providing customer-isolated account management, real-time transaction history with filtering/search, deep transaction details, and an interactive security center.

### 2. Routes Implemented
| Route | Page Component | Access | Description |
|---|---|---|---|
| `/login` | `LoginPage.tsx` | Public | Phase 1 authentication portal |
| `/forgot-password` | `ForgotPasswordPage.tsx` | Public | Phase 1 two-step identity verification & password reset |
| `/dashboard` | `DashboardPage.tsx` | Protected | Customer overview, dynamic metrics, and recent 5 transactions |
| `/transactions` | `TransactionsPage.tsx` | Protected | Full transaction history with search, type/flow/status filters |
| `/transactions/:transactionId` | `TransactionDetailsPage.tsx` | Protected | Standalone transaction record inspection |
| `/account` | `AccountPage.tsx` | Protected | Customer profile, masked account number, and security settings |
| `/security` | `SecurityPage.tsx` | Protected | Security parameters, authentication status, audit logs, and recommendations |

### 3. Database Schema Extensions (SQLite)
File: `database/securebank.db`

#### Table: `accounts`
- `id` INTEGER PRIMARY KEY AUTOINCREMENT
- `account_number` TEXT UNIQUE NOT NULL
- `bank_id` TEXT NOT NULL (FOREIGN KEY to `users.bank_id`)
- `customer_name` TEXT NOT NULL
- `account_type` TEXT NOT NULL DEFAULT 'SAVINGS'
- `balance` REAL NOT NULL DEFAULT 0.0
- `created_at` TEXT DEFAULT CURRENT_TIMESTAMP

#### Table: `transactions`
- `id` INTEGER PRIMARY KEY AUTOINCREMENT
- `transaction_id` TEXT UNIQUE NOT NULL (e.g. `TXN-20261004-0025`)
- `account_id` INTEGER NOT NULL (FOREIGN KEY to `accounts.id`)
- `transaction_type` TEXT NOT NULL (`GROCERY`, `SALARY`, `ATM`, `UPI`, `BILL`, `FOOD`, etc.)
- `description` TEXT NOT NULL
- `amount` REAL NOT NULL
- `transaction_direction` TEXT NOT NULL (`CREDIT` or `DEBIT`)
- `recipient` TEXT NOT NULL
- `location` TEXT NOT NULL (`Hyderabad`, `Bengaluru`, `Mumbai`, `Chennai`, `Vijayawada`, `Visakhapatnam`)
- `timestamp` TEXT NOT NULL
- `status` TEXT NOT NULL (`COMPLETED`, `PENDING`, `FAILED`)
- `created_at` TEXT DEFAULT CURRENT_TIMESTAMP

#### Table: `security_activities`
- `id` INTEGER PRIMARY KEY AUTOINCREMENT
- `bank_id` TEXT NOT NULL
- `activity_type` TEXT NOT NULL (`LOGIN_SUCCESS`, `PASSWORD_CHANGED`, etc.)
- `description` TEXT NOT NULL
- `ip_address` TEXT NOT NULL
- `timestamp` TEXT DEFAULT CURRENT_TIMESTAMP

### 4. Demo Customer Credentials & Data
* **Bank ID:** `SB100001`
* **Demo Password:** `Demo@12345`
* **Account Number:** `ACC100001` (Masked: `XXXX XXXX 0001`)
* **Customer Name:** `Rajesh Sharma`
* **Account Type:** `Savings Account`
* **Available Balance:** `₹84,250.00`
* **Seeded Transactions:** 25 realistic transactions covering grocery, salary credit, utilities, dining, ATM, and UPI.
* **Security Verification PIN:** `778899`

### 5. Backend Authorization & Data Isolation
* All customer endpoints (`/api/account/**`, `/api/transactions/**`, `/api/security/**`) require a valid Bearer token.
* The Java backend resolves the customer identity strictly from the authenticated token context (`authentication.getName()`).
* Querying a transaction (`/api/transactions/{id}`) enforces that the transaction belongs strictly to the authenticated customer's account ID, returning HTTP 404 if not found or unauthorized.
