# SecureBank

**Secure Digital Banking — Bank Transaction Anomaly/Fraud Detection Mini Project**

> **College Mini-Project** — A localhost-only banking security prototype that detects potentially anomalous transactions using multi-layer analysis.
>
> ⚠️ **IMPORTANT:** This is a college demonstration prototype. It is **NOT** intended for production banking use. Anomaly detection does not prove fraud.

---

## Overview

**Bank Transaction Anomaly Flagger** is a full-stack web application simulating a banking security system. It ingests synthetic customer transactions, applies a multi-layer anomaly detection pipeline, generates fraud alerts, and supports a structured manual review workflow for high-risk cases.

The system combines:

- ☕ **Java rule-based analysis** — propositional logic rule engine (DMGT concepts)
- 🐍 **Python statistical analysis** — Z-score based outlier detection
- 🕸️ **Transaction network/graph analysis** — account-recipient relationship mapping
- 🔢 **Composite risk scoring** — weighted combination of rule and statistical scores
- 🚨 **Fraud/anomaly alert generation** — automatic for risk score ≥ 60
- 🔍 **Manual review workflow** — structured investigation and decision recording
- 🔐 **Security auditing** — full audit log of all authentication and security events

---

## Features

### Customer Portal
- Secure login with BCrypt password hashing
- Personal banking dashboard with risk summary
- Transaction list with search and filter
- Per-transaction deep-dive with full rule-by-rule analysis
- Statistical anomaly score with Z-score explanation
- Composite final risk score with risk level
- Fraud alert inbox with status tracking
- Interactive transaction network graph (SVG, zoom/pan)
- Account overview
- Security center: password change, security activity log

### Reviewer Portal
- Reviewer dashboard with case statistics
- Investigation case list with filter by status
- Per-case workbench: transaction data + full risk breakdown
- Reviewer notes (persistent)
- Case escalation
- Decision recording: LEGITIMATE / SUSPICIOUS_CONFIRMED / REQUIRES_FURTHER_REVIEW / CONFIRMED_FRAUD

### Security Features
- BCrypt password hashing (cost factor 12)
- Secure session tokens (SHA-256, in-memory store, 2-hour expiry)
- Role-based access control (CUSTOMER vs REVIEWER)
- Object-level authorization (customers see only their own data)
- Forgot password / time-limited reset tokens (SHA-256 hashed, 15-minute expiry, single-use)
- Password change with server-side strength validation
- Login attempt limiting (brute-force protection)
- Full security audit log
- CORS restricted to `localhost:5173`

---

## Architecture

```
┌─────────────────────────────────────┐
│         React Frontend              │
│   (TypeScript + Vite, Port 5173)    │
└──────────────┬──────────────────────┘
               │  HTTP/REST (via Vite proxy /api)
┌──────────────▼──────────────────────┐
│      Spring Boot Backend            │
│   (Java 17, Maven, Port 8080)       │
│                                     │
│  ┌─────────────┐ ┌────────────────┐ │
│  │ Rule Engine │ │ Graph Analysis │ │
│  │ (Java OOP)  │ │  (Java OOP)    │ │
│  └──────┬──────┘ └───────┬────────┘ │
│         │                │          │
│  ┌──────▼──────────────┐ │          │
│  │  FraudDetection     │◄┘          │
│  │  Service (combiner) │            │
│  └──────┬──────────────┘            │
│         │ subprocess call           │
└─────────┼─────────────────────────--┘
          │
┌─────────▼──────────────┐  ┌─────────────────────┐
│  Python Anomaly         │  │   SQLite Database   │
│  Detector (Z-Score)     │  │  (backend/database/ │
│  anomaly_detector.py    │  │   securebank.db)    │
└─────────────────────────┘  └─────────────────────┘
```

---

## Technologies

| Layer | Technology | Version |
|-------|-----------|---------|
| Frontend | React + TypeScript | 18.x |
| Frontend build | Vite | 5.x |
| Backend | Spring Boot | 3.x |
| Backend language | Java | 17 |
| Backend build | Apache Maven | 3.6+ |
| Database | SQLite (via JDBC) | 3.x |
| Statistical analysis | Python | 3.8+ |
| Python libraries | NumPy, SciPy | latest |
| Password hashing | BCrypt (Spring Security) | — |
| Session tokens | Custom SHA-256 tokens | — |
| Styling | Custom CSS design system | — |

---

## Risk Calculation

```
Final Risk Score = (Rule Score × 0.60) + (Statistical Score × 0.40)
```

| Score Range | Risk Level |
|-------------|-----------|
| 0 – 29 | 🟢 NORMAL |
| 30 – 59 | 🟡 WATCH |
| 60 – 79 | 🟠 SUSPICIOUS |
| 80 – 100 | 🔴 HIGH RISK |

> Fraud alerts are automatically generated for transactions with **Final Risk Score ≥ 60**.

### Rule Engine (Java)

The rule engine evaluates each transaction against propositional-logic rules:

| Rule ID | Description | Risk Points |
|---------|-------------|-------------|
| RULE_HIGH_AMOUNT | Amount > 3× account average | 25 |
| RULE_NEW_RECIPIENT | First-time recipient | 20 |
| RULE_UNUSUAL_TIME | Transaction between 00:00–05:00 | 15 |
| RULE_LOCATION_CHANGE | Location differs from usual | 15 |
| RULE_RAPID_TRANSACTION | Multiple transactions within 5 minutes | 15 |
| RULE_ROUND_AMOUNT | Suspiciously round large amount | 5 |

---

## Statistical Analysis

**Method:** Z-Score based anomaly detection.

```
z = (x − μ) / σ

where:
  x  = current transaction amount
  μ  = mean of customer's historical transaction amounts
  σ  = standard deviation of customer's historical amounts
```

The raw Z-score is normalised to a 0–100 statistical score:

```
statistical_score = min(100, max(0, |z| × 25))
```

A transaction is considered a statistical outlier when `|z| > 2.0`.

> If Python is unavailable, the backend automatically falls back to a Java-based Z-score implementation. The frontend is notified transparently.

---

## Graph Analysis

The transaction network maps account-to-recipient relationships:

```
Account
  ↓
Transaction (edge)
  ↓
Recipient Account (node)
  ↓
Location (node attribute)
```

**Graph indicators computed:**
- Unique recipient count (fan-out)
- Shared recipients between accounts (structural link)
- Recipient concentration score
- Location concentration (many transactions to same city)
- New/unrecognized recipients

---

## Security

| Feature | Implementation |
|---------|---------------|
| Password storage | BCrypt (cost factor 12) — hashes only |
| Session tokens | 32-byte `SecureRandom`, SHA-256 hashed in-memory, 2h expiry |
| Reset tokens | 32-byte `SecureRandom`, SHA-256 hash stored in DB, 15-min expiry, single-use |
| API authentication | Bearer token in `Authorization` header |
| Role-based access | `CUSTOMER` / `REVIEWER` — enforced server-side on every request |
| Object authorization | Customers can only access their own account data |
| Login protection | Account lockout after repeated failed attempts |
| CORS | Restricted to `localhost:5173` only |
| Audit logging | Every login, logout, password event recorded in `security_audit_logs` |
| Password validation | Server-side: min 8 chars, uppercase, lowercase, digit, special character |

---

## User Roles

### CUSTOMER
- View own transactions and account
- View own fraud alerts
- View per-transaction rule and statistical analysis
- View own transaction network graph
- Change password / manage security

### REVIEWER
- View all fraud alerts system-wide
- Access investigation cases
- Open cases and add investigation notes
- View full risk analysis for any transaction
- Submit case decisions
- Escalate cases for senior review

---

## Installation

### Prerequisites

| Tool | Version |
|------|---------|
| Java JDK | 17 or newer |
| Apache Maven | 3.6 or newer |
| Node.js + npm | 18 or newer |
| Python | 3.8 or newer |
| pip packages | `numpy`, `scipy` |

```bash
# Install Python dependencies
pip install numpy scipy
```

### 1. Clone

```bash
git clone <repository-url>
cd "bank-transaction-anomaly-flagger"
```

### 2. Backend

```bash
cd backend
mvn spring-boot:run
```

The backend starts on **http://localhost:8080**.  
SQLite database and all demo data are created automatically on first startup by `DatabaseInitializer.java`.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on **http://localhost:5173**.  
All `/api` calls are proxied to `localhost:8080` via Vite — no manual CORS configuration needed.

### 4. Python

Python is invoked as a subprocess by the Java backend. No separate server is needed.  
Ensure `python` (or `python3`) is on your system PATH and `numpy`/`scipy` are installed.

### 5. Database

The SQLite database is auto-created at `backend/database/securebank.db` on first backend startup.  
To reset to demo data: stop the backend, delete `backend/database/securebank.db`, and restart.

> **Windows shortcut:** Use `run-backend.bat` and `run-frontend.bat` in the project root.

---

## Running Locally

| Service | URL |
|---------|-----|
| Frontend (React) | http://localhost:5173 |
| Backend (API) | http://localhost:8080/api |
| Login | http://localhost:5173/login |
| Customer Dashboard | http://localhost:5173/dashboard |
| Transactions | http://localhost:5173/transactions |
| Fraud Alerts | http://localhost:5173/alerts |
| Transaction Network | http://localhost:5173/transaction-network |
| Security Center | http://localhost:5173/security |
| Reviewer Dashboard | http://localhost:5173/review |
| Investigation Cases | http://localhost:5173/review/cases |

---

## Demo Accounts

> **FOR LOCAL COLLEGE DEMONSTRATION ONLY.**  
> These are synthetic accounts created by `DatabaseInitializer.java` using BCrypt hashing.  
> Do not reuse these credentials anywhere else.

| Role | Bank ID | Password | Security PIN |
|------|---------|----------|--------------|
| Customer 1 | `SB100001` | `Demo@12345` | `778899` |
| Customer 2 | `SB100002` | `Demo@12345` | `778899` |
| Reviewer | `REV100001` | `Review@12345` | `778899` |

---

## Project Structure

```
bank-transaction-anomaly-flagger/
├── README.md
├── run-backend.bat           Windows launcher for backend
├── run-frontend.bat          Windows launcher for frontend
│
├── backend/                  Spring Boot Java backend
│   ├── pom.xml
│   ├── .env.example
│   └── src/
│       ├── main/java/com/securebank/
│       │   ├── config/       DatabaseConfig, SecurityConfig, RiskConfig, DatabaseInitializer
│       │   ├── controller/   REST API controllers (Auth, Account, Transaction, Alert, Review, Security, Graph)
│       │   ├── model/        Java OOP models (User, Account, Transaction, FraudAlert, ReviewCase, ...)
│       │   ├── repository/   SQLite data access layer
│       │   ├── rules/        Propositional logic rule engine (RuleEngine, TransactionRule, ...)
│       │   ├── security/     TokenService, TokenAuthenticationFilter, LoginAttemptService
│       │   └── service/      Business logic (FraudDetectionService, PythonAnomalyService, ManualReviewService, ...)
│       └── test/java/        Unit tests (RuleEngine, FraudDetection, ManualReview, PythonAnomaly, Graph)
│
├── frontend/                 React + TypeScript + Vite frontend
│   ├── package.json
│   ├── vite.config.ts
│   ├── .env.example
│   └── src/
│       ├── App.tsx           Route definitions
│       ├── context/          AuthContext (global auth state)
│       ├── components/       Header, Footer, ProtectedRoute
│       ├── pages/            All page components (Login, Dashboard, Transactions, Alerts, Review, ...)
│       └── services/         api.ts — centralized API layer
│
├── python/
│   ├── anomaly_detector.py   Z-score statistical anomaly detector
│   ├── test_anomaly_detector.py  Python unit tests
│   ├── requirements.txt      Python dependencies
│   └── data/                 Synthetic test CSV files (not real banking data)
│
└── docs/
    ├── architecture.md
    ├── demo-flow.md
    └── security.md
```

---

## Testing

### Java Unit Tests (26 tests)

```bash
cd backend
mvn test
```

Test classes:
- `RuleEngineTest` — 8 tests covering all propositional rules
- `FraudDetectionServiceTest` — 4 tests for composite risk scoring
- `ManualReviewServiceTest` — 5 tests for review workflow
- `PythonAnomalyServiceTest` — 4 tests including Python Z-score and Java fallback
- `TransactionGraphServiceTest` — 5 tests for graph analysis

### Python Tests

```bash
cd python
python -m pytest test_anomaly_detector.py -v
```

### Frontend TypeScript Build

```bash
cd frontend
npm run build
```

---

## Limitations

- **Localhost only** — not intended for internet or production deployment
- **SQLite** — not suitable for high-concurrency production use; chosen for simplicity
- **Demo data only** — all accounts and transactions are synthetic, seeded automatically
- **No email** — password reset tokens are returned in the API response (demonstration only)
- **No real ML** — anomaly detection uses statistical Z-score, not trained machine learning models
- **Anomaly ≠ fraud** — the system flags potentially suspicious behaviour; it does not prove fraud
- **Single node** — no clustering, load balancing, or distributed architecture
- **Windows batch scripts** — `run-*.bat` files are Windows-only; use equivalent shell commands on Linux/macOS

---

## Academic Context

This project was developed as a college mini-project demonstrating:

| Subject | Concepts |
|---------|---------|
| OOPJ | Classes, objects, encapsulation, inheritance, interfaces, collections |
| DMGT | Propositional logic rules, graph theory, network analysis |
| Security | Cryptographic hashing, role-based access control, audit logging |
| Statistics | Z-score, mean, standard deviation, score normalisation |
| Full-Stack | React SPA, REST API, relational database, inter-process communication |
