# SecureBank - Statistical Anomaly Detector (Phase 4)

This directory contains the Python statistical outlier and anomaly detection engine for the **Bank Transaction Anomaly Flagger** mini-project.

## Methodology: Z-Score Outlier Analysis

The detector models the spending distribution of individual customer accounts to identify statistical anomalies using the **Z-Score** formulation:

$$z = \frac{x - \mu}{\sigma}$$

Where:
- $x$ = Transaction amount being screened
- $\mu$ = Historical mean transaction amount for the customer's account
- $\sigma$ = Historical sample standard deviation of transaction amounts ($ddof=1$)

### Metrics Produced:
1. **`z_score`**: Directional deviation from the mean in standard deviation units.
2. **`abs_z_score`**: Magnitude of deviation ($|z|$).
3. **`statistical_score`**: Normalized anomaly score from $0$ to $100$ ($Score = \min(100, \text{round}(|z| / 4.0 \times 100))$).
4. **`is_anomaly`**: Boolean flag triggered when $|z| \ge 2.5$ (configurable threshold).
5. **`explanation`**: Human-readable, non-accusatory statistical justification.

---

## Statistical Edge Cases Handled

1. **Insufficient Transaction History ($N < 3$):**
   - Flags as normal (`is_anomaly = False`, score = 0).
   - Explanation notes minimum historical sample required to prevent false positives.
2. **Single Transaction Account ($N = 1$):**
   - Avoids calculation of variance; sets `is_anomaly = False`.
3. **Zero Variance ($\sigma = 0$):**
   - If historical transactions are all identical (e.g. ₹500 recurring), matching amounts yield score = 0.
   - If a new amount deviates substantially, an adaptive non-zero baseline is used to prevent `ZeroDivisionError`.
4. **Extreme Small Variance ($\sigma < 10^{-4}$):**
   - Guarded against overflow and division-by-zero.
5. **Standard Distribution:**
   - Normal spending remains within $|z| < 2.5$ and receives low anomaly scores.

---

## Usage

### 1. Requirements
Install dependencies using Python 3:
```bash
pip install -r requirements.txt
```

### 2. Command Line Execution
```bash
# General syntax:
python anomaly_detector.py <input_file> <output_file> [target_transaction_id]

# Or using explicit flags:
python anomaly_detector.py --input data/export.csv --output data/results.csv --target-txn TXN-20261004-0026
```

### 3. File Formats
Supported input formats: **CSV** and **JSON**.
Output formats: **CSV** and **JSON** (auto-detected based on output file extension).
