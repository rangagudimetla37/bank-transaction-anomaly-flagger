import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api } from '../services/api';
import type { TransactionData, TransactionAnalysisResult, FinalRiskAnalysis, GraphAnalysisResult } from '../services/api';
import { Header } from '../components/Header';
import { Footer } from '../components/Footer';
import {
  ArrowLeft,
  ShieldCheck,
  Calendar,
  Tag,
  MapPin,
  User,
  Hash,
  AlertCircle,
  FileText,
  ShieldAlert,
  CheckCircle2,
  Info,
  Activity,
  ExternalLink,
  Share2
} from 'lucide-react';

export const TransactionDetailsPage: React.FC = () => {
  const { transactionId } = useParams<{ transactionId: string }>();
  const [transaction, setTransaction] = useState<TransactionData | null>(null);
  const [analysis, setAnalysis] = useState<TransactionAnalysisResult | null>(null);
  const [finalAnalysis, setFinalAnalysis] = useState<FinalRiskAnalysis | null>(null);
  const [graphAnalysis, setGraphAnalysis] = useState<GraphAnalysisResult | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [notFound, setNotFound] = useState<boolean>(false);

  useEffect(() => {
    const fetchTransactionAndRisk = async () => {
      if (!transactionId) {
        setNotFound(true);
        setIsLoading(false);
        return;
      }
      try {
        const [txnData, riskData, finalData, graphData] = await Promise.all([
          api.getTransactionDetail(transactionId),
          api.getTransactionAnalysis(transactionId).catch(() => null),
          api.getFinalRiskAnalysis(transactionId).catch(() => null),
          api.getTransactionGraphAnalysis(transactionId).catch(() => null),
        ]);
        setTransaction(txnData);
        setAnalysis(riskData);
        setFinalAnalysis(finalData);
        setGraphAnalysis(graphData);
      } catch {
        setNotFound(true);
      } finally {
        setIsLoading(false);
      }
    };
    fetchTransactionAndRisk();
  }, [transactionId]);

  const formatCurrency = (val: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2,
    }).format(val);
  };

  const getRiskBadgeClass = (level?: string) => {
    switch (level) {
      case 'HIGH_RISK': return 'risk-badge-high_risk';
      case 'SUSPICIOUS': return 'risk-badge-suspicious';
      case 'WATCH': return 'risk-badge-watch';
      default: return 'risk-badge-normal';
    }
  };

  const getScoreColor = (score: number) => {
    if (score >= 80) return '#dc2626';
    if (score >= 60) return '#ea580c';
    if (score >= 30) return '#2563eb';
    return '#16a34a';
  };

  return (
    <div className="auth-wrapper">
      <Header />

      <main className="main-content" style={{ maxWidth: '840px' }}>
        <div className="page-header-row">
          <div className="page-title-block">
            <h1>Transaction Details</h1>
            <p>Complete ledger record and propositional rule screening</p>
          </div>
          <div style={{ display: 'flex', gap: '10px', alignItems: 'center', flexWrap: 'wrap' }}>
            <Link
              to={`/transaction-network?transactionId=${transactionId}`}
              className="btn-secondary"
              style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}
            >
              <Share2 size={16} />
              <span>View Transaction Network</span>
            </Link>
            <Link to="/transactions" className="btn-secondary">
              <ArrowLeft size={16} />
              <span>Back to Transactions</span>
            </Link>
          </div>
        </div>

        {isLoading ? (
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '300px', gap: '12px' }}>
            <div className="spinner" style={{ borderColor: '#003366', borderTopColor: 'transparent', width: '32px', height: '32px', borderWidth: '3px' }}></div>
            <span style={{ fontSize: '0.85rem', color: '#64748b' }}>Retrieving transaction & risk analysis...</span>
          </div>
        ) : notFound || !transaction ? (
          <div className="content-card" style={{ textAlign: 'center', padding: '48px 24px' }}>
            <AlertCircle size={44} color="#dc2626" style={{ margin: '0 auto 16px' }} />
            <h2 style={{ fontSize: '1.3rem', color: '#003366', marginBottom: '8px' }}>Transaction Not Found</h2>
            <p style={{ fontSize: '0.88rem', color: '#64748b', maxWidth: '440px', margin: '0 auto 24px' }}>
              The requested transaction ID does not exist or does not belong to your authenticated customer account.
            </p>
            <Link to="/transactions" className="btn-primary" style={{ display: 'inline-flex' }}>
              <ArrowLeft size={16} />
              <span>Return to My Transactions</span>
            </Link>
          </div>
        ) : (
          <>
            <div className="content-card">
              {/* Prominent Amount & Status Banner */}
              <div className="prominent-amount-banner">
                <span className={`status-badge status-${transaction.status.toLowerCase()}`}>
                  {transaction.status}
                </span>
                <div
                  className={`prominent-amount-val ${
                    transaction.transactionDirection === 'CREDIT' ? 'amount-credit' : 'amount-debit'
                  }`}
                >
                  {transaction.transactionDirection === 'CREDIT' ? '+' : '-'}
                  {formatCurrency(transaction.amount)}
                </div>
                <p style={{ fontSize: '0.88rem', color: '#475569', fontWeight: 500 }}>
                  {transaction.description}
                </p>
              </div>

              {/* Detailed fields grid */}
              <div className="detail-grid">
                <div className="detail-item">
                  <div className="detail-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Hash size={14} />
                    <span>Transaction ID</span>
                  </div>
                  <div className="detail-value" style={{ fontFamily: 'monospace' }}>
                    {transaction.transactionId}
                  </div>
                </div>

                <div className="detail-item">
                  <div className="detail-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Calendar size={14} />
                    <span>Date & Time</span>
                  </div>
                  <div className="detail-value">
                    {transaction.timestamp}
                  </div>
                </div>

                <div className="detail-item">
                  <div className="detail-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Tag size={14} />
                    <span>Transaction Type</span>
                  </div>
                  <div className="detail-value">
                    {transaction.transactionType}
                  </div>
                </div>

                <div className="detail-item">
                  <div className="detail-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <FileText size={14} />
                    <span>Direction / Flow</span>
                  </div>
                  <div className="detail-value">
                    {transaction.transactionDirection === 'CREDIT' ? 'Credit (Money In)' : 'Debit (Money Out)'}
                  </div>
                </div>

                <div className="detail-item">
                  <div className="detail-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <User size={14} />
                    <span>Recipient / Merchant</span>
                  </div>
                  <div className="detail-value">
                    {transaction.recipient}
                  </div>
                </div>

                <div className="detail-item">
                  <div className="detail-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <MapPin size={14} />
                    <span>Location</span>
                  </div>
                  <div className="detail-value">
                    {transaction.location}
                  </div>
                </div>
              </div>

              {/* Verification note */}
              <div style={{ marginTop: '24px', padding: '14px 18px', backgroundColor: '#f0fdf4', borderRadius: '8px', border: '1px solid #bbf7d0', display: 'flex', alignItems: 'center', gap: '10px' }}>
                <ShieldCheck size={18} color="#16a34a" />
                <span style={{ fontSize: '0.82rem', color: '#166534' }}>
                  This transaction record was authenticated and verified in the SecureBank customer ledger.
                </span>
              </div>
            </div>

            {/* PHASE 5: FINAL RISK ANALYSIS SECTION */}
            {finalAnalysis && (
              <div className="risk-analysis-section" style={{
                marginBottom: '24px',
                borderLeft: finalAnalysis.finalRiskScore >= 80 ? '6px solid #dc2626' :
                            finalAnalysis.finalRiskScore >= 60 ? '6px solid #ea580c' :
                            finalAnalysis.finalRiskScore >= 30 ? '6px solid #2563eb' : '6px solid #16a34a'
              }}>
                <div className="risk-analysis-header">
                  <div>
                    <h2 style={{ fontSize: '1.35rem', color: '#003366', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <ShieldAlert size={24} color={finalAnalysis.finalRiskScore >= 80 ? '#dc2626' : '#0066cc'} />
                      <span>RISK ANALYSIS</span>
                    </h2>
                    <p style={{ fontSize: '0.82rem', color: '#64748b', marginTop: '3px' }}>
                      Combined Evaluation: Rule-Based Screening (60%) + Python Statistical Anomaly (40%)
                    </p>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <span className={`risk-badge ${getRiskBadgeClass(finalAnalysis.riskLevel)}`} style={{ fontSize: '0.88rem', padding: '5px 12px' }}>
                      {finalAnalysis.riskLevel.replace('_', ' ')}
                    </span>
                    {finalAnalysis.alertId && (
                      <Link
                        to={`/alerts/${finalAnalysis.alertId}`}
                        className="btn-secondary btn-sm"
                        style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}
                      >
                        <span>Alert: {finalAnalysis.alertId}</span>
                        <ExternalLink size={14} />
                      </Link>
                    )}
                  </div>
                </div>

                {/* FINAL RISK SCORE */}
                <div className="risk-score-overview" style={{ marginBottom: '20px' }}>
                  <div className="score-circle-block">
                    <div className="score-large-text" style={{ color: getScoreColor(finalAnalysis.finalRiskScore) }}>
                      {finalAnalysis.finalRiskScore}
                    </div>
                    <div className="score-denom">/ 100 Final Score</div>
                  </div>

                  <div className="risk-meta-block">
                    <div className="risk-level-heading" style={{ color: getScoreColor(finalAnalysis.finalRiskScore), fontSize: '1.15rem' }}>
                      <span>FINAL RISK SCORE: {finalAnalysis.finalRiskScore} / 100 ({finalAnalysis.riskLevel.replace('_', ' ')})</span>
                    </div>

                    {/* Progress Bar */}
                    <div style={{ width: '100%', height: '8px', backgroundColor: '#e2e8f0', borderRadius: '4px', overflow: 'hidden', margin: '8px 0' }}>
                      <div
                        style={{
                          width: `${Math.min(100, Math.max(0, finalAnalysis.finalRiskScore))}%`,
                          height: '100%',
                          backgroundColor: getScoreColor(finalAnalysis.finalRiskScore),
                          transition: 'width 0.4s ease',
                        }}
                      />
                    </div>

                    <div style={{ padding: '10px 14px', backgroundColor: '#f8fafc', borderRadius: '6px', border: '1px solid #e2e8f0', marginTop: '6px' }}>
                      <strong style={{ fontSize: '0.84rem', color: '#1e293b' }}>Recommendation: </strong>
                      <span style={{ fontSize: '0.84rem', color: '#003366', fontWeight: 600 }}>{finalAnalysis.recommendation}</span>
                    </div>
                  </div>
                </div>

                {/* Score Breakdown */}
                <div style={{ marginBottom: '22px' }}>
                  <h4 style={{ fontSize: '0.92rem', color: '#003366', fontWeight: 700, marginBottom: '10px' }}>
                    Score Breakdown
                  </h4>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(190px, 1fr))', gap: '12px' }}>
                    <div style={{ padding: '12px', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                      <div style={{ fontSize: '0.74rem', color: '#64748b', fontWeight: 600, textTransform: 'uppercase' }}>Rule-Based Analysis</div>
                      <div style={{ fontSize: '1.25rem', fontWeight: 700, color: '#003366', marginTop: '2px' }}>
                        {finalAnalysis.ruleScore} <span style={{ fontSize: '0.82rem', color: '#64748b', fontWeight: 400 }}>/ 100</span>
                      </div>
                      <div style={{ fontSize: '0.76rem', color: '#475569', marginTop: '2px' }}>
                        Weight: 60% (contributes {Math.round(finalAnalysis.ruleScore * 0.60)} pts)
                      </div>
                    </div>

                    <div style={{ padding: '12px', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                      <div style={{ fontSize: '0.74rem', color: '#64748b', fontWeight: 600, textTransform: 'uppercase' }}>Statistical Analysis</div>
                      <div style={{ fontSize: '1.25rem', fontWeight: 700, color: '#0284c7', marginTop: '2px' }}>
                        {finalAnalysis.statisticalScore} <span style={{ fontSize: '0.82rem', color: '#64748b', fontWeight: 400 }}>/ 100</span>
                      </div>
                      <div style={{ fontSize: '0.76rem', color: '#475569', marginTop: '2px' }}>
                        Weight: 40% (contributes {Math.round(finalAnalysis.statisticalScore * 0.40)} pts)
                      </div>
                    </div>

                    <div style={{ padding: '12px', backgroundColor: '#eff6ff', borderRadius: '8px', border: '1px solid #bfdbfe' }}>
                      <div style={{ fontSize: '0.74rem', color: '#1e40af', fontWeight: 600, textTransform: 'uppercase' }}>Final Risk Score</div>
                      <div style={{ fontSize: '1.25rem', fontWeight: 700, color: getScoreColor(finalAnalysis.finalRiskScore), marginTop: '2px' }}>
                        {finalAnalysis.finalRiskScore} <span style={{ fontSize: '0.82rem', color: '#64748b', fontWeight: 400 }}>/ 100</span>
                      </div>
                      <div style={{ fontSize: '0.76rem', color: '#1e40af', marginTop: '2px' }}>
                        Formula: ({finalAnalysis.ruleScore} × 0.6) + ({finalAnalysis.statisticalScore} × 0.4)
                      </div>
                    </div>
                  </div>
                </div>

                {/* Risk Reasons: Why was this transaction flagged? */}
                <div>
                  <h4 style={{ fontSize: '0.92rem', color: '#003366', fontWeight: 700, marginBottom: '10px' }}>
                    Why was this transaction flagged?
                  </h4>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                    {finalAnalysis.reasons && finalAnalysis.reasons.length > 0 ? (
                      finalAnalysis.reasons.map((reason, idx) => (
                        <div
                          key={idx}
                          style={{
                            padding: '10px 14px',
                            backgroundColor: finalAnalysis.finalRiskScore >= 60 ? '#fffbeb' : '#f0fdf4',
                            borderRadius: '6px',
                            border: finalAnalysis.finalRiskScore >= 60 ? '1px solid #fde68a' : '1px solid #bbf7d0',
                            fontSize: '0.85rem',
                            color: finalAnalysis.finalRiskScore >= 60 ? '#92400e' : '#166534',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '10px',
                          }}
                        >
                          <span style={{ fontWeight: 700, color: finalAnalysis.finalRiskScore >= 60 ? '#d97706' : '#16a34a' }}>✓</span>
                          <span>{reason}</span>
                        </div>
                      ))
                    ) : (
                      <div style={{ padding: '10px 14px', backgroundColor: '#f0fdf4', borderRadius: '6px', border: '1px solid #bbf7d0', color: '#166534', fontSize: '0.85rem' }}>
                        ✓ No specific anomaly signals triggered for this transaction.
                      </div>
                    )}
                  </div>
                </div>
              </div>
            )}

            {/* PHASE 3: PRELIMINARY RULE ENGINE BREAKDOWN */}
            <div className="risk-analysis-section">
              <div className="risk-analysis-header">
                <div>
                  <h3 style={{ fontSize: '1.2rem', color: '#003366', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <ShieldAlert size={20} color="#0066cc" />
                    <span>Preliminary Risk Analysis</span>
                  </h3>
                  <p style={{ fontSize: '0.8rem', color: '#64748b', marginTop: '2px' }}>
                    Propositional-logic rule engine evaluation • Phase 3 OOPJ Screening
                  </p>
                </div>

                {analysis && (
                  <span className={`risk-badge ${getRiskBadgeClass(analysis.riskLevel)}`}>
                    {analysis.riskLevel.replace('_', ' ')}
                  </span>
                )}
              </div>

              {analysis ? (
                <>
                  {/* Score overview block */}
                  <div className="risk-score-overview">
                    <div className="score-circle-block">
                      <div className="score-large-text" style={{ color: getScoreColor(analysis.ruleScore) }}>
                        {analysis.ruleScore}
                      </div>
                      <div className="score-denom">/ 100 Score</div>
                    </div>

                    <div className="risk-meta-block">
                      <div className="risk-level-heading" style={{ color: getScoreColor(analysis.ruleScore) }}>
                        <span>Assessment: {analysis.riskLevel.replace('_', ' ')}</span>
                      </div>
                      <p className="risk-disclaimer-text">
                        <strong>Rule-Based Risk Score:</strong> This evaluation represents a preliminary rule-based assessment.
                        {analysis.ruleScore >= 60 ? (
                          <span style={{ color: '#ea580c', fontWeight: 600 }}> Flagged for further analysis and recommended for review.</span>
                        ) : analysis.ruleScore >= 30 ? (
                          <span style={{ color: '#2563eb' }}> Minor atypical parameters noted for monitoring.</span>
                        ) : (
                          <span style={{ color: '#16a34a' }}> Transaction conforms to standard expected customer profile.</span>
                        )}
                      </p>
                    </div>
                  </div>

                  {/* Triggered Rules Breakdown */}
                  <div>
                    <h4 style={{ fontSize: '0.95rem', color: '#1e293b', fontWeight: 700, marginBottom: '14px' }}>
                      Propositional Rule Evaluation Results
                    </h4>

                    {analysis.triggeredRules && analysis.triggeredRules.length > 0 ? (
                      <div className="triggered-rules-list">
                        {analysis.ruleResults.map((r) => (
                          <div
                            key={r.ruleId}
                            className={`rule-result-card ${r.triggered ? 'triggered' : 'normal'}`}
                          >
                            <div className="rule-card-header">
                              <span className="rule-title" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                {r.triggered ? (
                                  <ShieldAlert size={16} color="#ea580c" />
                                ) : (
                                  <CheckCircle2 size={16} color="#16a34a" />
                                )}
                                <span>{r.ruleName}</span>
                              </span>
                              <span className={`rule-points-tag ${r.triggered ? 'points-positive' : 'points-zero'}`}>
                                {r.triggered ? `+${r.riskPoints} pts` : '0 pts (Passed)'}
                              </span>
                            </div>
                            <p className="rule-explanation">
                              {r.explanation}
                            </p>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <div style={{ padding: '16px', backgroundColor: '#f0fdf4', borderRadius: '8px', border: '1px solid #bbf7d0', display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <CheckCircle2 size={20} color="#16a34a" />
                        <div>
                          <strong style={{ color: '#166534', fontSize: '0.88rem' }}>All Propositional Rules Passed:</strong>
                          <p style={{ fontSize: '0.8rem', color: '#15803d', marginTop: '2px' }}>
                            No anomaly rules were triggered for this transaction. Standard transaction pattern.
                          </p>
                        </div>
                      </div>
                    )}
                  </div>
                </>
              ) : (
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px', padding: '16px', backgroundColor: '#f8fafc', borderRadius: '8px' }}>
                  <Info size={18} color="#64748b" />
                  <span style={{ fontSize: '0.85rem', color: '#64748b' }}>
                    Rule engine analysis data is loading or temporarily unavailable.
                  </span>
                </div>
              )}
            </div>

            {/* PHASE 4: STATISTICAL OUTLIER ANALYSIS (PYTHON Z-SCORE ENGINE) */}
            {analysis && (analysis.zScore !== undefined || analysis.statisticalScore !== undefined) && (
              <div className="risk-analysis-section" style={{ marginTop: '24px' }}>
                <div className="risk-analysis-header">
                  <div>
                    <h3 style={{ fontSize: '1.2rem', color: '#003366', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <Activity size={20} color="#0284c7" />
                      <span>Statistical Outlier Analysis (Z-Score)</span>
                    </h3>
                    <p style={{ fontSize: '0.8rem', color: '#64748b', marginTop: '2px' }}>
                      Python NumPy &amp; Pandas Statistical Engine • Outlier Detection Formulation
                    </p>
                  </div>

                  <span className={`risk-badge ${analysis.statisticalAnomaly ? 'risk-badge-suspicious' : 'risk-badge-normal'}`}>
                    {analysis.statisticalAnomaly ? 'STATISTICAL OUTLIER' : 'STATISTICALLY NORMAL'}
                  </span>
                </div>

                <div className="risk-score-overview" style={{ borderLeftColor: analysis.statisticalAnomaly ? '#f97316' : '#10b981' }}>
                  <div className="score-circle-block">
                    <div className="score-large-text" style={{ color: analysis.statisticalAnomaly ? '#ea580c' : '#16a34a' }}>
                      {analysis.statisticalScore ?? 0}
                    </div>
                    <div className="score-denom">/ 100 Stat Score</div>
                  </div>

                  <div className="risk-meta-block">
                    <div className="risk-level-heading" style={{ color: analysis.statisticalAnomaly ? '#ea580c' : '#16a34a', display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <span>Z-Score: {analysis.zScore != null ? (analysis.zScore > 0 ? `+${analysis.zScore.toFixed(2)}` : analysis.zScore.toFixed(2)) : '0.00'} σ</span>
                      {analysis.compositeScore != null && (
                        <span style={{ fontSize: '0.8rem', backgroundColor: '#eff6ff', color: '#1e40af', padding: '2px 8px', borderRadius: '4px', border: '1px solid #bfdbfe' }}>
                          Composite Risk: {analysis.compositeScore}/100
                        </span>
                      )}
                    </div>
                    <p className="risk-disclaimer-text" style={{ marginTop: '6px' }}>
                      {analysis.statisticalExplanation ||
                        (analysis.statisticalAnomaly
                          ? 'Transaction amount diverges significantly from the customer historical distribution.'
                          : 'Transaction amount aligns with historical customer spending variance.')}
                    </p>
                  </div>
                </div>

                {/* Distribution Parameters */}
                <div style={{ marginTop: '16px', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '12px' }}>
                  <div style={{ padding: '12px', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                    <div style={{ fontSize: '0.75rem', color: '#64748b', textTransform: 'uppercase', fontWeight: 600 }}>Customer Historical Mean (μ)</div>
                    <div style={{ fontSize: '1.05rem', color: '#0f172a', fontWeight: 700, marginTop: '4px' }}>
                      ₹{analysis.historicalMean != null ? analysis.historicalMean.toLocaleString('en-IN', { minimumFractionDigits: 2 }) : '0.00'}
                    </div>
                  </div>

                  <div style={{ padding: '12px', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                    <div style={{ fontSize: '0.75rem', color: '#64748b', textTransform: 'uppercase', fontWeight: 600 }}>Standard Deviation (σ)</div>
                    <div style={{ fontSize: '1.05rem', color: '#0f172a', fontWeight: 700, marginTop: '4px' }}>
                      ₹{analysis.historicalStdDev != null ? analysis.historicalStdDev.toLocaleString('en-IN', { minimumFractionDigits: 2 }) : '0.00'}
                    </div>
                  </div>

                  <div style={{ padding: '12px', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                    <div style={{ fontSize: '0.75rem', color: '#64748b', textTransform: 'uppercase', fontWeight: 600 }}>Current Amount (x)</div>
                    <div style={{ fontSize: '1.05rem', color: '#003366', fontWeight: 700, marginTop: '4px' }}>
                      ₹{transaction?.amount != null ? transaction.amount.toLocaleString('en-IN', { minimumFractionDigits: 2 }) : '0.00'}
                    </div>
                  </div>

                  <div style={{ padding: '12px', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                    <div style={{ fontSize: '0.75rem', color: '#64748b', textTransform: 'uppercase', fontWeight: 600 }}>Outlier Threshold</div>
                    <div style={{ fontSize: '1.05rem', color: '#475569', fontWeight: 700, marginTop: '4px' }}>
                      |Z| ≥ 2.50 σ
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* PHASE 7: TRANSACTION RELATIONSHIP & NETWORK GRAPH ANALYSIS */}
            {graphAnalysis && (
              <div className="risk-analysis-section" style={{
                marginTop: '24px',
                borderLeft: graphAnalysis.graphScore >= 80 ? '6px solid #dc2626' :
                            graphAnalysis.graphScore >= 60 ? '6px solid #ea580c' :
                            graphAnalysis.graphScore >= 30 ? '6px solid #2563eb' : '6px solid #16a34a'
              }}>
                <div className="risk-analysis-header">
                  <div>
                    <h3 style={{ fontSize: '1.2rem', color: '#003366', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <Share2 size={20} color="#003366" />
                      <span>Transaction Relationship &amp; Network Analysis</span>
                    </h3>
                    <p style={{ fontSize: '0.8rem', color: '#64748b', marginTop: '2px' }}>
                      Graph topological screening &bull; Counterparty &amp; Location Relationship Intelligence
                    </p>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <span className={`risk-badge ${getRiskBadgeClass(graphAnalysis.graphRiskLevel)}`} style={{ fontSize: '0.88rem', padding: '5px 12px' }}>
                      {graphAnalysis.graphRiskLevel.replace('_', ' ')}
                    </span>
                    <Link
                      to={`/transaction-network?transactionId=${transaction.transactionId}`}
                      className="btn-secondary btn-sm"
                      style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}
                    >
                      <span>Interactive Graph</span>
                      <ExternalLink size={14} />
                    </Link>
                  </div>
                </div>

                <div className="risk-score-overview" style={{ marginBottom: '20px' }}>
                  <div className="score-circle-block">
                    <div className="score-large-text" style={{ color: getScoreColor(graphAnalysis.graphScore) }}>
                      {graphAnalysis.graphScore}
                    </div>
                    <div className="score-denom">/ 100 Graph Score</div>
                  </div>

                  <div className="risk-meta-block">
                    <div className="risk-level-heading" style={{ color: getScoreColor(graphAnalysis.graphScore) }}>
                      <span>GRAPH RELATIONSHIP RISK: {graphAnalysis.graphScore} / 100 ({graphAnalysis.graphRiskLevel.replace('_', ' ')})</span>
                    </div>

                    <div style={{ width: '100%', height: '8px', backgroundColor: '#e2e8f0', borderRadius: '4px', overflow: 'hidden', margin: '8px 0' }}>
                      <div
                        style={{
                          width: `${Math.min(100, Math.max(0, graphAnalysis.graphScore))}%`,
                          height: '100%',
                          backgroundColor: getScoreColor(graphAnalysis.graphScore),
                          transition: 'width 0.4s ease',
                        }}
                      />
                    </div>

                    <p className="risk-disclaimer-text">
                      Graph analysis evaluates structural counterparty patterns, location diversity, and recipient connectivity across ledger history without altering the primary fraud risk score.
                    </p>
                  </div>
                </div>

                {/* Network Relationship Indicators */}
                <div style={{ marginBottom: '18px' }}>
                  <h4 style={{ fontSize: '0.92rem', color: '#003366', fontWeight: 700, marginBottom: '10px' }}>
                    Network Indicators
                  </h4>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '12px' }}>
                    <div style={{ padding: '12px', backgroundColor: graphAnalysis.newRecipient ? '#fff7ed' : '#f8fafc', borderRadius: '8px', border: `1px solid ${graphAnalysis.newRecipient ? '#fed7aa' : '#e2e8f0'}` }}>
                      <div style={{ fontSize: '0.74rem', color: '#64748b', fontWeight: 600, textTransform: 'uppercase' }}>Recipient Status</div>
                      <div style={{ fontSize: '0.95rem', fontWeight: 700, color: graphAnalysis.newRecipient ? '#c2410c' : '#15803d', marginTop: '3px' }}>
                        {graphAnalysis.newRecipient ? 'New Recipient (+25 pts)' : 'Known Recipient (Passed)'}
                      </div>
                      <div style={{ fontSize: '0.74rem', color: '#64748b', marginTop: '2px' }}>
                        {graphAnalysis.recipientTransactionCount} prior transaction(s) with this recipient
                      </div>
                    </div>

                    <div style={{ padding: '12px', backgroundColor: graphAnalysis.sharedRecipient ? '#fef2f2' : '#f8fafc', borderRadius: '8px', border: `1px solid ${graphAnalysis.sharedRecipient ? '#fecaca' : '#e2e8f0'}` }}>
                      <div style={{ fontSize: '0.74rem', color: '#64748b', fontWeight: 600, textTransform: 'uppercase' }}>Multi-Account Association</div>
                      <div style={{ fontSize: '0.95rem', fontWeight: 700, color: graphAnalysis.sharedRecipient ? '#b91c1c' : '#15803d', marginTop: '3px' }}>
                        {graphAnalysis.sharedRecipient ? 'Shared Recipient (+20 pts)' : 'Exclusive to Account (Passed)'}
                      </div>
                      <div style={{ fontSize: '0.74rem', color: '#64748b', marginTop: '2px' }}>
                        {graphAnalysis.sharedRecipient ? 'Recipient appears across other bank accounts' : 'Recipient isolated to this customer'}
                      </div>
                    </div>

                    <div style={{ padding: '12px', backgroundColor: graphAnalysis.unusualLocation ? '#fff7ed' : '#f8fafc', borderRadius: '8px', border: `1px solid ${graphAnalysis.unusualLocation ? '#fed7aa' : '#e2e8f0'}` }}>
                      <div style={{ fontSize: '0.74rem', color: '#64748b', fontWeight: 600, textTransform: 'uppercase' }}>Location Pattern</div>
                      <div style={{ fontSize: '0.95rem', fontWeight: 700, color: graphAnalysis.unusualLocation ? '#c2410c' : '#15803d', marginTop: '3px' }}>
                        {graphAnalysis.unusualLocation ? 'Unusual Location (+20 pts)' : 'Established Location (Passed)'}
                      </div>
                      <div style={{ fontSize: '0.74rem', color: '#64748b', marginTop: '2px' }}>
                        {graphAnalysis.locationTransactionCount} transaction(s) at this location
                      </div>
                    </div>

                    <div style={{ padding: '12px', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                      <div style={{ fontSize: '0.74rem', color: '#64748b', fontWeight: 600, textTransform: 'uppercase' }}>Account Graph Breadth</div>
                      <div style={{ fontSize: '0.95rem', fontWeight: 700, color: '#003366', marginTop: '3px' }}>
                        {graphAnalysis.uniqueRecipients} Recipients &bull; {graphAnalysis.uniqueLocations} Locations
                      </div>
                      <div style={{ fontSize: '0.74rem', color: '#64748b', marginTop: '2px' }}>
                        Across {graphAnalysis.totalAccountTransactions} total ledger transactions
                      </div>
                    </div>
                  </div>
                </div>

                {/* Graph Diagnostic Reasons */}
                {graphAnalysis.reasons && graphAnalysis.reasons.length > 0 && (
                  <div>
                    <h4 style={{ fontSize: '0.92rem', color: '#003366', fontWeight: 700, marginBottom: '8px' }}>
                      Graph Diagnostic Reasons
                    </h4>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                      {graphAnalysis.reasons.map((reason, idx) => (
                        <div
                          key={idx}
                          style={{
                            padding: '9px 12px',
                            backgroundColor: '#fffbeb',
                            borderRadius: '6px',
                            border: '1px solid #fde68a',
                            fontSize: '0.84rem',
                            color: '#92400e',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '8px',
                          }}
                        >
                          <span style={{ fontWeight: 700, color: '#d97706' }}>&bull;</span>
                          <span>{reason}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            )}
          </>
        )}
      </main>

      <Footer />
    </div>
  );
};
