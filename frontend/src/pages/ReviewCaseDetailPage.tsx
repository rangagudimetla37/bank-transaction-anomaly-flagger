import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import type { ReviewCaseDetail, RiskLevelType } from '../services/api';
import { Header } from '../components/Header';
import { Footer } from '../components/Footer';
import {
  ShieldAlert,
  ArrowLeft,
  RefreshCw,
  Cpu,
  Activity,
  FileCheck2,
  CheckCircle,
  AlertTriangle,
  Save,
  MapPin,
  Calendar,
  CreditCard,
  History,
  ChevronRight,
  Share2
} from 'lucide-react';

export const ReviewCaseDetailPage: React.FC = () => {
  const { caseId } = useParams<{ caseId: string }>();
  const navigate = useNavigate();

  const [detail, setDetail] = useState<ReviewCaseDetail | null>(null);
  const [reviewerNotes, setReviewerNotes] = useState<string>('');
  const [selectedDecision, setSelectedDecision] = useState<string>('');
  const [decisionNotes, setDecisionNotes] = useState<string>('');
  const [escalationReason, setEscalationReason] = useState<string>('');
  const [isEscalating, setIsEscalating] = useState<boolean>(false);

  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isSavingNotes, setIsSavingNotes] = useState<boolean>(false);
  const [isSubmittingDecision, setIsSubmittingDecision] = useState<boolean>(false);
  const [feedbackMessage, setFeedbackMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [errorMessage, setErrorMessage] = useState<string>('');

  const loadCase = async () => {
    if (!caseId) return;
    setIsLoading(true);
    setErrorMessage('');
    try {
      const data = await api.getReviewCaseDetail(caseId);
      setDetail(data);
      setReviewerNotes(data.reviewerNotes || '');
      if (data.decision) {
        setSelectedDecision(data.decision);
      }
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to load review case details.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadCase();
  }, [caseId]);

  const handleSaveNotes = async () => {
    if (!caseId) return;
    setIsSavingNotes(true);
    setFeedbackMessage(null);
    try {
      const res = await api.updateReviewNotes(caseId, reviewerNotes);
      if (res.success) {
        setFeedbackMessage({ type: 'success', text: 'Investigation notes saved and recorded to audit trail.' });
        loadCase();
      } else {
        setFeedbackMessage({ type: 'error', text: res.message || 'Failed to save notes.' });
      }
    } catch (err: any) {
      setFeedbackMessage({ type: 'error', text: err.message || 'Error updating notes.' });
    } finally {
      setIsSavingNotes(false);
    }
  };

  const handleEscalateCase = async () => {
    if (!caseId) return;
    setIsSavingNotes(true);
    setFeedbackMessage(null);
    try {
      const res = await api.escalateReviewCase(caseId, escalationReason);
      if (res.success) {
        setFeedbackMessage({ type: 'success', text: 'Case escalated to senior review team successfully.' });
        setIsEscalating(false);
        setEscalationReason('');
        loadCase();
      } else {
        setFeedbackMessage({ type: 'error', text: res.message || 'Failed to escalate case.' });
      }
    } catch (err: any) {
      setFeedbackMessage({ type: 'error', text: err.message || 'Error escalating case.' });
    } finally {
      setIsSavingNotes(false);
    }
  };

  const handleSubmitDecision = async () => {
    if (!caseId || !selectedDecision) {
      setFeedbackMessage({ type: 'error', text: 'Please select an adjudication disposition first.' });
      return;
    }
    setIsSubmittingDecision(true);
    setFeedbackMessage(null);
    try {
      const res = await api.submitReviewDecision(caseId, selectedDecision, decisionNotes || reviewerNotes);
      if (res.success) {
        setFeedbackMessage({ type: 'success', text: `Case resolved successfully with disposition: ${selectedDecision.replace('_', ' ')}` });
        loadCase();
      } else {
        setFeedbackMessage({ type: 'error', text: res.message || 'Failed to submit decision.' });
      }
    } catch (err: any) {
      setFeedbackMessage({ type: 'error', text: err.message || 'Error submitting decision.' });
    } finally {
      setIsSubmittingDecision(false);
    }
  };

  const getRiskBadge = (level?: RiskLevelType, score?: number) => {
    if (!level) return null;
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
        padding: '4px 10px',
        borderRadius: '6px',
        fontSize: '0.82rem',
        fontWeight: 700,
        display: 'inline-flex',
        alignItems: 'center',
        gap: '6px'
      }}>
        <Activity size={14} />
        <span>RISK SCORE: {score ?? 0}/100 ({level.replace('_', ' ')})</span>
      </span>
    );
  };

  const getStatusBadge = (status?: string) => {
    if (!status) return null;
    let bg = '#f8fafc';
    let text = '#475569';
    let border = '#e2e8f0';

    if (status === 'NEW') {
      bg = '#fef2f2';
      text = '#dc2626';
      border = '#fecaca';
    } else if (status === 'IN_REVIEW') {
      bg = '#eff6ff';
      text = '#2563eb';
      border = '#bfdbfe';
    } else if (status === 'ESCALATED') {
      bg = '#faf5ff';
      text = '#7c3aed';
      border = '#e9d5ff';
    } else if (status === 'RESOLVED') {
      bg = '#f0fdf4';
      text = '#16a34a';
      border = '#bbf7d0';
    }

    return (
      <span style={{
        backgroundColor: bg,
        color: text,
        border: `1px solid ${border}`,
        padding: '4px 10px',
        borderRadius: '12px',
        fontSize: '0.78rem',
        fontWeight: 700
      }}>
        {status.replace('_', ' ')}
      </span>
    );
  };

  return (
    <div className="banking-page-layout">
      <Header />

      <main className="dashboard-content" style={{ maxWidth: '1200px', margin: '0 auto', padding: '2rem 1.5rem' }}>
        {/* Breadcrumb Navigation */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.82rem', color: '#64748b', marginBottom: '1rem' }}>
          <Link to="/review" style={{ color: '#003366', textDecoration: 'none', fontWeight: 600 }}>Reviewer Portal</Link>
          <ChevronRight size={14} />
          <Link to="/review/cases" style={{ color: '#003366', textDecoration: 'none', fontWeight: 600 }}>Cases</Link>
          <ChevronRight size={14} />
          <span style={{ color: '#0f172a', fontWeight: 600 }}>{caseId}</span>
        </div>

        {/* Back Link & Action Bar */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem', flexWrap: 'wrap', gap: '1rem' }}>
          <button
            onClick={() => navigate('/review/cases')}
            className="btn-secondary"
            style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', padding: '0.5rem 0.85rem', fontSize: '0.82rem' }}
          >
            <ArrowLeft size={14} />
            <span>Back to Case Queue</span>
          </button>

          <div style={{ display: 'flex', gap: '8px', alignItems: 'center', flexWrap: 'wrap' }}>
            {detail && (
              <Link
                to={`/transaction-network?transactionId=${detail.transactionId}`}
                className="btn-secondary"
                style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', padding: '0.5rem 0.85rem', fontSize: '0.82rem', textDecoration: 'none' }}
              >
                <Share2 size={14} />
                <span>View Transaction Network</span>
              </Link>
            )}

            <button
              onClick={loadCase}
              disabled={isLoading}
              className="btn-secondary"
              style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', padding: '0.5rem 0.85rem', fontSize: '0.82rem' }}
            >
              <RefreshCw size={14} className={isLoading ? 'spin-icon' : ''} />
              <span>Refresh Workbench</span>
            </button>
          </div>
        </div>

        {feedbackMessage && (
          <div style={{
            backgroundColor: feedbackMessage.type === 'success' ? '#f0fdf4' : '#fef2f2',
            border: `1px solid ${feedbackMessage.type === 'success' ? '#bbf7d0' : '#fecaca'}`,
            color: feedbackMessage.type === 'success' ? '#15803d' : '#b91c1c',
            padding: '1rem',
            borderRadius: '8px',
            marginBottom: '1.5rem',
            fontSize: '0.88rem',
            display: 'flex',
            alignItems: 'center',
            gap: '8px'
          }}>
            {feedbackMessage.type === 'success' ? <CheckCircle size={18} /> : <AlertTriangle size={18} />}
            <span>{feedbackMessage.text}</span>
          </div>
        )}

        {errorMessage && (
          <div style={{ backgroundColor: '#fef2f2', border: '1px solid #fecaca', color: '#b91c1c', padding: '1rem', borderRadius: '8px', marginBottom: '1.5rem', fontSize: '0.88rem' }}>
            {errorMessage}
          </div>
        )}

        {isLoading ? (
          <div style={{ textAlign: 'center', padding: '4rem 0', color: '#64748b' }}>
            <div className="spinner" style={{ margin: '0 auto 1rem auto' }}></div>
            <div>Loading investigation workbench...</div>
          </div>
        ) : !detail ? (
          <div style={{ textAlign: 'center', padding: '4rem 0', color: '#64748b' }}>
            <ShieldAlert size={40} color="#dc2626" style={{ marginBottom: '0.75rem' }} />
            <div style={{ fontWeight: 600, color: '#0f172a' }}>Case Not Found</div>
            <div>Unable to retrieve details for case {caseId}.</div>
          </div>
        ) : (
          <div>
            {/* Header Hero Banner */}
            <div style={{
              backgroundColor: '#002244',
              borderRadius: '12px',
              padding: '1.75rem 2rem',
              color: '#ffffff',
              marginBottom: '2rem',
              boxShadow: '0 4px 12px rgba(0, 34, 68, 0.15)'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem', marginBottom: '1rem' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap', marginBottom: '0.5rem' }}>
                    <h1 style={{ fontSize: '1.6rem', fontWeight: 800, margin: 0 }}>
                      Investigation Case: {detail.caseId}
                    </h1>
                    {getStatusBadge(detail.status)}
                    {getRiskBadge(detail.riskLevel, detail.riskScore)}
                  </div>
                  <div style={{ fontSize: '0.85rem', color: '#94a3b8' }}>
                    Associated Alert: <strong style={{ color: '#f8fafc' }}>{detail.alertId}</strong> | Transaction ID: <strong style={{ color: '#f8fafc' }}>{detail.transactionId}</strong>
                  </div>
                </div>

                <div style={{ textAlign: 'right', fontSize: '0.82rem', color: '#cbd5e1' }}>
                  <div>Assigned Reviewer: <strong style={{ color: '#ffffff' }}>{detail.assignedReviewerId || 'Unassigned'}</strong></div>
                  <div>Opened: <strong>{detail.createdAt}</strong></div>
                  {detail.resolvedAt && (
                    <div style={{ color: '#86efac' }}>Resolved: <strong>{detail.resolvedAt}</strong></div>
                  )}
                </div>
              </div>
            </div>

            {/* Workbench Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(350px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
              {/* Card 1: Transaction Information */}
              <div style={{ backgroundColor: '#ffffff', borderRadius: '10px', border: '1px solid #e2e8f0', padding: '1.5rem', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '1.25rem', borderBottom: '1px solid #f1f5f9', paddingBottom: '0.75rem' }}>
                  <CreditCard size={18} color="#003366" />
                  <h2 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#0f172a', margin: 0 }}>Transaction Information</h2>
                </div>

                {detail.transaction ? (
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', fontSize: '0.85rem' }}>
                    <div>
                      <span style={{ color: '#64748b', display: 'block', fontSize: '0.75rem', textTransform: 'uppercase' }}>Amount</span>
                      <strong style={{ fontSize: '1.25rem', color: '#0f172a' }}>
                        ₹{detail.transaction.amount?.toLocaleString('en-IN')}
                      </strong>
                    </div>

                    <div>
                      <span style={{ color: '#64748b', display: 'block', fontSize: '0.75rem', textTransform: 'uppercase' }}>Direction</span>
                      <span style={{
                        display: 'inline-block',
                        padding: '2px 8px',
                        borderRadius: '4px',
                        fontSize: '0.75rem',
                        fontWeight: 700,
                        backgroundColor: detail.transaction.transactionDirection === 'DEBIT' ? '#fef2f2' : '#f0fdf4',
                        color: detail.transaction.transactionDirection === 'DEBIT' ? '#dc2626' : '#16a34a'
                      }}>
                        {detail.transaction.transactionDirection}
                      </span>
                    </div>

                    <div>
                      <span style={{ color: '#64748b', display: 'block', fontSize: '0.75rem', textTransform: 'uppercase' }}>Counterparty / Merchant</span>
                      <strong style={{ color: '#0f172a' }}>{detail.transaction.recipient || 'N/A'}</strong>
                    </div>

                    <div>
                      <span style={{ color: '#64748b', display: 'block', fontSize: '0.75rem', textTransform: 'uppercase' }}>Category</span>
                      <span style={{ color: '#334155' }}>{detail.transaction.transactionType}</span>
                    </div>

                    <div>
                      <span style={{ color: '#64748b', display: 'block', fontSize: '0.75rem', textTransform: 'uppercase' }}>Location</span>
                      <span style={{ color: '#334155', display: 'flex', alignItems: 'center', gap: '4px' }}>
                        <MapPin size={13} color="#64748b" />
                        {detail.transaction.location}
                      </span>
                    </div>

                    <div>
                      <span style={{ color: '#64748b', display: 'block', fontSize: '0.75rem', textTransform: 'uppercase' }}>Timestamp</span>
                      <span style={{ color: '#334155', display: 'flex', alignItems: 'center', gap: '4px' }}>
                        <Calendar size={13} color="#64748b" />
                        {detail.transaction.timestamp}
                      </span>
                    </div>

                    <div style={{ gridColumn: 'span 2' }}>
                      <span style={{ color: '#64748b', display: 'block', fontSize: '0.75rem', textTransform: 'uppercase' }}>Description</span>
                      <span style={{ color: '#334155' }}>{detail.transaction.description}</span>
                    </div>
                  </div>
                ) : (
                  <div style={{ color: '#64748b', fontSize: '0.85rem' }}>Transaction metadata loaded from ledger.</div>
                )}
              </div>

              {/* Card 2: Composite Risk Diagnostics */}
              <div style={{ backgroundColor: '#ffffff', borderRadius: '10px', border: '1px solid #e2e8f0', padding: '1.5rem', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '1.25rem', borderBottom: '1px solid #f1f5f9', paddingBottom: '0.75rem' }}>
                  <Activity size={18} color="#003366" />
                  <h2 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#0f172a', margin: 0 }}>Analytical Risk Breakdown</h2>
                </div>

                {detail.riskAnalysis ? (
                  <div>
                    {/* Two Engine Pillars */}
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginBottom: '1rem' }}>
                      {/* Java Rule Score */}
                      <div style={{ backgroundColor: '#f8fafc', padding: '0.85rem', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#003366', fontSize: '0.78rem', fontWeight: 700 }}>
                          <Cpu size={14} />
                          <span>Java Propositional (60%)</span>
                        </div>
                        <div style={{ fontSize: '1.4rem', fontWeight: 800, color: '#0f172a', marginTop: '4px' }}>
                          {detail.riskAnalysis.ruleScore}/100
                        </div>
                        <div style={{ fontSize: '0.72rem', color: '#64748b', marginTop: '2px' }}>
                          Weighted: {Math.round(detail.riskAnalysis.ruleScore * 0.60)} pts
                        </div>
                      </div>

                      {/* Python Statistical Score */}
                      <div style={{ backgroundColor: '#f8fafc', padding: '0.85rem', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#0284c7', fontSize: '0.78rem', fontWeight: 700 }}>
                          <Activity size={14} />
                          <span>Python Z-Score (40%)</span>
                        </div>
                        <div style={{ fontSize: '1.4rem', fontWeight: 800, color: '#0f172a', marginTop: '4px' }}>
                          {detail.riskAnalysis.statisticalScore}/100
                        </div>
                        <div style={{ fontSize: '0.72rem', color: '#64748b', marginTop: '2px' }}>
                          Weighted: {Math.round(detail.riskAnalysis.statisticalScore * 0.40)} pts
                        </div>
                      </div>
                    </div>

                    {/* Statistical Parameters */}
                    <div style={{ backgroundColor: '#eff6ff', border: '1px solid #bfdbfe', borderRadius: '8px', padding: '0.75rem 1rem', marginBottom: '1rem', fontSize: '0.82rem' }}>
                      <div style={{ fontWeight: 700, color: '#1e40af', marginBottom: '4px' }}>Python Statistical Parameters:</div>
                      <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', color: '#1e3a8a' }}>
                        <div>Z-Score: <strong>{detail.riskAnalysis.zScore !== undefined ? detail.riskAnalysis.zScore.toFixed(2) : 'N/A'}</strong></div>
                        <div>Hist Mean: <strong>{detail.riskAnalysis.historicalMean !== undefined ? `₹${detail.riskAnalysis.historicalMean.toFixed(2)}` : 'N/A'}</strong></div>
                        <div>StdDev: <strong>{detail.riskAnalysis.historicalStdDev !== undefined ? `₹${detail.riskAnalysis.historicalStdDev.toFixed(2)}` : 'N/A'}</strong></div>
                      </div>
                    </div>

                    {/* Triggered Reasons */}
                    <div>
                      <div style={{ fontSize: '0.8rem', fontWeight: 700, color: '#475569', textTransform: 'uppercase', marginBottom: '6px' }}>Diagnostic Reasons:</div>
                      <ul style={{ margin: 0, paddingLeft: '1.25rem', fontSize: '0.82rem', color: '#334155' }}>
                        {detail.riskAnalysis.reasons?.map((r, i) => (
                          <li key={i} style={{ marginBottom: '4px' }}>{r}</li>
                        ))}
                      </ul>
                    </div>
                  </div>
                ) : (
                  <div style={{ color: '#64748b', fontSize: '0.85rem' }}>Composite risk score: {detail.riskScore}/100.</div>
                )}
              </div>

              {/* Card 3: Phase 7 Transaction Relationship & Graph Intelligence */}
              <div style={{ backgroundColor: '#ffffff', borderRadius: '10px', border: '1px solid #e2e8f0', padding: '1.5rem', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem', borderBottom: '1px solid #f1f5f9', paddingBottom: '0.75rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Share2 size={18} color="#003366" />
                    <h2 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#0f172a', margin: 0 }}>Network Graph Intelligence</h2>
                  </div>
                  {detail.graphAnalysis && (
                    <span style={{
                      backgroundColor: detail.graphAnalysis.graphScore >= 60 ? '#fff7ed' : '#eff6ff',
                      color: detail.graphAnalysis.graphScore >= 60 ? '#c2410c' : '#1e40af',
                      border: `1px solid ${detail.graphAnalysis.graphScore >= 60 ? '#fed7aa' : '#bfdbfe'}`,
                      padding: '3px 8px',
                      borderRadius: '4px',
                      fontSize: '0.75rem',
                      fontWeight: 700
                    }}>
                      GRAPH: {detail.graphAnalysis.graphScore}/100 ({detail.graphAnalysis.graphRiskLevel.replace('_', ' ')})
                    </span>
                  )}
                </div>

                {detail.graphAnalysis ? (
                  <div>
                    {/* Key Indicators Badges */}
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginBottom: '1rem' }}>
                      <div style={{
                        padding: '0.75rem',
                        borderRadius: '8px',
                        backgroundColor: detail.graphAnalysis.newRecipient ? '#fff7ed' : '#f8fafc',
                        border: `1px solid ${detail.graphAnalysis.newRecipient ? '#fed7aa' : '#e2e8f0'}`
                      }}>
                        <div style={{ fontSize: '0.72rem', color: '#64748b', textTransform: 'uppercase', fontWeight: 700 }}>Recipient Status</div>
                        <div style={{
                          fontWeight: 700,
                          fontSize: '0.85rem',
                          color: detail.graphAnalysis.newRecipient ? '#c2410c' : '#15803d',
                          marginTop: '3px'
                        }}>
                          {detail.graphAnalysis.newRecipient ? 'New Counterparty (+25)' : 'Known Recipient (0)'}
                        </div>
                        <div style={{ fontSize: '0.72rem', color: '#64748b', marginTop: '2px' }}>
                          {detail.graphAnalysis.recipientTransactionCount} prior transaction(s)
                        </div>
                      </div>

                      <div style={{
                        padding: '0.75rem',
                        borderRadius: '8px',
                        backgroundColor: detail.graphAnalysis.sharedRecipient ? '#fef2f2' : '#f8fafc',
                        border: `1px solid ${detail.graphAnalysis.sharedRecipient ? '#fecaca' : '#e2e8f0'}`
                      }}>
                        <div style={{ fontSize: '0.72rem', color: '#64748b', textTransform: 'uppercase', fontWeight: 700 }}>Multi-Account Entity</div>
                        <div style={{
                          fontWeight: 700,
                          fontSize: '0.85rem',
                          color: detail.graphAnalysis.sharedRecipient ? '#b91c1c' : '#15803d',
                          marginTop: '3px'
                        }}>
                          {detail.graphAnalysis.sharedRecipient ? 'Shared Counterparty (+20)' : 'Isolated to Account (0)'}
                        </div>
                        <div style={{ fontSize: '0.72rem', color: '#64748b', marginTop: '2px' }}>
                          {detail.graphAnalysis.sharedRecipient ? 'Linked to multiple accounts' : 'Single customer history'}
                        </div>
                      </div>
                    </div>

                    {/* Network Breadth Stats */}
                    <div style={{ backgroundColor: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '8px', padding: '0.75rem 1rem', marginBottom: '1rem', fontSize: '0.82rem' }}>
                      <div style={{ fontWeight: 700, color: '#003366', marginBottom: '4px' }}>Customer Network Breadth:</div>
                      <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', color: '#334155' }}>
                        <div>Recipients: <strong>{detail.graphAnalysis.uniqueRecipients}</strong></div>
                        <div>Locations: <strong>{detail.graphAnalysis.uniqueLocations}</strong></div>
                        <div>Ledger Txns: <strong>{detail.graphAnalysis.totalAccountTransactions}</strong></div>
                      </div>
                    </div>

                    {/* Graph Diagnostic Reasons */}
                    {detail.graphAnalysis.reasons && detail.graphAnalysis.reasons.length > 0 && (
                      <div style={{ marginBottom: '1rem' }}>
                        <div style={{ fontSize: '0.8rem', fontWeight: 700, color: '#475569', textTransform: 'uppercase', marginBottom: '6px' }}>Network Reasons:</div>
                        <ul style={{ margin: 0, paddingLeft: '1.25rem', fontSize: '0.82rem', color: '#334155' }}>
                          {detail.graphAnalysis.reasons.map((r, i) => (
                            <li key={i} style={{ marginBottom: '3px' }}>{r}</li>
                          ))}
                        </ul>
                      </div>
                    )}

                    {/* Link to visualizer */}
                    <Link
                      to={`/transaction-network?transactionId=${detail.transactionId}`}
                      className="btn-secondary"
                      style={{
                        display: 'inline-flex',
                        alignItems: 'center',
                        gap: '6px',
                        width: '100%',
                        justifyContent: 'center',
                        padding: '0.55rem',
                        fontSize: '0.82rem',
                        textDecoration: 'none'
                      }}
                    >
                      <Share2 size={14} />
                      <span>Open Interactive Graph Visualization</span>
                    </Link>
                  </div>
                ) : (
                  <div style={{ color: '#64748b', fontSize: '0.85rem' }}>
                    Graph analysis data unavailable for this transaction.
                  </div>
                )}
              </div>
            </div>

            {/* Reviewer Action Center (Notes & Decision) */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(350px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
              {/* Working Notes */}
              <div style={{ backgroundColor: '#ffffff', borderRadius: '10px', border: '1px solid #e2e8f0', padding: '1.5rem', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem', borderBottom: '1px solid #f1f5f9', paddingBottom: '0.75rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Save size={18} color="#003366" />
                    <h2 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#0f172a', margin: 0 }}>Reviewer Investigation Notes</h2>
                  </div>
                  <span style={{ fontSize: '0.75rem', color: '#64748b' }}>Internal & Confidential</span>
                </div>

                <p style={{ fontSize: '0.82rem', color: '#64748b', margin: '0 0 0.75rem 0' }}>
                  Document findings from customer contact, merchant inquiries, device verification, or travel checks.
                </p>

                <textarea
                  rows={6}
                  value={reviewerNotes}
                  onChange={(e) => setReviewerNotes(e.target.value)}
                  placeholder="Enter detailed reviewer notes (e.g., Contacted customer on registered mobile; confirmed outstation travel...)"
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    fontSize: '0.85rem',
                    borderRadius: '8px',
                    border: '1px solid #cbd5e1',
                    outline: 'none',
                    boxSizing: 'border-box',
                    fontFamily: 'inherit',
                    resize: 'vertical',
                    marginBottom: '1rem'
                  }}
                />

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.5rem' }}>
                  <button
                    onClick={handleSaveNotes}
                    disabled={isSavingNotes}
                    className="btn-primary"
                    style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', padding: '0.55rem 1.15rem', fontSize: '0.85rem' }}
                  >
                    <Save size={15} />
                    <span>{isSavingNotes ? 'Saving...' : 'Save Notes'}</span>
                  </button>

                  {!isEscalating ? (
                    <button
                      onClick={() => setIsEscalating(true)}
                      style={{
                        backgroundColor: '#faf5ff',
                        color: '#7c3aed',
                        border: '1px solid #e9d5ff',
                        padding: '0.55rem 1rem',
                        borderRadius: '6px',
                        fontSize: '0.82rem',
                        fontWeight: 600,
                        cursor: 'pointer'
                      }}
                    >
                      Escalate Case
                    </button>
                  ) : null}
                </div>

                {/* Escalation Modal/Panel */}
                {isEscalating && (
                  <div style={{ marginTop: '1rem', padding: '1rem', backgroundColor: '#faf5ff', border: '1px solid #e9d5ff', borderRadius: '8px' }}>
                    <div style={{ fontWeight: 700, color: '#7c3aed', fontSize: '0.85rem', marginBottom: '6px' }}>Escalate to Senior Analyst</div>
                    <input
                      type="text"
                      placeholder="Reason for escalation (e.g. High amount bullion purchase)..."
                      value={escalationReason}
                      onChange={(e) => setEscalationReason(e.target.value)}
                      style={{
                        width: '100%',
                        padding: '6px 10px',
                        fontSize: '0.82rem',
                        borderRadius: '6px',
                        border: '1px solid #cbd5e1',
                        marginBottom: '8px',
                        boxSizing: 'border-box'
                      }}
                    />
                    <div style={{ display: 'flex', gap: '8px' }}>
                      <button
                        onClick={handleEscalateCase}
                        disabled={isSavingNotes}
                        style={{ backgroundColor: '#7c3aed', color: '#ffffff', border: 'none', borderRadius: '4px', padding: '5px 12px', fontSize: '0.8rem', fontWeight: 600, cursor: 'pointer' }}
                      >
                        Confirm Escalation
                      </button>
                      <button
                        onClick={() => setIsEscalating(false)}
                        style={{ backgroundColor: 'transparent', color: '#64748b', border: '1px solid #cbd5e1', borderRadius: '4px', padding: '5px 12px', fontSize: '0.8rem', cursor: 'pointer' }}
                      >
                        Cancel
                      </button>
                    </div>
                  </div>
                )}
              </div>

              {/* Adjudication Decision Panel */}
              <div style={{ backgroundColor: '#ffffff', borderRadius: '10px', border: '1px solid #e2e8f0', padding: '1.5rem', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '1rem', borderBottom: '1px solid #f1f5f9', paddingBottom: '0.75rem' }}>
                  <FileCheck2 size={18} color="#003366" />
                  <h2 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#0f172a', margin: 0 }}>Case Disposition & Resolution</h2>
                </div>

                <p style={{ fontSize: '0.82rem', color: '#64748b', margin: '0 0 1rem 0' }}>
                  Select the analytical disposition. The case will be marked as RESOLVED and permanently recorded.
                </p>

                {/* Option 1: LEGITIMATE */}
                <label style={{
                  display: 'flex',
                  alignItems: 'flex-start',
                  gap: '10px',
                  padding: '0.85rem',
                  borderRadius: '8px',
                  border: `1px solid ${selectedDecision === 'LEGITIMATE' ? '#16a34a' : '#e2e8f0'}`,
                  backgroundColor: selectedDecision === 'LEGITIMATE' ? '#f0fdf4' : '#ffffff',
                  cursor: 'pointer',
                  marginBottom: '0.75rem'
                }}>
                  <input
                    type="radio"
                    name="decision"
                    value="LEGITIMATE"
                    checked={selectedDecision === 'LEGITIMATE'}
                    onChange={(e) => setSelectedDecision(e.target.value)}
                    style={{ marginTop: '3px' }}
                  />
                  <div>
                    <div style={{ fontWeight: 700, color: '#15803d', fontSize: '0.88rem' }}>Legitimate Transaction</div>
                    <div style={{ fontSize: '0.78rem', color: '#475569' }}>
                      Verified legitimate through cardholder outreach or confirmed merchant authorization.
                    </div>
                  </div>
                </label>

                {/* Option 2: SUSPICIOUS_CONFIRMED */}
                <label style={{
                  display: 'flex',
                  alignItems: 'flex-start',
                  gap: '10px',
                  padding: '0.85rem',
                  borderRadius: '8px',
                  border: `1px solid ${selectedDecision === 'SUSPICIOUS_CONFIRMED' ? '#ea580c' : '#e2e8f0'}`,
                  backgroundColor: selectedDecision === 'SUSPICIOUS_CONFIRMED' ? '#fff7ed' : '#ffffff',
                  cursor: 'pointer',
                  marginBottom: '0.75rem'
                }}>
                  <input
                    type="radio"
                    name="decision"
                    value="SUSPICIOUS_CONFIRMED"
                    checked={selectedDecision === 'SUSPICIOUS_CONFIRMED'}
                    onChange={(e) => setSelectedDecision(e.target.value)}
                    style={{ marginTop: '3px' }}
                  />
                  <div>
                    <div style={{ fontWeight: 700, color: '#c2410c', fontSize: '0.88rem' }}>Suspicious — Confirmed for Monitoring</div>
                    <div style={{ fontSize: '0.78rem', color: '#475569' }}>
                      Irregular parameters confirmed; account flagged for heightened surveillance and limits.
                    </div>
                  </div>
                </label>

                {/* Option 3: REQUIRES_FURTHER_REVIEW */}
                <label style={{
                  display: 'flex',
                  alignItems: 'flex-start',
                  gap: '10px',
                  padding: '0.85rem',
                  borderRadius: '8px',
                  border: `1px solid ${selectedDecision === 'REQUIRES_FURTHER_REVIEW' ? '#7c3aed' : '#e2e8f0'}`,
                  backgroundColor: selectedDecision === 'REQUIRES_FURTHER_REVIEW' ? '#faf5ff' : '#ffffff',
                  cursor: 'pointer',
                  marginBottom: '1rem'
                }}>
                  <input
                    type="radio"
                    name="decision"
                    value="REQUIRES_FURTHER_REVIEW"
                    checked={selectedDecision === 'REQUIRES_FURTHER_REVIEW'}
                    onChange={(e) => setSelectedDecision(e.target.value)}
                    style={{ marginTop: '3px' }}
                  />
                  <div>
                    <div style={{ fontWeight: 700, color: '#7c3aed', fontSize: '0.88rem' }}>Requires Further Review</div>
                    <div style={{ fontSize: '0.78rem', color: '#475569' }}>
                      Unable to reach customer or awaiting secondary verification documents.
                    </div>
                  </div>
                </label>

                <div style={{ marginBottom: '1rem' }}>
                  <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#475569', marginBottom: '4px' }}>
                    Disposition Rationale / Summary Comments:
                  </label>
                  <input
                    type="text"
                    value={decisionNotes}
                    onChange={(e) => setDecisionNotes(e.target.value)}
                    placeholder="Enter disposition comments for the audit log..."
                    style={{
                      width: '100%',
                      padding: '7px 10px',
                      fontSize: '0.82rem',
                      borderRadius: '6px',
                      border: '1px solid #cbd5e1',
                      boxSizing: 'border-box'
                    }}
                  />
                </div>

                <button
                  onClick={handleSubmitDecision}
                  disabled={isSubmittingDecision || !selectedDecision}
                  className="btn-primary"
                  style={{
                    width: '100%',
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    gap: '6px',
                    padding: '0.65rem',
                    fontSize: '0.88rem'
                  }}
                >
                  <CheckCircle size={16} />
                  <span>{isSubmittingDecision ? 'Submitting Disposition...' : 'Submit Resolution'}</span>
                </button>
              </div>
            </div>

            {/* Audit Trail Timeline */}
            <div style={{ backgroundColor: '#ffffff', borderRadius: '10px', border: '1px solid #e2e8f0', padding: '1.5rem', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '1.25rem', borderBottom: '1px solid #f1f5f9', paddingBottom: '0.75rem' }}>
                <History size={18} color="#003366" />
                <h2 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#0f172a', margin: 0 }}>Review Audit Trail</h2>
              </div>

              {detail.auditLogs && detail.auditLogs.length > 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
                  {detail.auditLogs.map((log, index) => (
                    <div
                      key={index}
                      style={{
                        display: 'flex',
                        gap: '12px',
                        padding: '0.75rem 1rem',
                        backgroundColor: '#f8fafc',
                        borderRadius: '8px',
                        border: '1px solid #e2e8f0',
                        fontSize: '0.82rem'
                      }}
                    >
                      <div style={{ minWidth: '130px', color: '#64748b', fontFamily: 'monospace', fontSize: '0.78rem' }}>
                        {log.createdAt}
                      </div>

                      <div style={{ minWidth: '140px' }}>
                        <span style={{
                          backgroundColor: '#e2e8f0',
                          color: '#334155',
                          padding: '2px 6px',
                          borderRadius: '4px',
                          fontSize: '0.72rem',
                          fontWeight: 700
                        }}>
                          {log.action}
                        </span>
                      </div>

                      <div style={{ flex: 1, color: '#0f172a' }}>
                        {log.details}
                      </div>

                      <div style={{ color: '#64748b', fontSize: '0.78rem' }}>
                        Reviewer: <strong>{log.reviewerId}</strong>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div style={{ color: '#64748b', fontSize: '0.85rem' }}>No audit activity recorded yet.</div>
              )}
            </div>
          </div>
        )}
      </main>

      <Footer />
    </div>
  );
};
