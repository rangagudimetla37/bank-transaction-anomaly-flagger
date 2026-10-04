import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../services/api';
import type { ReviewCaseSummary, RiskLevelType } from '../services/api';
import { Header } from '../components/Header';
import { Footer } from '../components/Footer';
import {
  FolderSearch,
  Search,
  RefreshCw,
  ChevronRight
} from 'lucide-react';

export const ReviewCasesPage: React.FC = () => {
  const [cases, setCases] = useState<ReviewCaseSummary[]>([]);
  const [filteredCases, setFilteredCases] = useState<ReviewCaseSummary[]>([]);
  const [activeTab, setActiveTab] = useState<string>('ALL');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [errorMessage, setErrorMessage] = useState<string>('');

  const loadCases = async () => {
    setIsLoading(true);
    setErrorMessage('');
    try {
      const data = await api.getReviewCases();
      setCases(data || []);
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to load review cases.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadCases();
  }, []);

  useEffect(() => {
    let result = [...cases];

    // Filter by Tab
    if (activeTab === 'NEW') {
      result = result.filter((c) => c.status === 'NEW');
    } else if (activeTab === 'IN_REVIEW') {
      result = result.filter((c) => c.status === 'IN_REVIEW');
    } else if (activeTab === 'ESCALATED') {
      result = result.filter((c) => c.status === 'ESCALATED');
    } else if (activeTab === 'RESOLVED') {
      result = result.filter((c) => c.status === 'RESOLVED');
    } else if (activeTab === 'HIGH_RISK') {
      result = result.filter((c) => c.riskLevel === 'HIGH_RISK');
    }

    // Filter by Search
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase().trim();
      result = result.filter(
        (c) =>
          c.caseId.toLowerCase().includes(q) ||
          c.transactionId.toLowerCase().includes(q) ||
          (c.alertId && c.alertId.toLowerCase().includes(q)) ||
          (c.merchant && c.merchant.toLowerCase().includes(q)) ||
          (c.assignedReviewerId && c.assignedReviewerId.toLowerCase().includes(q))
      );
    }

    setFilteredCases(result);
  }, [cases, activeTab, searchQuery]);

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

  const getDecisionBadge = (decision?: string) => {
    if (!decision) return null;
    let bg = '#f1f5f9';
    let text = '#475569';
    let label = decision;

    if (decision === 'LEGITIMATE') {
      bg = '#f0fdf4';
      text = '#15803d';
      label = 'Legitimate';
    } else if (decision === 'SUSPICIOUS_CONFIRMED') {
      bg = '#fff7ed';
      text = '#c2410c';
      label = 'Suspicious Confirmed';
    } else if (decision === 'REQUIRES_FURTHER_REVIEW') {
      bg = '#faf5ff';
      text = '#7c3aed';
      label = 'Further Review';
    }

    return (
      <span style={{
        backgroundColor: bg,
        color: text,
        fontSize: '0.72rem',
        fontWeight: 700,
        padding: '2px 6px',
        borderRadius: '4px',
        border: '1px solid rgba(0,0,0,0.08)'
      }}>
        {label}
      </span>
    );
  };

  const counts = {
    all: cases.length,
    new: cases.filter((c) => c.status === 'NEW').length,
    inReview: cases.filter((c) => c.status === 'IN_REVIEW').length,
    escalated: cases.filter((c) => c.status === 'ESCALATED').length,
    resolved: cases.filter((c) => c.status === 'RESOLVED').length,
    highRisk: cases.filter((c) => c.riskLevel === 'HIGH_RISK').length,
  };

  return (
    <div className="banking-page-layout">
      <Header />

      <main className="dashboard-content" style={{ maxWidth: '1200px', margin: '0 auto', padding: '2rem 1.5rem' }}>
        {/* Breadcrumb Navigation */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.82rem', color: '#64748b', marginBottom: '1rem' }}>
          <Link to="/review" style={{ color: '#003366', textDecoration: 'none', fontWeight: 600 }}>Reviewer Portal</Link>
          <ChevronRight size={14} />
          <span style={{ color: '#0f172a', fontWeight: 600 }}>Investigation Cases</span>
        </div>

        {/* Page Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
          <div>
            <h1 style={{ fontSize: '1.5rem', fontWeight: 800, color: '#003366', margin: 0 }}>
              Transaction Investigation Cases
            </h1>
            <p style={{ fontSize: '0.85rem', color: '#64748b', margin: '4px 0 0 0' }}>
              Queue of flagged suspicious & high-risk transactions requiring analyst review and disposition
            </p>
          </div>

          <button
            onClick={loadCases}
            disabled={isLoading}
            className="btn-secondary"
            style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', padding: '0.55rem 0.95rem', fontSize: '0.85rem' }}
          >
            <RefreshCw size={15} className={isLoading ? 'spin-icon' : ''} />
            <span>Refresh</span>
          </button>
        </div>

        {errorMessage && (
          <div style={{ backgroundColor: '#fef2f2', border: '1px solid #fecaca', color: '#b91c1c', padding: '1rem', borderRadius: '8px', marginBottom: '1.5rem', fontSize: '0.88rem' }}>
            {errorMessage}
          </div>
        )}

        {/* Filters and Search Bar */}
        <div style={{ backgroundColor: '#ffffff', border: '1px solid #e2e8f0', borderRadius: '10px', padding: '1rem', marginBottom: '1.5rem', boxShadow: '0 1px 3px rgba(0,0,0,0.04)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
            {/* Filter Tabs */}
            <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
              <button
                onClick={() => setActiveTab('ALL')}
                style={{
                  padding: '6px 12px',
                  borderRadius: '6px',
                  fontSize: '0.82rem',
                  fontWeight: 600,
                  border: 'none',
                  cursor: 'pointer',
                  backgroundColor: activeTab === 'ALL' ? '#003366' : '#f1f5f9',
                  color: activeTab === 'ALL' ? '#ffffff' : '#475569'
                }}
              >
                All Cases ({counts.all})
              </button>

              <button
                onClick={() => setActiveTab('NEW')}
                style={{
                  padding: '6px 12px',
                  borderRadius: '6px',
                  fontSize: '0.82rem',
                  fontWeight: 600,
                  border: 'none',
                  cursor: 'pointer',
                  backgroundColor: activeTab === 'NEW' ? '#dc2626' : '#fef2f2',
                  color: activeTab === 'NEW' ? '#ffffff' : '#dc2626'
                }}
              >
                New ({counts.new})
              </button>

              <button
                onClick={() => setActiveTab('IN_REVIEW')}
                style={{
                  padding: '6px 12px',
                  borderRadius: '6px',
                  fontSize: '0.82rem',
                  fontWeight: 600,
                  border: 'none',
                  cursor: 'pointer',
                  backgroundColor: activeTab === 'IN_REVIEW' ? '#2563eb' : '#eff6ff',
                  color: activeTab === 'IN_REVIEW' ? '#ffffff' : '#2563eb'
                }}
              >
                In Review ({counts.inReview})
              </button>

              <button
                onClick={() => setActiveTab('ESCALATED')}
                style={{
                  padding: '6px 12px',
                  borderRadius: '6px',
                  fontSize: '0.82rem',
                  fontWeight: 600,
                  border: 'none',
                  cursor: 'pointer',
                  backgroundColor: activeTab === 'ESCALATED' ? '#7c3aed' : '#faf5ff',
                  color: activeTab === 'ESCALATED' ? '#ffffff' : '#7c3aed'
                }}
              >
                Escalated ({counts.escalated})
              </button>

              <button
                onClick={() => setActiveTab('RESOLVED')}
                style={{
                  padding: '6px 12px',
                  borderRadius: '6px',
                  fontSize: '0.82rem',
                  fontWeight: 600,
                  border: 'none',
                  cursor: 'pointer',
                  backgroundColor: activeTab === 'RESOLVED' ? '#16a34a' : '#f0fdf4',
                  color: activeTab === 'RESOLVED' ? '#ffffff' : '#16a34a'
                }}
              >
                Resolved ({counts.resolved})
              </button>

              <button
                onClick={() => setActiveTab('HIGH_RISK')}
                style={{
                  padding: '6px 12px',
                  borderRadius: '6px',
                  fontSize: '0.82rem',
                  fontWeight: 600,
                  border: 'none',
                  cursor: 'pointer',
                  backgroundColor: activeTab === 'HIGH_RISK' ? '#991b1b' : '#fff1f2',
                  color: activeTab === 'HIGH_RISK' ? '#ffffff' : '#991b1b'
                }}
              >
                High Risk ({counts.highRisk})
              </button>
            </div>

            {/* Search Input */}
            <div style={{ position: 'relative', minWidth: '260px' }}>
              <Search size={16} color="#94a3b8" style={{ position: 'absolute', left: '10px', top: '50%', transform: 'translateY(-50%)' }} />
              <input
                type="text"
                placeholder="Search case, txn, or merchant..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                style={{
                  width: '100%',
                  padding: '7px 10px 7px 32px',
                  fontSize: '0.85rem',
                  borderRadius: '6px',
                  border: '1px solid #cbd5e1',
                  outline: 'none',
                  boxSizing: 'border-box'
                }}
              />
            </div>
          </div>
        </div>

        {/* Cases Table */}
        <div style={{ backgroundColor: '#ffffff', borderRadius: '10px', border: '1px solid #e2e8f0', boxShadow: '0 1px 3px rgba(0,0,0,0.05)', overflow: 'hidden' }}>
          {isLoading ? (
            <div style={{ textAlign: 'center', padding: '3.5rem 0', color: '#64748b' }}>
              <div className="spinner" style={{ margin: '0 auto 1rem auto' }}></div>
              <div>Loading cases...</div>
            </div>
          ) : filteredCases.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '3.5rem 0', color: '#64748b' }}>
              <FolderSearch size={40} color="#cbd5e1" style={{ marginBottom: '0.75rem' }} />
              <div style={{ fontWeight: 600, color: '#0f172a' }}>No Cases Found</div>
              <div style={{ fontSize: '0.85rem' }}>No investigation cases match the active tab or search criteria.</div>
            </div>
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.88rem' }}>
                <thead>
                  <tr style={{ backgroundColor: '#f8fafc', borderBottom: '2px solid #e2e8f0', textAlign: 'left', color: '#475569', fontSize: '0.78rem', textTransform: 'uppercase' }}>
                    <th style={{ padding: '0.85rem 1rem' }}>Case ID</th>
                    <th style={{ padding: '0.85rem 1rem' }}>Transaction ID</th>
                    <th style={{ padding: '0.85rem 1rem' }}>Counterparty</th>
                    <th style={{ padding: '0.85rem 1rem' }}>Amount</th>
                    <th style={{ padding: '0.85rem 1rem' }}>Risk Score</th>
                    <th style={{ padding: '0.85rem 1rem' }}>Workflow Status</th>
                    <th style={{ padding: '0.85rem 1rem' }}>Assigned To</th>
                    <th style={{ padding: '0.85rem 1rem' }}>Decision</th>
                    <th style={{ padding: '0.85rem 1rem', textAlign: 'right' }}>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredCases.map((c) => {
                    const statusMeta = getStatusBadge(c.status);
                    return (
                      <tr key={c.caseId} style={{ borderBottom: '1px solid #f1f5f9' }}>
                        <td style={{ padding: '0.9rem 1rem', fontWeight: 700 }}>
                          <Link to={`/review/cases/${c.caseId}`} style={{ color: '#003366', textDecoration: 'none' }}>
                            {c.caseId}
                          </Link>
                        </td>
                        <td style={{ padding: '0.9rem 1rem', fontFamily: 'monospace', fontSize: '0.82rem', color: '#334155' }}>
                          {c.transactionId}
                        </td>
                        <td style={{ padding: '0.9rem 1rem', color: '#0f172a', fontWeight: 500 }}>
                          {c.merchant || 'Commercial Merchant'}
                        </td>
                        <td style={{ padding: '0.9rem 1rem', fontWeight: 700, color: '#0f172a' }}>
                          {c.amount !== undefined ? `₹${c.amount.toLocaleString('en-IN')}` : '—'}
                        </td>
                        <td style={{ padding: '0.9rem 1rem' }}>
                          {getRiskBadge(c.riskLevel, c.riskScore)}
                        </td>
                        <td style={{ padding: '0.9rem 1rem' }}>
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
                        <td style={{ padding: '0.9rem 1rem', fontSize: '0.82rem', color: '#64748b' }}>
                          {c.assignedReviewerId || <span style={{ color: '#dc2626', fontStyle: 'italic' }}>Unassigned</span>}
                        </td>
                        <td style={{ padding: '0.9rem 1rem' }}>
                          {getDecisionBadge(c.decision) || <span style={{ color: '#94a3b8', fontSize: '0.78rem' }}>Pending</span>}
                        </td>
                        <td style={{ padding: '0.9rem 1rem', textAlign: 'right' }}>
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
                            <span>Investigate</span>
                            <ChevronRight size={14} />
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
