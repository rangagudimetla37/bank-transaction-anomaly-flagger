import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import type { ReviewCaseSummary, ReviewStats, RiskLevelType } from '../services/api';
import { Header } from '../components/Header';
import { Footer } from '../components/Footer';
import {
  ShieldAlert,
  FolderSearch,
  CheckCircle,
  AlertTriangle,
  Clock,
  ArrowRight,
  RefreshCw,
  Activity,
  Cpu,
  FileCheck2,
  ExternalLink
} from 'lucide-react';

export const ReviewDashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const [stats, setStats] = useState<ReviewStats | null>(null);
  const [cases, setCases] = useState<ReviewCaseSummary[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [errorMessage, setErrorMessage] = useState<string>('');

  const loadData = async () => {
    setIsLoading(true);
    setErrorMessage('');
    try {
      const [statsData, casesData] = await Promise.all([
        api.getReviewStats(),
        api.getReviewCases(),
      ]);
      setStats(statsData);
      setCases(casesData || []);
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to load reviewer dashboard data.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const getRiskBadge = (level: RiskLevelType, score: number) => {
    let bg = '#eff6ff';
    let text = '#1e40af';
    let border = '#bfdbfe';

    if (level === 'HIGH_RISK') {
      bg = '#fef2f2';
      text = '#991b1b';
      border = '#fecaca';
    } else if (level === 'SUSPICIOUS') {
      bg = '#fff7ed';
      text = '#c2410c';
      border = '#fed7aa';
    } else if (level === 'WATCH') {
      bg = '#fefce8';
      text = '#854d0e';
      border = '#fef08a';
    }

    return (
      <span style={{
        backgroundColor: bg,
        color: text,
        border: `1px solid ${border}`,
        padding: '3px 8px',
        borderRadius: '6px',
        fontSize: '0.75rem',
        fontWeight: 700,
        display: 'inline-flex',
        alignItems: 'center',
        gap: '4px'
      }}>
        {score}/100 {level.replace('_', ' ')}
      </span>
    );
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'NEW':
        return { bg: '#fef2f2', text: '#dc2626', border: '#fecaca', label: 'NEW' };
      case 'IN_REVIEW':
        return { bg: '#eff6ff', text: '#2563eb', border: '#bfdbfe', label: 'IN REVIEW' };
      case 'ESCALATED':
        return { bg: '#faf5ff', text: '#7c3aed', border: '#e9d5ff', label: 'ESCALATED' };
      case 'RESOLVED':
        return { bg: '#f0fdf4', text: '#16a34a', border: '#bbf7d0', label: 'RESOLVED' };
      default:
        return { bg: '#f8fafc', text: '#475569', border: '#e2e8f0', label: status };
    }
  };

  return (
    <div className="banking-page-layout">
      <Header />

      <main className="dashboard-content" style={{ maxWidth: '1200px', margin: '0 auto', padding: '2rem 1.5rem' }}>
        {/* Reviewer Portal Hero */}
        <div style={{
          backgroundColor: '#002244',
          borderRadius: '12px',
          padding: '2rem',
          color: '#ffffff',
          marginBottom: '2rem',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1.5rem',
          boxShadow: '0 4px 12px rgba(0, 34, 68, 0.15)'
        }}>
          <div>
            <div style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', backgroundColor: 'rgba(255, 255, 255, 0.15)', padding: '4px 10px', borderRadius: '20px', fontSize: '0.78rem', fontWeight: 600, marginBottom: '0.75rem' }}>
              <ShieldAlert size={14} color="#f87171" />
              <span>PHASE 6: HUMAN-IN-THE-LOOP INVESTIGATION</span>
            </div>
            <h1 style={{ fontSize: '1.75rem', fontWeight: 700, margin: '0 0 0.5rem 0', letterSpacing: '-0.02em' }}>
              Security Anomaly Review Portal
            </h1>
            <p style={{ color: '#cbd5e1', fontSize: '0.92rem', margin: 0, maxWidth: '650px', lineHeight: 1.5 }}>
              Centralized reviewer workbench for adjudicating flagged transactions combining Java Propositional Logic rules (60%) and Python Z-score statistical modeling (40%).
            </p>
          </div>

          <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
            <button
              onClick={loadData}
              disabled={isLoading}
              className="btn-secondary"
              style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', padding: '0.65rem 1rem', fontSize: '0.85rem' }}
            >
              <RefreshCw size={15} className={isLoading ? 'spin-icon' : ''} />
              <span>Refresh</span>
            </button>
            <button
              onClick={() => navigate('/review/cases')}
              className="btn-primary"
              style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', padding: '0.65rem 1.25rem', fontSize: '0.85rem' }}
            >
              <FolderSearch size={16} />
              <span>All Cases</span>
            </button>
          </div>
        </div>

        {errorMessage && (
          <div style={{
            backgroundColor: '#fef2f2',
            border: '1px solid #fecaca',
            color: '#b91c1c',
            padding: '1rem',
            borderRadius: '8px',
            marginBottom: '1.5rem',
            fontSize: '0.88rem'
          }}>
            {errorMessage}
          </div>
        )}

        {/* Metrics Row */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
          gap: '1rem',
          marginBottom: '2rem'
        }}>
          {/* Total Cases */}
          <div className="metric-card" style={{ padding: '1.25rem', backgroundColor: '#ffffff', borderRadius: '10px', border: '1px solid #e2e8f0', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
              <span style={{ fontSize: '0.82rem', color: '#64748b', fontWeight: 600 }}>TOTAL CASES</span>
              <FolderSearch size={18} color="#003366" />
            </div>
            <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#0f172a' }}>
              {stats?.totalCases ?? cases.length}
            </div>
            <div style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '4px' }}>In review pipeline</div>
          </div>

          {/* New / Action Required */}
          <div className="metric-card" style={{ padding: '1.25rem', backgroundColor: '#ffffff', borderRadius: '10px', border: '1px solid #fecaca', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
              <span style={{ fontSize: '0.82rem', color: '#dc2626', fontWeight: 600 }}>NEW CASES</span>
              <AlertTriangle size={18} color="#dc2626" />
            </div>
            <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#dc2626' }}>
              {stats?.newCases ?? 0}
            </div>
            <div style={{ fontSize: '0.75rem', color: '#dc2626', marginTop: '4px' }}>Awaiting initial triage</div>
          </div>

          {/* In Review */}
          <div className="metric-card" style={{ padding: '1.25rem', backgroundColor: '#ffffff', borderRadius: '10px', border: '1px solid #bfdbfe', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
              <span style={{ fontSize: '0.82rem', color: '#2563eb', fontWeight: 600 }}>IN REVIEW</span>
              <Clock size={18} color="#2563eb" />
            </div>
            <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#2563eb' }}>
              {stats?.inReviewCases ?? 0}
            </div>
            <div style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '4px' }}>Active investigation</div>
          </div>

          {/* Escalated */}
          <div className="metric-card" style={{ padding: '1.25rem', backgroundColor: '#ffffff', borderRadius: '10px', border: '1px solid #e9d5ff', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
              <span style={{ fontSize: '0.82rem', color: '#7c3aed', fontWeight: 600 }}>ESCALATED</span>
              <ShieldAlert size={18} color="#7c3aed" />
            </div>
            <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#7c3aed' }}>
              {stats?.escalatedCases ?? 0}
            </div>
            <div style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '4px' }}>Senior review required</div>
          </div>

          {/* High Risk */}
          <div className="metric-card" style={{ padding: '1.25rem', backgroundColor: '#ffffff', borderRadius: '10px', border: '1px solid #fecaca', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
              <span style={{ fontSize: '0.82rem', color: '#991b1b', fontWeight: 600 }}>HIGH RISK</span>
              <Activity size={18} color="#991b1b" />
            </div>
            <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#991b1b' }}>
              {stats?.highRiskCases ?? 0}
            </div>
            <div style={{ fontSize: '0.75rem', color: '#991b1b', marginTop: '4px' }}>Risk score 80–100</div>
          </div>

          {/* Resolved */}
          <div className="metric-card" style={{ padding: '1.25rem', backgroundColor: '#ffffff', borderRadius: '10px', border: '1px solid #bbf7d0', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
              <span style={{ fontSize: '0.82rem', color: '#16a34a', fontWeight: 600 }}>RESOLVED</span>
              <CheckCircle size={18} color="#16a34a" />
            </div>
            <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#16a34a' }}>
              {stats?.resolvedCases ?? 0}
            </div>
            <div style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '4px' }}>Disposition finalized</div>
          </div>
        </div>

        {/* System Architecture Callout */}
        <div style={{
          backgroundColor: '#f8fafc',
          border: '1px solid #e2e8f0',
          borderRadius: '10px',
          padding: '1.25rem 1.5rem',
          marginBottom: '2rem',
          display: 'flex',
          gap: '1.5rem',
          alignItems: 'center',
          flexWrap: 'wrap'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flex: 1, minWidth: '260px' }}>
            <Cpu size={24} color="#003366" />
            <div>
              <div style={{ fontSize: '0.88rem', fontWeight: 700, color: '#0f172a' }}>Java Propositional Rules (60%)</div>
              <div style={{ fontSize: '0.78rem', color: '#64748b' }}>DMGT logic rules (High Amount, Velocity burst, Late Night, Geo)</div>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flex: 1, minWidth: '260px' }}>
            <Activity size={24} color="#0284c7" />
            <div>
              <div style={{ fontSize: '0.88rem', fontWeight: 700, color: '#0f172a' }}>Python Z-Score Outlier (40%)</div>
              <div style={{ fontSize: '0.78rem', color: '#64748b' }}>Statistical anomaly formula z = (x - mean) / standard_deviation</div>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flex: 1, minWidth: '260px' }}>
            <FileCheck2 size={24} color="#16a34a" />
            <div>
              <div style={{ fontSize: '0.88rem', fontWeight: 700, color: '#0f172a' }}>Human-in-the-Loop Review</div>
              <div style={{ fontSize: '0.78rem', color: '#64748b' }}>Non-accusatory disposition (Legitimate / Suspicious Confirmed)</div>
            </div>
          </div>
        </div>

        {/* Priority Cases Workbench */}
        <div style={{
          backgroundColor: '#ffffff',
          borderRadius: '10px',
          border: '1px solid #e2e8f0',
          padding: '1.5rem',
          boxShadow: '0 1px 3px rgba(0,0,0,0.05)'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem', flexWrap: 'wrap', gap: '1rem' }}>
            <div>
              <h2 style={{ fontSize: '1.15rem', fontWeight: 700, color: '#0f172a', margin: 0 }}>
                Recent Investigation Cases
              </h2>
              <p style={{ fontSize: '0.82rem', color: '#64748b', margin: '4px 0 0 0' }}>
                Transactions flagged with composite risk score ≥ 60 for analyst adjudications
              </p>
            </div>
            <Link
              to="/review/cases"
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '6px',
                fontSize: '0.85rem',
                fontWeight: 600,
                color: '#003366',
                textDecoration: 'none'
              }}
            >
              <span>View All Investigation Cases</span>
              <ArrowRight size={14} />
            </Link>
          </div>

          {isLoading ? (
            <div style={{ textAlign: 'center', padding: '3rem 0', color: '#64748b' }}>
              <div className="spinner" style={{ margin: '0 auto 1rem auto' }}></div>
              <div>Loading review cases...</div>
            </div>
          ) : cases.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '3rem 0', color: '#64748b' }}>
              <CheckCircle size={36} color="#16a34a" style={{ marginBottom: '0.75rem' }} />
              <div style={{ fontWeight: 600, color: '#0f172a' }}>No Pending Review Cases</div>
              <div style={{ fontSize: '0.85rem' }}>All flagged transactions have been reviewed and resolved.</div>
            </div>
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.88rem' }}>
                <thead>
                  <tr style={{ backgroundColor: '#f8fafc', borderBottom: '2px solid #e2e8f0', textAlign: 'left', color: '#475569', fontSize: '0.78rem', textTransform: 'uppercase' }}>
                    <th style={{ padding: '0.75rem 1rem' }}>Case ID</th>
                    <th style={{ padding: '0.75rem 1rem' }}>Transaction</th>
                    <th style={{ padding: '0.75rem 1rem' }}>Merchant / Recipient</th>
                    <th style={{ padding: '0.75rem 1rem' }}>Amount</th>
                    <th style={{ padding: '0.75rem 1rem' }}>Risk Score</th>
                    <th style={{ padding: '0.75rem 1rem' }}>Status</th>
                    <th style={{ padding: '0.75rem 1rem' }}>Reviewer</th>
                    <th style={{ padding: '0.75rem 1rem', textAlign: 'right' }}>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {cases.slice(0, 5).map((c) => {
                    const statusMeta = getStatusBadge(c.status);
                    return (
                      <tr key={c.caseId} style={{ borderBottom: '1px solid #f1f5f9' }}>
                        <td style={{ padding: '1rem', fontWeight: 600, color: '#003366' }}>
                          <Link to={`/review/cases/${c.caseId}`} style={{ color: '#003366', textDecoration: 'none', fontWeight: 700 }}>
                            {c.caseId}
                          </Link>
                        </td>
                        <td style={{ padding: '1rem', fontFamily: 'monospace', fontSize: '0.82rem', color: '#334155' }}>
                          {c.transactionId}
                        </td>
                        <td style={{ padding: '1rem', color: '#0f172a', fontWeight: 500 }}>
                          {c.merchant || 'Commercial Merchant'}
                        </td>
                        <td style={{ padding: '1rem', fontWeight: 700, color: '#0f172a' }}>
                          {c.amount !== undefined ? `₹${c.amount.toLocaleString('en-IN')}` : '—'}
                        </td>
                        <td style={{ padding: '1rem' }}>
                          {getRiskBadge(c.riskLevel, c.riskScore)}
                        </td>
                        <td style={{ padding: '1rem' }}>
                          <span style={{
                            backgroundColor: statusMeta.bg,
                            color: statusMeta.text,
                            border: `1px solid ${statusMeta.border}`,
                            padding: '3px 8px',
                            borderRadius: '12px',
                            fontSize: '0.72rem',
                            fontWeight: 700
                          }}>
                            {statusMeta.label}
                          </span>
                        </td>
                        <td style={{ padding: '1rem', fontSize: '0.82rem', color: '#64748b' }}>
                          {c.assignedReviewerId || <span style={{ color: '#dc2626', fontStyle: 'italic' }}>Unassigned</span>}
                        </td>
                        <td style={{ padding: '1rem', textAlign: 'right' }}>
                          <Link
                            to={`/review/cases/${c.caseId}`}
                            className="btn-primary"
                            style={{
                              padding: '0.4rem 0.85rem',
                              fontSize: '0.78rem',
                              textDecoration: 'none',
                              display: 'inline-flex',
                              alignItems: 'center',
                              gap: '4px'
                            }}
                          >
                            <span>Open</span>
                            <ExternalLink size={12} />
                          </Link>
                        </td>
                      </tr>
                    );
                  })}
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
