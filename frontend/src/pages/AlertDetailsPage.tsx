import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import type { FraudAlert, FinalRiskAnalysis, TransactionData } from '../services/api';
import { Header } from '../components/Header';
import { Footer } from '../components/Footer';
import {
  ArrowLeft,
  ShieldAlert,
  AlertTriangle,
  ExternalLink,
  RefreshCw
} from 'lucide-react';

export const AlertDetailsPage: React.FC = () => {
  const { alertId } = useParams<{ alertId: string }>();
  const navigate = useNavigate();

  const [alert, setAlert] = useState<FraudAlert | null>(null);
  const [analysis, setAnalysis] = useState<FinalRiskAnalysis | null>(null);
  const [transaction, setTransaction] = useState<TransactionData | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [statusUpdating, setStatusUpdating] = useState<boolean>(false);
  const [feedbackMessage, setFeedbackMessage] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');

  useEffect(() => {
    const fetchAlertDetails = async () => {
      if (!alertId) return;
      setIsLoading(true);
      try {
        const alertData = await api.getAlertDetail(alertId);
        setAlert(alertData);

        // Fetch associated transaction and full risk analysis
        if (alertData.transactionId) {
          try {
            const [txnData, riskData] = await Promise.all([
              api.getTransactionDetail(alertData.transactionId).catch(() => null),
              api.getFinalRiskAnalysis(alertData.transactionId).catch(() => null),
            ]);
            setTransaction(txnData);
            setAnalysis(riskData);
          } catch (e) {
            // Non-blocking
          }
        }
      } catch (err: any) {
        setErrorMessage(err.message || 'Alert not found or unauthorized.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchAlertDetails();
  }, [alertId]);

  const handleUpdateStatus = async (newStatus: string) => {
    if (!alertId || !alert) return;
    setStatusUpdating(true);
    setFeedbackMessage('');
    try {
      await api.updateAlertStatus(alertId, newStatus);
      setAlert({ ...alert, status: newStatus as any });
      setFeedbackMessage(`Alert status updated to ${newStatus.replace('_', ' ')}.`);
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to update alert status.');
    } finally {
      setStatusUpdating(false);
    }
  };

  const getStatusBadgeStyle = (status: string) => {
    switch (status) {
      case 'NEW':
        return { backgroundColor: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca' };
      case 'REVIEW_REQUIRED':
        return { backgroundColor: '#fffbeb', color: '#b45309', border: '1px solid #fde68a' };
      case 'VIEWED':
        return { backgroundColor: '#eff6ff', color: '#1d4ed8', border: '1px solid #bfdbfe' };
      case 'RESOLVED':
        return { backgroundColor: '#f0fdf4', color: '#15803d', border: '1px solid #bbf7d0' };
      default:
        return { backgroundColor: '#f1f5f9', color: '#475569', border: '1px solid #e2e8f0' };
    }
  };

  return (
    <div className="bank-app-layout">
      <Header />

      <main className="dashboard-main container">
        {/* Navigation Breadcrumb */}
        <div style={{ marginBottom: '20px' }}>
          <button
            onClick={() => navigate('/alerts')}
            className="btn-secondary btn-sm"
            style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}
          >
            <ArrowLeft size={16} />
            <span>Back to Alerts</span>
          </button>
        </div>

        {errorMessage && (
          <div className="alert-box alert-error" style={{ marginBottom: '20px' }}>
            <span>{errorMessage}</span>
          </div>
        )}

        {feedbackMessage && (
          <div className="alert-box alert-success" style={{ marginBottom: '20px' }}>
            <span>{feedbackMessage}</span>
          </div>
        )}

        {isLoading ? (
          <div style={{ textAlign: 'center', padding: '60px 0', color: '#64748b' }}>
            <RefreshCw className="spin" size={28} style={{ margin: '0 auto 12px' }} />
            <p>Loading alert details...</p>
          </div>
        ) : !alert ? (
          <div className="content-card" style={{ textAlign: 'center', padding: '48px 24px' }}>
            <AlertTriangle size={36} color="#ea580c" style={{ margin: '0 auto 8px' }} />
            <h3>Alert Not Found</h3>
            <p style={{ color: '#64748b', marginTop: '6px' }}>
              The requested alert could not be retrieved or belongs to another customer account.
            </p>
          </div>
        ) : (
          <>
            {/* Top Alert Header Banner */}
            <div
              className="content-card"
              style={{
                borderLeft: alert.riskScore >= 80 ? '5px solid #dc2626' : '5px solid #d97706',
                marginBottom: '20px',
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '16px' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <ShieldAlert size={24} color={alert.riskScore >= 80 ? '#dc2626' : '#d97706'} />
                    <h2 style={{ fontSize: '1.4rem', color: '#003366', fontWeight: 700 }}>
                      {alert.title}
                    </h2>
                  </div>
                  <p style={{ color: '#64748b', fontSize: '0.85rem', marginTop: '4px' }}>
                    {alert.description}
                  </p>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <span
                    style={{
                      fontSize: '0.82rem',
                      fontWeight: 700,
                      padding: '4px 10px',
                      borderRadius: '4px',
                      ...getStatusBadgeStyle(alert.status),
                    }}
                  >
                    STATUS: {alert.status.replace('_', ' ')}
                  </span>
                </div>
              </div>

              {/* Status Action Buttons */}
              <div style={{ marginTop: '18px', paddingTop: '16px', borderTop: '1px solid #f1f5f9', display: 'flex', gap: '10px', flexWrap: 'wrap', alignItems: 'center' }}>
                <span style={{ fontSize: '0.82rem', color: '#64748b', fontWeight: 600 }}>Mark Status:</span>
                <button
                  onClick={() => handleUpdateStatus('VIEWED')}
                  disabled={statusUpdating || alert.status === 'VIEWED'}
                  className="btn-secondary btn-sm"
                  style={{ opacity: alert.status === 'VIEWED' ? 0.6 : 1 }}
                >
                  ✓ Mark as Viewed
                </button>
                <button
                  onClick={() => handleUpdateStatus('REVIEW_REQUIRED')}
                  disabled={statusUpdating || alert.status === 'REVIEW_REQUIRED'}
                  className="btn-secondary btn-sm"
                  style={{ opacity: alert.status === 'REVIEW_REQUIRED' ? 0.6 : 1 }}
                >
                  ⚠ Mark Review Required
                </button>
                <button
                  onClick={() => handleUpdateStatus('RESOLVED')}
                  disabled={statusUpdating || alert.status === 'RESOLVED'}
                  className="btn-secondary btn-sm"
                  style={{ opacity: alert.status === 'RESOLVED' ? 0.6 : 1 }}
                >
                  ✔ Mark Resolved
                </button>
              </div>
            </div>

            {/* Alert Meta Details Grid */}
            <div className="content-card" style={{ marginBottom: '20px' }}>
              <h3 style={{ fontSize: '1.1rem', color: '#003366', fontWeight: 700, marginBottom: '16px' }}>
                Alert Metadata
              </h3>

              <div className="transaction-details-grid">
                <div className="detail-item">
                  <div className="detail-label">Alert ID</div>
                  <div className="detail-value" style={{ fontWeight: 700, color: '#003366' }}>
                    {alert.alertId}
                  </div>
                </div>

                <div className="detail-item">
                  <div className="detail-label">Transaction ID</div>
                  <div className="detail-value">
                    <Link
                      to={`/transactions/${alert.transactionId}`}
                      style={{ color: '#0066cc', fontWeight: 600, textDecoration: 'underline' }}
                    >
                      {alert.transactionId}
                    </Link>
                  </div>
                </div>

                <div className="detail-item">
                  <div className="detail-label">Composite Risk Score</div>
                  <div className="detail-value" style={{ fontWeight: 700, color: alert.riskScore >= 80 ? '#dc2626' : '#d97706' }}>
                    {alert.riskScore} / 100 ({alert.riskLevel.replace('_', ' ')})
                  </div>
                </div>

                <div className="detail-item">
                  <div className="detail-label">Generated Timestamp</div>
                  <div className="detail-value">{alert.createdAt}</div>
                </div>
              </div>
            </div>

            {/* Risk Reasons Section */}
            {analysis && analysis.reasons && (
              <div className="content-card" style={{ marginBottom: '20px' }}>
                <h3 style={{ fontSize: '1.1rem', color: '#003366', fontWeight: 700, marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <AlertTriangle size={18} color="#ea580c" />
                  <span>Why Was This Transaction Flagged?</span>
                </h3>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                  {analysis.reasons.map((reason, idx) => (
                    <div
                      key={idx}
                      style={{
                        padding: '12px 16px',
                        backgroundColor: '#fffbeb',
                        border: '1px solid #fde68a',
                        borderRadius: '6px',
                        fontSize: '0.86rem',
                        color: '#92400e',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '10px',
                      }}
                    >
                      <span style={{ fontWeight: 700, color: '#d97706' }}>✓</span>
                      <span>{reason}</span>
                    </div>
                  ))}
                </div>

                {analysis.recommendation && (
                  <div style={{ marginTop: '16px', padding: '12px 16px', backgroundColor: '#eff6ff', borderRadius: '6px', border: '1px solid #bfdbfe' }}>
                    <strong style={{ fontSize: '0.84rem', color: '#1e40af' }}>Analyst Recommendation: </strong>
                    <span style={{ fontSize: '0.84rem', color: '#1e3a8a' }}>{analysis.recommendation}</span>
                  </div>
                )}
              </div>
            )}

            {/* Associated Transaction Overview */}
            {transaction && (
              <div className="content-card" style={{ marginBottom: '20px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', flexWrap: 'wrap', gap: '10px' }}>
                  <h3 style={{ fontSize: '1.1rem', color: '#003366', fontWeight: 700 }}>
                    Associated Transaction Details
                  </h3>
                  <Link
                    to={`/transactions/${alert.transactionId}`}
                    className="btn-primary btn-sm"
                    style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}
                  >
                    <span>View Full Transaction &amp; Analysis</span>
                    <ExternalLink size={14} />
                  </Link>
                </div>

                <div className="transaction-details-grid">
                  <div className="detail-item">
                    <div className="detail-label">Amount</div>
                    <div className="detail-value" style={{ fontWeight: 700, fontSize: '1.1rem', color: '#003366' }}>
                      ₹{transaction.amount.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                    </div>
                  </div>

                  <div className="detail-item">
                    <div className="detail-label">Recipient / Merchant</div>
                    <div className="detail-value">{transaction.recipient}</div>
                  </div>

                  <div className="detail-item">
                    <div className="detail-label">Location</div>
                    <div className="detail-value">{transaction.location}</div>
                  </div>

                  <div className="detail-item">
                    <div className="detail-label">Timestamp</div>
                    <div className="detail-value">{transaction.timestamp}</div>
                  </div>
                </div>
              </div>
            )}
          </>
        )}
      </main>

      <Footer />
    </div>
  );
};
