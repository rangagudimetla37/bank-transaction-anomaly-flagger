# Architecture — Bank Transaction Anomaly Flagger

## System Overview

```
┌─────────────────────────────────────┐
│         React Frontend              │
│   TypeScript + Vite  Port 5173      │
│                                     │
│  AuthContext  →  api.ts  →  Pages   │
└──────────────┬──────────────────────┘
               │  HTTP/REST (/api proxy)
┌──────────────▼──────────────────────┐
│      Spring Boot Backend            │
│   Java 17 + Maven   Port 8080       │
│                                     │
│  SecurityConfig (token filter)      │
│  Controllers → Services → Repos     │
│                                     │
│  ┌──────────────┐  ┌─────────────┐  │
│  │  Rule Engine │  │ Graph Svc   │  │
│  │  (DMGT rules)│  │ (network)   │  │
│  └──────┬───────┘  └──────┬──────┘  │
│         │                 │         │
│  ┌──────▼─────────────────▼──────┐  │
│  │   FraudDetectionService       │  │
│  │   compositeScore = rule×0.6   │  │
│  │                 + stat×0.4    │  │
│  └──────────────┬────────────────┘  │
│                 │ subprocess         │
└─────────────────┼────────────────---┘
                  │
    ┌─────────────▼──────────────┐
    │  Python Anomaly Detector   │
    │  anomaly_detector.py       │
    │  Z-score calculation       │
    │  (Java fallback if absent) │
    └────────────────────────────┘
                  │
    ┌─────────────▼──────────────┐
    │  SQLite Database           │
    │  backend/database/         │
    │  securebank.db             │
    │                            │
    │  10 tables:                │
    │  users, accounts,          │
    │  transactions,             │
    │  fraud_alerts,             │
    │  review_cases,             │
    │  review_audit_logs,        │
    │  password_reset_tokens,    │
    │  security_activities,      │
    │  security_audit_logs,      │
    │  sqlite_sequence           │
    └────────────────────────────┘
```

## Data Flow — Transaction Analysis

```
Customer visits /transactions/:id
        ↓
Frontend calls GET /api/transactions/{id}/analysis
        ↓
TransactionAnalysisController
        ↓
TransactionAnalysisService
        ├─→ RuleEngine.evaluate()
        │     Checks 6 propositional rules
        │     Returns rule score + explanations
        │
        └─→ PythonAnomalyService.analyze()
              Calls anomaly_detector.py via subprocess
              Returns Z-score + statistical score
              (Java fallback if Python unavailable)
        ↓
FraudDetectionService.compositeScore()
        compositeScore = ruleScore×0.6 + statScore×0.4
        ↓
Returns JSON: ruleScore, statisticalScore,
              compositeScore, riskLevel,
              triggeredRules, explanations,
              zscore, statisticalExplanation
```

## Security Architecture

```
Request → TokenAuthenticationFilter
              ↓
         Extracts Bearer token from Authorization header
              ↓
         TokenService.validateSession()
         (in-memory ConcurrentHashMap, 2h expiry)
              ↓
         Injects SecurityContext with bankId + role
              ↓
         Controller reads authenticated user
              ↓
         Repository filters by account ownership
         (CUSTOMER sees only own account data)
```

## Package Structure

```
com.securebank
├── config/
│   ├── DatabaseConfig.java        SQLite DataSource (dynamic path resolution)
│   ├── DatabaseInitializer.java   Schema creation + demo data seeding
│   ├── RiskConfig.java            Risk weights, thresholds, constants
│   └── SecurityConfig.java        Spring Security config, CORS
│
├── controller/
│   ├── AuthController.java        /api/auth/*
│   ├── AccountController.java     /api/account/*
│   ├── TransactionController.java /api/transactions
│   ├── TransactionAnalysisController.java  /api/transactions/{id}/analysis
│   ├── TransactionGraphController.java     /api/transactions/network
│   ├── AlertController.java       /api/alerts/*
│   ├── ReviewController.java      /api/review/*
│   └── SecurityController.java    /api/security/*
│
├── model/                         Java OOP models (plain POJOs)
├── repository/                    SQLite JDBC data access
├── rules/                         Propositional logic rule engine
├── security/                      Token service, auth filter, login limiter
└── service/                       Business logic layer
```
