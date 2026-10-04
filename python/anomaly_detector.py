"""
SecureBank - Transaction Statistical Anomaly Detector (Phase 4)
College Mini-Project: Bank Transaction Anomaly Flagger

Calculates statistical outlier scores using Z-Score methodology:
    z = (x - mean) / standard_deviation

Where:
    x = transaction amount
    mean = customer's historical average transaction amount
    standard_deviation = customer's historical standard deviation
"""

import sys
import os
import argparse
import math
import json
import pandas as pd
import numpy as np

# Ensure UTF-8 output encoding on Windows consoles
if hasattr(sys.stdout, 'reconfigure'):
    try:
        sys.stdout.reconfigure(encoding='utf-8')
        sys.stderr.reconfigure(encoding='utf-8')
    except Exception:
        pass


DEFAULT_Z_THRESHOLD = 2.5
MIN_HISTORY_REQUIRED = 3


def calculate_z_score_metrics(df, target_txn_id=None, z_threshold=DEFAULT_Z_THRESHOLD, min_history=MIN_HISTORY_REQUIRED):
    """
    Computes Z-Score, normalized statistical anomaly score (0-100), and anomaly flag.
    Handles all statistical edge cases:
      - Insufficient transaction history (< min_history)
      - Single transaction accounts
      - Zero or near-zero standard deviation
      - Normal transaction distributions
    """
    results = []

    # Ensure required columns exist
    if 'transaction_id' not in df.columns or 'amount' not in df.columns:
        raise ValueError("Input data must contain 'transaction_id' and 'amount' columns.")

    if 'account_id' not in df.columns:
        df['account_id'] = 'DEFAULT_ACC'

    # Clean amount column
    df['amount'] = pd.to_numeric(df['amount'], errors='coerce').fillna(0.0)

    # Group by account_id so customer accounts are isolated statistically
    grouped = df.groupby('account_id')

    for account_id, group in grouped:
        all_amounts = group['amount'].values
        total_n = len(all_amounts)

        for _, row in group.iterrows():
            txn_id = str(row['transaction_id'])
            amt = float(row['amount'])

            # Determine historical baseline for this transaction:
            # Prefer other transactions in account so current amount does not distort its own baseline
            other_rows = group[group['transaction_id'] != txn_id]
            other_amounts = other_rows['amount'].values

            if len(other_amounts) >= min_history:
                hist_mean = float(np.mean(other_amounts))
                hist_std = float(np.std(other_amounts, ddof=1))
                effective_n = len(other_amounts)
            elif total_n >= min_history:
                hist_mean = float(np.mean(all_amounts))
                hist_std = float(np.std(all_amounts, ddof=1))
                effective_n = total_n
            else:
                # Insufficient history
                hist_mean = float(np.mean(all_amounts)) if total_n > 0 else amt
                hist_std = 0.0
                results.append({
                    'transaction_id': txn_id,
                    'account_id': str(account_id),
                    'amount': round(amt, 2),
                    'historical_mean': round(hist_mean, 2),
                    'historical_std': 0.0,
                    'z_score': 0.0,
                    'abs_z_score': 0.0,
                    'statistical_score': 0,
                    'is_anomaly': False,
                    'explanation': f"Insufficient transaction history (n={total_n}, minimum {min_history} required for baseline)."
                })
                continue

            # Standard deviation check
            is_zero_variance = hist_std < 1e-4

            if is_zero_variance:
                diff = abs(amt - hist_mean)
                if diff < 1e-4:
                    z = 0.0
                    abs_z = 0.0
                    stat_score = 0
                    is_anomaly = False
                    explanation = f"Transaction amount (INR {amt:,.2f}) matches uniform historical baseline (zero variance)."
                else:
                    effective_std = max(hist_std, hist_mean * 0.05 if hist_mean > 0 else 1.0)
                    z = (amt - hist_mean) / effective_std
                    abs_z = abs(z)
                    stat_score = min(100, int(round((abs_z / 4.0) * 100)))
                    is_anomaly = abs_z >= z_threshold
                    explanation = (
                        f"Amount (INR {amt:,.2f}) deviates from uniform historical baseline of INR {hist_mean:,.2f} "
                        f"(effective Z-score: {z:+.2f})."
                    )
            else:
                z = (amt - hist_mean) / hist_std
                abs_z = abs(z)

                stat_score = min(100, max(0, int(round((abs_z / 4.0) * 100))))
                is_anomaly = abs_z >= z_threshold

                if is_anomaly:
                    explanation = (
                        f"Statistical outlier detected: Z-Score of {z:+.2f} indicates amount (INR {amt:,.2f}) is "
                        f"{abs_z:.2f} standard deviations from customer historical mean (INR {hist_mean:,.2f}, std dev: INR {hist_std:,.2f})."
                    )
                else:
                    explanation = (
                        f"Amount (INR {amt:,.2f}) is within normal statistical distribution "
                        f"(Z-Score: {z:+.2f}, historical mean: INR {hist_mean:,.2f}, std dev: INR {hist_std:,.2f})."
                    )

            results.append({
                'transaction_id': txn_id,
                'account_id': str(account_id),
                'amount': round(amt, 2),
                'historical_mean': round(hist_mean, 2),
                'historical_std': round(hist_std, 2),
                'z_score': round(z, 2),
                'abs_z_score': round(abs_z, 2),
                'statistical_score': stat_score,
                'is_anomaly': is_anomaly,
                'explanation': explanation
            })

    result_df = pd.DataFrame(results)

    # If target transaction is requested, sort so it is first or filter
    if target_txn_id:
        target_rows = result_df[result_df['transaction_id'] == target_txn_id]
        if not target_rows.empty:
            # Place target row at top
            other_rows = result_df[result_df['transaction_id'] != target_txn_id]
            result_df = pd.concat([target_rows, other_rows], ignore_index=True)

    return result_df


def run_pipeline(input_path, output_path, target_txn_id=None, z_threshold=DEFAULT_Z_THRESHOLD):
    """Loads input CSV/JSON, calculates Z-Scores, and writes output CSV/JSON."""
    if not os.path.exists(input_path):
        raise FileNotFoundError(f"Input file not found: {input_path}")

    # Read input based on file extension
    if input_path.endswith('.json'):
        df = pd.read_json(input_path, encoding='utf-8')
    else:
        df = pd.read_csv(input_path, encoding='utf-8')

    if df.empty:
        raise ValueError("Input transaction file is empty.")

    result_df = calculate_z_score_metrics(df, target_txn_id=target_txn_id, z_threshold=z_threshold)

    # Ensure output parent directory exists
    output_dir = os.path.dirname(output_path)
    if output_dir and not os.path.exists(output_dir):
        os.makedirs(output_dir, exist_ok=True)

    # Write output
    if output_path.endswith('.json'):
        result_df.to_json(output_path, orient='records', indent=2, force_ascii=False)
    else:
        result_df.to_csv(output_path, index=False, encoding='utf-8')

    print(f"Successfully processed {len(result_df)} transactions.")
    print(f"Output saved to: {output_path}")

    # Summary preview if target specified
    if target_txn_id:
        target_info = result_df[result_df['transaction_id'] == target_txn_id]
        if not target_info.empty:
            r = target_info.iloc[0]
            print(f"Target [{target_txn_id}]: Amount=Rs.{r['amount']}, Mean=Rs.{r['historical_mean']}, "
                  f"Z={r['z_score']:+.2f}, StatScore={r['statistical_score']}, Outlier={r['is_anomaly']}")


def main():
    parser = argparse.ArgumentParser(description="SecureBank Statistical Anomaly Detector (Z-Score)")
    parser.add_argument('input', nargs='?', help="Input CSV or JSON path containing transactions")
    parser.add_argument('output', nargs='?', help="Output CSV or JSON path for statistical results")
    parser.add_argument('target_txn', nargs='?', help="Optional target transaction ID to prioritize/evaluate")

    # Optional named arguments
    parser.add_argument('--input', dest='named_input', help="Named input file path")
    parser.add_argument('--output', dest='named_output', help="Named output file path")
    parser.add_argument('--target-txn', dest='named_target_txn', help="Named target transaction ID")
    parser.add_argument('--threshold', type=float, default=DEFAULT_Z_THRESHOLD, help="Z-score anomaly threshold (default: 2.5)")
    parser.add_argument('--min-history', type=int, default=MIN_HISTORY_REQUIRED, help="Minimum history transactions required")

    args = parser.parse_args()

    input_file = args.named_input or args.input
    output_file = args.named_output or args.output
    target_txn = args.named_target_txn or args.target_txn

    if not input_file or not output_file:
        parser.print_help()
        sys.exit(1)

    try:
        run_pipeline(input_file, output_file, target_txn_id=target_txn, z_threshold=args.threshold)
    except Exception as e:
        print(f"Error during anomaly detection: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == '__main__':
    main()
