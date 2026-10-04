"""
Unit tests for SecureBank Python Statistical Anomaly Detector (Phase 4).
"""

import unittest
import pandas as pd
import numpy as np
from anomaly_detector import calculate_z_score_metrics


class TestStatisticalAnomalyDetector(unittest.TestCase):

    def test_normal_transaction(self):
        """Test transactions with normal amounts within standard deviation."""
        data = {
            'transaction_id': ['TXN1', 'TXN2', 'TXN3', 'TXN4', 'TXN5'],
            'account_id': ['ACC1', 'ACC1', 'ACC1', 'ACC1', 'ACC1'],
            'amount': [2000.0, 2200.0, 1900.0, 2100.0, 2050.0]
        }
        df = pd.DataFrame(data)
        result_df = calculate_z_score_metrics(df, target_txn_id='TXN5')

        row = result_df[result_df['transaction_id'] == 'TXN5'].iloc[0]
        self.assertFalse(row['is_anomaly'])
        self.assertLess(row['abs_z_score'], 2.0)
        self.assertLess(row['statistical_score'], 50)

    def test_outlier_transaction(self):
        """Test a strong statistical outlier with large z-score."""
        data = {
            'transaction_id': ['TXN1', 'TXN2', 'TXN3', 'TXN4', 'TXN_OUTLIER'],
            'account_id': ['ACC1', 'ACC1', 'ACC1', 'ACC1', 'ACC1'],
            'amount': [2000.0, 2200.0, 1900.0, 2100.0, 150000.0]
        }
        df = pd.DataFrame(data)
        result_df = calculate_z_score_metrics(df, target_txn_id='TXN_OUTLIER')

        row = result_df[result_df['transaction_id'] == 'TXN_OUTLIER'].iloc[0]
        # 150000 is heavily displaced
        self.assertGreater(row['abs_z_score'], 1.5)
        self.assertGreaterEqual(row['statistical_score'], 40)

    def test_insufficient_history(self):
        """Test handling of accounts with fewer than min_history transactions."""
        data = {
            'transaction_id': ['TXN1', 'TXN2'],
            'account_id': ['ACC_NEW', 'ACC_NEW'],
            'amount': [5000.0, 95000.0]
        }
        df = pd.DataFrame(data)
        result_df = calculate_z_score_metrics(df, min_history=3)

        for _, row in result_df.iterrows():
            self.assertFalse(row['is_anomaly'])
            self.assertEqual(row['z_score'], 0.0)
            self.assertEqual(row['statistical_score'], 0)
            self.assertIn("Insufficient transaction history", row['explanation'])

    def test_single_transaction_account(self):
        """Test account with only 1 transaction."""
        data = {
            'transaction_id': ['TXN1'],
            'account_id': ['ACC_SINGLE'],
            'amount': [50000.0]
        }
        df = pd.DataFrame(data)
        result_df = calculate_z_score_metrics(df)

        row = result_df.iloc[0]
        self.assertFalse(row['is_anomaly'])
        self.assertEqual(row['statistical_score'], 0)
        self.assertEqual(row['z_score'], 0.0)

    def test_zero_standard_deviation(self):
        """Test account where all historical transactions have identical amounts."""
        data = {
            'transaction_id': ['TXN1', 'TXN2', 'TXN3', 'TXN4'],
            'account_id': ['ACC_UNIFORM', 'ACC_UNIFORM', 'ACC_UNIFORM', 'ACC_UNIFORM'],
            'amount': [1000.0, 1000.0, 1000.0, 1000.0]
        }
        df = pd.DataFrame(data)
        result_df = calculate_z_score_metrics(df)

        for _, row in result_df.iterrows():
            self.assertFalse(row['is_anomaly'])
            self.assertEqual(row['z_score'], 0.0)
            self.assertEqual(row['statistical_score'], 0)
            self.assertIn("matches uniform historical baseline", row['explanation'])


if __name__ == '__main__':
    unittest.main()
