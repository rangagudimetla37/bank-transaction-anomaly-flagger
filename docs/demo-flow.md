# Demo Flow — Bank Transaction Anomaly Flagger

> **Start:** Backend running on port 8080. Frontend running on port 5173.  
> Open browser to: http://localhost:5173/login

---

## Customer Demo Flow

### Step 1 — Login as Customer

- **Bank ID:** `SB100001`
- **Password:** `Demo@12345`
- Click **Login**
- → Redirected to `/dashboard`

### Step 2 — Dashboard

- View account balance and account number
- Observe the **risk summary** panel (alerts count, risk level breakdown)
- Note the system notice: "Multi-layer risk analysis active"

### Step 3 — Transactions

- Click **My Transactions** in the navigation
- Observe the full transaction list (31 transactions for SB100001)
- Try the **search** field (search by description, recipient, or type)
- Try the **filter** by transaction type

### Step 4 — High-Risk Transaction Detail

- Find transaction: **`TXN-20261004-0035`** — ₹95,000 ONLINE (or sort by amount descending)
- Click on it to open `/transactions/TXN-20261004-0035`
- Observe:
  - **Rule Engine Results:** 5 rules triggered (HIGH_AMOUNT, LOCATION_CHANGE, NEW_RECIPIENT, RAPID_TRANSACTION, UNUSUAL_TIME)
  - **Rule Score:** 90/100
  - **Statistical Analysis:** Z-Score 4.88, Statistical Score 100/100
  - **Final Risk Score:** 94/100 — HIGH RISK
  - **Transaction Graph** panel at the bottom

### Step 5 — Fraud Alerts

- Click **Alerts** in the navigation
- Observe the 3 demo alerts (1 HIGH_RISK, 2 SUSPICIOUS)
- Click on any alert to view details
- Note: alert linked to the transaction, risk breakdown visible

### Step 6 — Transaction Network

- Click **Transaction Network** in the navigation
- Observe the interactive SVG graph
- Nodes: Account → Recipients
- Edges: Transactions (colour-coded by amount)
- Use scroll to zoom, drag to pan

### Step 7 — Security Center

- Click **My Account** / **Security Center**
- View 4 security status cards
- See the security activity log (last events)
- Optionally try the **Change Password** form

### Step 8 — Logout

- Click the user icon (top right) → **Logout**
- → Redirected to `/login`

---

## Reviewer Demo Flow

### Step 1 — Login as Reviewer

- **Bank ID:** `REV100001`
- **Password:** `Review@12345`
- Click **Login**
- → Redirected to `/review` (Review Dashboard)

### Step 2 — Review Dashboard

- Observe the **statistics cards**: Total Cases, New, In Review, Escalated, Resolved
- See the **Recent Cases** table with case IDs and risk levels

### Step 3 — Investigation Cases

- Click **Investigation Cases** in the navigation
- Observe all cases with filter tabs: ALL / NEW / IN_REVIEW / ESCALATED / RESOLVED / HIGH_RISK
- Click on **CASE-20261004-0002** (ESCALATED case)

### Step 4 — Case Investigation Workbench

- View the full case detail page
- **Transaction Data** panel: amount, type, location, recipient, timestamp
- **Risk Analysis** panel: rule score, statistical score, final score, all triggered rules
- **Graph Analysis** panel: network indicators for this transaction
- **Reviewer Notes** section: type a note and save
- **Decision** section: see existing decision or submit a new one

### Step 5 — Fraud Alerts (Reviewer)

- Click **Fraud Alerts** in the navigation
- Reviewer can see ALL system alerts (not just one account)
- Update alert status (VIEWED / UNDER_REVIEW / RESOLVED / FALSE_POSITIVE)

### Step 6 — Transaction Network (Reviewer)

- Click **Transaction Network**
- Reviewer sees the full account network (same view as customer but for demo account)

### Step 7 — Security Center (Reviewer)

- Click **Security Center**
- View reviewer's own security activity

### Step 8 — Logout

- Click user icon → **Logout**

---

## Key Points to Highlight in Demo

1. **Role separation** — Customer and Reviewer see different navigation and data
2. **Multi-layer analysis** — Rule engine + Python Z-score + Graph = composite score
3. **No hardcoded risk** — score is calculated live for each transaction
4. **OOP concepts** — `Transaction`, `FraudAlert`, `ReviewCase`, `GraphNode`, `RuleResult` are Java classes
5. **Propositional logic** — each rule is an independent `if-condition → risk-points` proposition
6. **Z-score formula** — demonstrated live in the statistical analysis panel
7. **Graph theory** — visualized in the transaction network page
8. **RBAC enforced** — backend rejects any cross-role data access at the repository level
9. **Audit trail** — every security event is recorded in `security_audit_logs`
10. **Localhost only** — no cloud, no external API, no real banking data
