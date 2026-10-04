import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../services/api';
import type { FraudAlert, AlertSummary, RiskLevelType } from '../services/api';
import { Header } from '../components/Header';
import { Footer } from '../components/Footer';
import {
  ShieldAlert,
  AlertTriangle,
  Search,
  ShieldCheck,
  Clock,
  Eye,
  RefreshCw
} from 'lucide-react';

export const AlertsPage: React.FC = () => {
  const [alerts, setAlerts] = useState<FraudAlert[]>([]);
  const [summary, setSummary] = useState<AlertSummary | null>(null);
  const [filterStatus, setFilterStatus] = useState<string>('ALL');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [errorMessage, setErrorMessage] = useState<string>('');

  const loadAlerts = async () => {
    setIsLoading(true);
    setErrorMessage('');
    try {
      const [alertsData, summaryData] = await Promise.all([
        api.getAlerts(),
        api.getAlertSummary().catch(() => null),
      ]);
      setAlerts(alertsData || []);
      setSummary(summaryData);
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to load fraud alerts.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadAlerts();
  }, []);

  const getRiskBadgeClass = (level: RiskLevelType) => {
    switch (level) {
      case 'HIGH_RISK':
        return 'risk-badge-high_risk';
      case 'SUSPICIOUS':
        return 'risk-badge-suspicious';
      case 'WATCH':
        return 'risk-badge-watch';
      default:
        return 'risk-badge-normal';
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

  const filteredAlerts = alerts.filter((alert) => {
    // Status filter
    if (filterStatus === 'NEW' && alert.status !== 'NEW') return false;
    if (filterStatus === 'SUSPICIOUS' && alert.riskLevel !== 'SUSPICIOUS') return false;
    if (filterStatus === 'HIGH_RISK' && alert.riskLevel !== 'HIGH_RISK') return false;
    if (filterStatus === 'RESOLVED' && alert.status !== 'RESOLVED' && alert.status !== 'VIEWED') return false;

    // Search query
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase().trim();
      const matchAlertId = alert.alertId.toLowerCase().includes(q);
      const matchTxnId = alert.transactionId.toLowerCase().includes(q);
      const matchDesc = alert.description?.toLowerCase().includes(q);
      if (!matchAlertId && !matchTxnId && !matchDesc) return false;
    }

    return true;
  });

  return (
    <div className="bank-app-layout">
      <Header />

      <main className="dashboard-main container">
        {/* Page Title & Breadcrumb */}
        <div className="dashboard-welcome-row" style={{ alignItems: 'flex-start' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
              <ShieldAlert size={26} color="#d97706" />
              <h1 style={{ fontSize: '1.6rem', color: '#003366', fontWeight: 700 }}>
                Fraud &amp; Anomaly Alerts
              </h1>
            </div>
            <p className="welcome-subtitle">
              Transactions requiring additional attention • Composite Screening (Rules + Statistical Z-Score)
            </p>
          </div>

          <button
            onClick={loadAlerts}
            className="btn-secondary btn-sm"
            style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
            title="Refresh alerts"
          >
            <RefreshCw size={14} />
            <span>Refresh</span>
          </button>
        </div>

        {/* Summary Metric Cards */}
        <div className="summary-grid" style={{ marginBottom: '24px' }}>
          <div className="summary-card">
            <div className="summary-header">
              <span className="summary-label">Total Alerts</span>
              <div className="summary-icon icon-balance">
                <ShieldAlert size={18} />
              </div>
            </div>
            <div className="summary-amount" style={{ color: '#003366' }}>
              {summary ? summary.totalAlerts : alerts.length}
            </div>
            <div className="summary-subtext">System-generated anomaly flags</div>
          </div>

          <div className="summary-card">
            <div className="summary-header">
              <span className="summary-label">High Risk</span>
              <div className="summary-icon" style={{ backgroundColor: '#fee2e2', color: '#dc2626' }}>
                <AlertTriangle size={18} />
              </div>
            </div>
            <div className="summary-amount" style={{ color: '#dc2626' }}>
              {summary ? summary.highRiskCount : alerts.filter((a) => a.riskLevel === 'HIGH_RISK').length}
            </div>
            <div className="summary-subtext">Score ≥ 80 / 100</div>
          </div>

          <div className="summary-card">
            <div className="summary-header">
              <span className="summary-label">Suspicious</span>
              <div className="summary-icon" style={{ backgroundColor: '#fef3c7', color: '#d97706' }}>
                <Clock size={18} />
              </div>
            </div>
            <div className="summary-amount" style={{ color: '#d97706' }}>
              {summary ? summary.suspiciousCount : alerts.filter((a) => a.riskLevel === 'SUSPICIOUS').length}
            </div>
            <div className="summary-subtext">Score 60–79 / 100</div>
          </div>

          <div className="summary-card">
            <div className="summary-header">
              <span className="summary-label">New / Actionable</span>
              <div className="summary-icon" style={{ backgroundColor: '#e0f2fe', color: '#0284c7' }}>
                <Eye size={18} />
              </div>
            </div>
            <div className="summary-amount" style={{ color: '#0284c7' }}>
              {summary ? summary.newAlerts : alerts.filter((a) => a.status === 'NEW').length}
            </div>
            <div className="summary-subtext">Unreviewed notification flags</div>
          </div>
        </div>

        {/* Content Card with Filters and Table */}
        <div className="content-card">
          {/* Filter Bar */}
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '14px', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            {/* Filter Tabs */}
            <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
              {[
                { label: 'All', value: 'ALL' },
                { label: 'New', value: 'NEW' },
                { label: 'High Risk', value: 'HIGH_RISK' },
                { label: 'Suspicious', value: 'SUSPICIOUS' },
                { label: 'Viewed / Handled', value: 'RESOLVED' },
              ].map((tab) => (
                <button
                  key={tab.value}
                  onClick={() => setFilterStatus(tab.value)}
                  style={{
                    padding: '6px 14px',
                    borderRadius: '6px',
                    fontSize: '0.84rem',
                    fontWeight: 600,
                    cursor: 'pointer',
                    border: '1px solid',
                    transition: 'all 0.2s',
                    backgroundColor: filterStatus === tab.value ? '#003366' : '#ffffff',
                    color: filterStatus === tab.value ? '#ffffff' : '#475569',
                    borderColor: filterStatus === tab.value ? '#003366' : '#cbd5e1',
                  }}
                >
                  {tab.label}
                </button>
              ))}
            </div>

            {/* Search Input */}
            <div style={{ position: 'relative', minWidth: '260px' }}>
              <Search size={16} color="#94a3b8" style={{ position: 'absolute', left: '10px', top: '50%', transform: 'translateY(-50%)' }} />
              <input
                type="text"
                placeholder="Search Alert or Transaction ID..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                style={{
                  width: '100%',
                  padding: '7px 12px 7px 34px',
                  borderRadius: '6px',
                  border: '1px solid #cbd5e1',
                  fontSize: '0.85rem',
                  outline: 'none',
                }}
              />
            </div>
          </div>

          {/* Error Message */}
          {errorMessage && (
            <div className="alert-box alert-error" style={{ marginBottom: '16px' }}>
              <span>{errorMessage}</span>
            </div>
          )}

          {/* Alerts Table */}
          {isLoading ? (
            <div style={{ textAlign: 'center', padding: '48px 0', color: '#64748b' }}>
              <RefreshCw className="spin" size={24} style={{ margin: '0 auto 8px' }} />
              <p style={{ fontSize: '0.9rem' }}>Loading alerts...</p>
            </div>
          ) : filteredAlerts.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '48px 0', backgroundColor: '#f8fafc', borderRadius: '8px' }}>
              <ShieldCheck size={36} color="#16a34a" style={{ margin: '0 auto 8px' }} />
              <h3 style={{ fontSize: '1.05rem', color: '#1e293b', fontWeight: 600 }}>No Alerts Found</h3>
              <p style={{ fontSize: '0.85rem', color: '#64748b', marginTop: '4px' }}>
                {searchQuery || filterStatus !== 'ALL'
                  ? 'No alerts match the active filter or search criteria.'
                  : 'No high-risk or suspicious anomalies have triggered fraud alerts.'}
              </p>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="banking-table">
                <thead>
                  <tr>
                    <th>Alert ID</th>
                    <th>Transaction</th>
                    <th>Risk Score</th>
                    <th>Risk Level</th>
                    <th>Reason / Details</th>
                    <th>Status</th>
                    <th>Created</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredAlerts.map((alert) => (
                    <tr key={alert.alertId}>
                      <td>
                        <strong style={{ fontSize: '0.86rem', color: '#003366' }}>{alert.alertId}</strong>
                      </td>
                      <td>
                        <Link
                          to={`/transactions/${alert.transactionId}`}
                          className="txn-id-tag"
                          style={{ textDecoration: 'none' }}
                          title="View Transaction"
                        >
                          {alert.transactionId}
                        </Link>
                      </td>
                      <td>
                        <strong style={{ fontSize: '0.94rem', color: alert.riskScore >= 80 ? '#dc2626' : '#d97706' }}>
                          {alert.riskScore}
                        </strong>
                        <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}> / 100</span>
                      </td>
                      <td>
                        <span className={`risk-badge ${getRiskBadgeClass(alert.riskLevel)}`}>
                          {alert.riskLevel.replace('_', ' ')}
                        </span>
                      </td>
                      <td>
                        <span style={{ fontSize: '0.82rem', color: '#475569', display: 'block', maxWidth: '320px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                          {alert.description}
                        </span>
                      </td>
                      <td>
                        <span
                          style={{
                            fontSize: '0.75rem',
                            fontWeight: 700,
                            padding: '3px 8px',
                            borderRadius: '4px',
                            ...getStatusBadgeStyle(alert.status),
                          }}
                        >
                          {alert.status.replace('_', ' ')}
                        </span>
                      </td>
                      <td>
                        <span style={{ fontSize: '0.8rem', color: '#64748b' }}>{alert.createdAt}</span>
                      </td>
                      <td>
                        <Link
                          to={`/alerts/${alert.alertId}`}
                          className="btn-secondary btn-sm"
                          style={{ padding: '4px 12px' }}
                        >
                          View
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </main>

      <Footer />
    </div>
  );
};
