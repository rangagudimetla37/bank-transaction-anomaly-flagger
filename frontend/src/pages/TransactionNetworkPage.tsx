import React, { useEffect, useState, useMemo, useRef } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { api } from '../services/api';
import type {
  TransactionGraph,
  GraphNode,
  GraphEdge,
  GraphAnalysisResult,
  RiskLevelType
} from '../services/api';
import { Header } from '../components/Header';
import { Footer } from '../components/Footer';
import {
  Share2,
  RefreshCw,
  ZoomIn,
  ZoomOut,
  Maximize2,
  ExternalLink,
  ChevronRight,
  Activity,
  Layers
} from 'lucide-react';

interface PositionedNode extends GraphNode {
  x: number;
  y: number;
  radius: number;
  color: string;
}

export const TransactionNetworkPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const focusTxnId = searchParams.get('transactionId') || '';

  const [graphData, setGraphData] = useState<TransactionGraph | null>(null);
  const [selectedNode, setSelectedNode] = useState<GraphNode | null>(null);
  const [hoveredNode, setHoveredNode] = useState<GraphNode | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [errorMessage, setErrorMessage] = useState<string>('');

  // Zoom & Pan state
  const [zoom, setZoom] = useState<number>(1);
  const [pan, setPan] = useState<{ x: number; y: number }>({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState<boolean>(false);
  const [dragStart, setDragStart] = useState<{ x: number; y: number }>({ x: 0, y: 0 });
  const svgRef = useRef<SVGSVGElement | null>(null);

  const loadGraph = async (txnId?: string) => {
    setIsLoading(true);
    setErrorMessage('');
    try {
      const data = await api.getAccountNetwork(txnId || undefined);
      setGraphData(data);
      if (txnId && data.nodes) {
        const target = data.nodes.find(n => n.id.includes(txnId) || n.label.includes(txnId));
        if (target) {
          setSelectedNode(target);
        }
      }
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to load transaction network.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadGraph(focusTxnId);
  }, [focusTxnId]);

  // Handle Focus Change
  const handleSelectTransaction = (newTxnId: string) => {
    if (newTxnId) {
      setSearchParams({ transactionId: newTxnId });
    } else {
      setSearchParams({});
    }
  };

  // Graph Layout Computation (Deterministic Layered Radial Layout)
  const { positionedNodes, positionedEdges, bounds } = useMemo(() => {
    if (!graphData || !graphData.nodes || graphData.nodes.length === 0) {
      return { positionedNodes: [], positionedEdges: [], bounds: { width: 900, height: 600 } };
    }

    const width = 1000;
    const height = 650;
    const centerX = width / 2;
    const centerY = height / 2;

    const accountNode = graphData.nodes.find(n => n.type === 'ACCOUNT');
    const txnNodes = graphData.nodes.filter(n => n.type === 'TRANSACTION');
    const recipientNodes = graphData.nodes.filter(n => n.type === 'RECIPIENT');
    const locationNodes = graphData.nodes.filter(n => n.type === 'LOCATION');

    const nodeMap = new Map<string, PositionedNode>();

    // 1. Account Center Node
    if (accountNode) {
      nodeMap.set(accountNode.id, {
        ...accountNode,
        x: centerX,
        y: centerY,
        radius: 36,
        color: '#003366'
      });
    }

    // 2. Position Transactions in an Inner Circle around Account
    const txnRadius = 180;
    const txnStep = txnNodes.length > 0 ? (2 * Math.PI) / txnNodes.length : 0;
    txnNodes.forEach((txn, i) => {
      const angle = i * txnStep - Math.PI / 2;
      const x = centerX + txnRadius * Math.cos(angle);
      const y = centerY + txnRadius * Math.sin(angle);
      const isFocused = focusTxnId && (txn.id.includes(focusTxnId) || txn.label.includes(focusTxnId));

      let color = '#2563eb'; // Default Blue
      const amount = txn.properties?.amount;
      if (amount && amount >= 50000) {
        color = '#dc2626'; // High amount alert Red
      } else if (amount && amount >= 10000) {
        color = '#ea580c'; // Moderate amount Orange
      }

      nodeMap.set(txn.id, {
        ...txn,
        x,
        y,
        radius: isFocused ? 22 : 16,
        color: isFocused ? '#7c3aed' : color
      });
    });

    // 3. Position Recipients in Outer Right Ring
    const recRadius = 310;
    const recStep = recipientNodes.length > 0 ? Math.PI / Math.max(1, recipientNodes.length - 1) : 0;
    recipientNodes.forEach((rec, i) => {
      const angle = recipientNodes.length === 1 ? 0 : -Math.PI / 2 + i * recStep;
      const x = centerX + recRadius * Math.cos(angle) * 1.05;
      const y = centerY + recRadius * Math.sin(angle);
      const isShared = rec.properties?.isShared;

      nodeMap.set(rec.id, {
        ...rec,
        x,
        y,
        radius: isShared ? 24 : 18,
        color: isShared ? '#c026d3' : '#7c3aed' // Fuchsia for shared, Purple for standard
      });
    });

    // 4. Position Locations in Outer Left Ring
    const locRadius = 310;
    const locStep = locationNodes.length > 0 ? Math.PI / Math.max(1, locationNodes.length - 1) : 0;
    locationNodes.forEach((loc, i) => {
      const angle = locationNodes.length === 1 ? Math.PI : Math.PI / 2 + i * locStep;
      const x = centerX + locRadius * Math.cos(angle) * 1.05;
      const y = centerY + locRadius * Math.sin(angle);

      nodeMap.set(loc.id, {
        ...loc,
        x,
        y,
        radius: 20,
        color: '#059669' // Emerald Green
      });
    });

    // Match Edges
    const edgesWithCoords = graphData.edges
      .map(edge => {
        const sourceNode = nodeMap.get(edge.source);
        const targetNode = nodeMap.get(edge.target);
        if (!sourceNode || !targetNode) return null;
        return {
          ...edge,
          sourceNode,
          targetNode
        };
      })
      .filter(Boolean) as Array<GraphEdge & { sourceNode: PositionedNode; targetNode: PositionedNode }>;

    return {
      positionedNodes: Array.from(nodeMap.values()),
      positionedEdges: edgesWithCoords,
      bounds: { width, height }
    };
  }, [graphData, focusTxnId]);

  // Pan Handlers
  const handleMouseDown = (e: React.MouseEvent) => {
    setIsDragging(true);
    setDragStart({ x: e.clientX - pan.x, y: e.clientY - pan.y });
  };

  const handleMouseMove = (e: React.MouseEvent) => {
    if (isDragging) {
      setPan({ x: e.clientX - dragStart.x, y: e.clientY - dragStart.y });
    }
  };

  const handleMouseUp = () => {
    setIsDragging(false);
  };

  const handleResetView = () => {
    setZoom(1);
    setPan({ x: 0, y: 0 });
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
        padding: '3px 8px',
        borderRadius: '6px',
        fontSize: '0.78rem',
        fontWeight: 700,
        display: 'inline-flex',
        alignItems: 'center',
        gap: '4px'
      }}>
        {score !== undefined ? `${score}/100 ` : ''}{level.replace('_', ' ')}
      </span>
    );
  };

  const analysis: GraphAnalysisResult | undefined = graphData?.analysis;
  const stats = graphData?.stats;

  return (
    <div className="banking-page-layout">
      <Header />

      <main className="dashboard-content" style={{ maxWidth: '1280px', margin: '0 auto', padding: '2rem 1.5rem' }}>
        {/* Breadcrumb Navigation */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.82rem', color: '#64748b', marginBottom: '1rem' }}>
          <Link to="/dashboard" style={{ color: '#003366', textDecoration: 'none', fontWeight: 600 }}>Home</Link>
          <ChevronRight size={14} />
          <span style={{ color: '#0f172a', fontWeight: 600 }}>Transaction Network</span>
          {focusTxnId && (
            <>
              <ChevronRight size={14} />
              <span style={{ color: '#7c3aed', fontWeight: 700 }}>{focusTxnId}</span>
            </>
          )}
        </div>

        {/* Page Hero Header */}
        <div style={{
          backgroundColor: '#002244',
          borderRadius: '12px',
          padding: '1.75rem 2rem',
          color: '#ffffff',
          marginBottom: '1.75rem',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1.25rem',
          boxShadow: '0 4px 12px rgba(0, 34, 68, 0.15)'
        }}>
          <div>
            <div style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', backgroundColor: 'rgba(255, 255, 255, 0.15)', padding: '3px 10px', borderRadius: '20px', fontSize: '0.75rem', fontWeight: 600, marginBottom: '0.5rem' }}>
              <Share2 size={13} color="#38bdf8" />
              <span>PHASE 7: RELATIONSHIP & NETWORK ANALYSIS</span>
            </div>
            <h1 style={{ fontSize: '1.65rem', fontWeight: 800, margin: '0 0 0.4rem 0', letterSpacing: '-0.02em' }}>
              Transaction Network
            </h1>
            <p style={{ color: '#cbd5e1', fontSize: '0.88rem', margin: 0, maxWidth: '650px', lineHeight: 1.4 }}>
              Visualize relationships between accounts, transactions, counterparty recipients, and transaction locations.
            </p>
          </div>

          {/* Transaction Focus Selector */}
          <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center', flexWrap: 'wrap' }}>
            <div style={{ backgroundColor: 'rgba(255, 255, 255, 0.1)', padding: '6px 12px', borderRadius: '8px' }}>
              <span style={{ fontSize: '0.75rem', color: '#94a3b8', display: 'block', marginBottom: '2px' }}>FOCUSED TRANSACTION</span>
              <select
                value={focusTxnId}
                onChange={(e) => handleSelectTransaction(e.target.value)}
                style={{
                  backgroundColor: '#003366',
                  color: '#ffffff',
                  border: '1px solid rgba(255, 255, 255, 0.3)',
                  borderRadius: '4px',
                  padding: '4px 8px',
                  fontSize: '0.82rem',
                  outline: 'none',
                  cursor: 'pointer'
                }}
              >
                <option value="">Full Network Overview</option>
                <option value="TXN-20261004-0035">TXN-20261004-0035 (₹95,000 Apex Luxury Bullion)</option>
                <option value="TXN-20261004-0026">TXN-20261004-0026 (₹48,500 Global Imports)</option>
                <option value="TXN-20261004-0028">TXN-20261004-0028 (Speed Merchant Burst)</option>
                <option value="TXN-20261001-0021">TXN-20261001-0021 (Salary Tech Corp)</option>
                <option value="TXN-20260925-0016">TXN-20260925-0016 (Flipkart Shared Recipient)</option>
              </select>
            </div>

            <button
              onClick={() => loadGraph(focusTxnId)}
              disabled={isLoading}
              className="btn-secondary"
              style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', padding: '0.6rem 0.9rem', fontSize: '0.82rem' }}
            >
              <RefreshCw size={14} className={isLoading ? 'spin-icon' : ''} />
              <span>Refresh</span>
            </button>
          </div>
        </div>

        {errorMessage && (
          <div style={{ backgroundColor: '#fef2f2', border: '1px solid #fecaca', color: '#b91c1c', padding: '1rem', borderRadius: '8px', marginBottom: '1.5rem', fontSize: '0.88rem' }}>
            {errorMessage}
          </div>
        )}

        {/* Network Canvas & Interactive Workbench */}
        <div style={{
          backgroundColor: '#ffffff',
          borderRadius: '12px',
          border: '1px solid #e2e8f0',
          boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
          overflow: 'hidden',
          marginBottom: '2rem',
          position: 'relative'
        }}>
          {/* Canvas Controls Header */}
          <div style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            padding: '0.75rem 1.25rem',
            backgroundColor: '#f8fafc',
            borderBottom: '1px solid #e2e8f0',
            fontSize: '0.82rem'
          }}>
            {/* Legend */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem', flexWrap: 'wrap' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <span style={{ width: '12px', height: '12px', borderRadius: '50%', backgroundColor: '#003366', display: 'inline-block' }}></span>
                <span style={{ fontWeight: 600, color: '#334155' }}>Account</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <span style={{ width: '12px', height: '12px', borderRadius: '50%', backgroundColor: '#2563eb', display: 'inline-block' }}></span>
                <span style={{ fontWeight: 600, color: '#334155' }}>Transaction</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <span style={{ width: '12px', height: '12px', borderRadius: '4px', backgroundColor: '#7c3aed', display: 'inline-block' }}></span>
                <span style={{ fontWeight: 600, color: '#334155' }}>Recipient</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <span style={{ width: '12px', height: '12px', borderRadius: '4px', backgroundColor: '#c026d3', display: 'inline-block' }}></span>
                <span style={{ fontWeight: 600, color: '#c026d3' }}>Shared Recipient</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <span style={{ width: '12px', height: '12px', borderRadius: '4px', backgroundColor: '#059669', display: 'inline-block' }}></span>
                <span style={{ fontWeight: 600, color: '#334155' }}>Location</span>
              </div>
            </div>

            {/* Zoom / Pan Controls */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <button
                onClick={() => setZoom(prev => Math.min(prev + 0.2, 2.2))}
                title="Zoom In"
                style={{ padding: '5px 8px', borderRadius: '4px', border: '1px solid #cbd5e1', backgroundColor: '#ffffff', cursor: 'pointer' }}
              >
                <ZoomIn size={14} />
              </button>
              <button
                onClick={() => setZoom(prev => Math.max(prev - 0.2, 0.5))}
                title="Zoom Out"
                style={{ padding: '5px 8px', borderRadius: '4px', border: '1px solid #cbd5e1', backgroundColor: '#ffffff', cursor: 'pointer' }}
              >
                <ZoomOut size={14} />
              </button>
              <button
                onClick={handleResetView}
                title="Reset View"
                style={{ padding: '5px 8px', borderRadius: '4px', border: '1px solid #cbd5e1', backgroundColor: '#ffffff', cursor: 'pointer' }}
              >
                <Maximize2 size={14} />
              </button>
            </div>
          </div>

          {/* SVG Canvas */}
          <div
            style={{
              height: '560px',
              backgroundColor: '#fafbfc',
              position: 'relative',
              overflow: 'hidden',
              cursor: isDragging ? 'grabbing' : 'grab'
            }}
            onMouseDown={handleMouseDown}
            onMouseMove={handleMouseMove}
            onMouseUp={handleMouseUp}
            onMouseLeave={handleMouseUp}
          >
            {isLoading ? (
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', flexDirection: 'column', gap: '10px' }}>
                <div className="spinner"></div>
                <span style={{ fontSize: '0.85rem', color: '#64748b' }}>Constructing in-memory transaction graph...</span>
              </div>
            ) : (
              <svg
                ref={svgRef}
                width="100%"
                height="100%"
                viewBox={`0 0 ${bounds.width} ${bounds.height}`}
                style={{
                  transform: `translate(${pan.x}px, ${pan.y}px) scale(${zoom})`,
                  transformOrigin: 'center center',
                  transition: isDragging ? 'none' : 'transform 0.1s ease-out'
                }}
              >
                <defs>
                  {/* Marker for directed edges */}
                  <marker
                    id="arrowhead"
                    viewBox="0 0 10 10"
                    refX="18"
                    refY="5"
                    markerWidth="6"
                    markerHeight="6"
                    orient="auto-start-reverse"
                  >
                    <path d="M 0 1 L 10 5 L 0 9 z" fill="#94a3b8" />
                  </marker>

                  <marker
                    id="arrowhead-highlight"
                    viewBox="0 0 10 10"
                    refX="18"
                    refY="5"
                    markerWidth="6"
                    markerHeight="6"
                    orient="auto-start-reverse"
                  >
                    <path d="M 0 1 L 10 5 L 0 9 z" fill="#0284c7" />
                  </marker>
                </defs>

                {/* Edges */}
                <g className="edges">
                  {positionedEdges.map((edge, index) => {
                    const isEdgeActive =
                      (selectedNode && (edge.source === selectedNode.id || edge.target === selectedNode.id)) ||
                      (hoveredNode && (edge.source === hoveredNode.id || edge.target === hoveredNode.id));

                    return (
                      <g key={index}>
                        <line
                          x1={edge.sourceNode.x}
                          y1={edge.sourceNode.y}
                          x2={edge.targetNode.x}
                          y2={edge.targetNode.y}
                          stroke={isEdgeActive ? '#0284c7' : '#cbd5e1'}
                          strokeWidth={isEdgeActive ? 2.5 : 1.2}
                          strokeDasharray={edge.relationship === 'TRANSACTION_AT_LOCATION' ? '3,3' : 'none'}
                          markerEnd={isEdgeActive ? 'url(#arrowhead-highlight)' : 'url(#arrowhead)'}
                          opacity={isEdgeActive ? 1 : 0.65}
                        />
                      </g>
                    );
                  })}
                </g>

                {/* Nodes */}
                <g className="nodes">
                  {positionedNodes.map((node) => {
                    const isSelected = selectedNode?.id === node.id;
                    const isHovered = hoveredNode?.id === node.id;

                    return (
                      <g
                        key={node.id}
                        transform={`translate(${node.x}, ${node.y})`}
                        onClick={(e) => {
                          e.stopPropagation();
                          setSelectedNode(node);
                        }}
                        onMouseEnter={() => setHoveredNode(node)}
                        onMouseLeave={() => setHoveredNode(null)}
                        style={{ cursor: 'pointer' }}
                      >
                        {/* Selected / Hover Ring */}
                        {(isSelected || isHovered) && (
                          <circle
                            r={node.radius + 6}
                            fill="none"
                            stroke={isSelected ? '#0284c7' : '#94a3b8'}
                            strokeWidth={3}
                            opacity={0.8}
                          />
                        )}

                        {/* Node Shape */}
                        <circle
                          r={node.radius}
                          fill={node.color}
                          stroke="#ffffff"
                          strokeWidth={2.5}
                          filter="drop-shadow(0px 2px 4px rgba(0,0,0,0.15))"
                        />

                        {/* Node Label Text */}
                        <text
                          textAnchor="middle"
                          y={node.radius + 14}
                          fontSize={node.type === 'ACCOUNT' ? '12px' : '10px'}
                          fontWeight={node.type === 'ACCOUNT' || isSelected ? 700 : 500}
                          fill="#0f172a"
                          style={{
                            pointerEvents: 'none',
                            textShadow: '0 1px 2px rgba(255,255,255,0.9)'
                          }}
                        >
                          {node.label.length > 18 ? node.label.substring(0, 16) + '...' : node.label}
                        </text>

                        {/* Node Type Indicator Badge */}
                        <text
                          textAnchor="middle"
                          y={node.radius + 26}
                          fontSize="8px"
                          fontWeight={600}
                          fill={node.properties?.isShared ? '#c026d3' : '#64748b'}
                          style={{ pointerEvents: 'none' }}
                        >
                          {node.properties?.isShared ? 'SHARED RECIPIENT' : node.type}
                        </text>
                      </g>
                    );
                  })}
                </g>
              </svg>
            )}

            {/* Selected Node Floating Details Card */}
            {selectedNode && (
              <div style={{
                position: 'absolute',
                top: '16px',
                right: '16px',
                width: '290px',
                backgroundColor: '#ffffff',
                border: '1px solid #cbd5e1',
                borderRadius: '10px',
                padding: '1rem',
                boxShadow: '0 4px 14px rgba(0,0,0,0.12)',
                fontSize: '0.82rem',
                zIndex: 10
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px', borderBottom: '1px solid #f1f5f9', paddingBottom: '6px' }}>
                  <span style={{
                    fontWeight: 700,
                    color: selectedNode.type === 'ACCOUNT' ? '#003366' :
                           selectedNode.type === 'TRANSACTION' ? '#2563eb' :
                           selectedNode.type === 'RECIPIENT' ? '#7c3aed' : '#059669',
                    fontSize: '0.78rem',
                    textTransform: 'uppercase'
                  }}>
                    {selectedNode.type} NODE
                  </span>
                  <button
                    onClick={() => setSelectedNode(null)}
                    style={{ background: 'none', border: 'none', color: '#94a3b8', cursor: 'pointer', fontSize: '1rem', lineHeight: 1 }}
                  >
                    ×
                  </button>
                </div>

                <div style={{ fontWeight: 700, color: '#0f172a', fontSize: '0.92rem', marginBottom: '8px' }}>
                  {selectedNode.label}
                </div>

                {/* Metadata based on node type */}
                {selectedNode.type === 'TRANSACTION' && (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', color: '#475569' }}>
                    <div>Amount: <strong style={{ color: '#0f172a' }}>₹{selectedNode.properties?.amount?.toLocaleString('en-IN')}</strong></div>
                    <div>Counterparty: <strong>{selectedNode.properties?.recipient}</strong></div>
                    <div>Location: <strong>{selectedNode.properties?.location}</strong></div>
                    <div>Timestamp: <span>{selectedNode.properties?.timestamp}</span></div>
                    <div>Category: <span>{selectedNode.properties?.category}</span></div>
                    <div style={{ marginTop: '8px' }}>
                      <Link
                        to={`/transactions/${selectedNode.label}`}
                        className="btn-primary"
                        style={{ display: 'inline-flex', alignItems: 'center', gap: '4px', padding: '4px 10px', fontSize: '0.75rem', textDecoration: 'none' }}
                      >
                        <span>View Details</span>
                        <ExternalLink size={12} />
                      </Link>
                    </div>
                  </div>
                )}

                {selectedNode.type === 'RECIPIENT' && (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', color: '#475569' }}>
                    <div>Connected Transactions: <strong style={{ color: '#0f172a' }}>{selectedNode.properties?.transactionCount ?? 1}</strong></div>
                    {selectedNode.properties?.isShared && (
                      <div style={{ backgroundColor: '#fdf4ff', border: '1px solid #f0abfc', color: '#a21caf', padding: '4px 8px', borderRadius: '4px', fontWeight: 600, fontSize: '0.75rem', marginTop: '4px' }}>
                        ★ Shared Counterparty: Connected to multiple accounts in database.
                      </div>
                    )}
                  </div>
                )}

                {selectedNode.type === 'LOCATION' && (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', color: '#475569' }}>
                    <div>Transaction Frequency: <strong style={{ color: '#0f172a' }}>{selectedNode.properties?.transactionCount ?? 1}</strong></div>
                    <div style={{ color: '#64748b', fontSize: '0.75rem', marginTop: '4px' }}>
                      Geographic node in customer transaction baseline.
                    </div>
                  </div>
                )}

                {selectedNode.type === 'ACCOUNT' && (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', color: '#475569' }}>
                    <div>Account Holder: <strong style={{ color: '#0f172a' }}>{selectedNode.properties?.customerName}</strong></div>
                    <div>Bank ID: <strong>{selectedNode.properties?.bankId}</strong></div>
                    <div>Ledger Balance: <strong style={{ color: '#16a34a' }}>₹{selectedNode.properties?.balance?.toLocaleString('en-IN')}</strong></div>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>

        {/* Network Insights & Analysis Section */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
          {/* Card 1: Network Topology Metrics */}
          <div style={{ backgroundColor: '#ffffff', borderRadius: '10px', border: '1px solid #e2e8f0', padding: '1.5rem', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '1.25rem', borderBottom: '1px solid #f1f5f9', paddingBottom: '0.75rem' }}>
              <Layers size={18} color="#003366" />
              <h2 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#0f172a', margin: 0 }}>Network Topology Insights</h2>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', fontSize: '0.85rem' }}>
              <div style={{ backgroundColor: '#f8fafc', padding: '0.85rem', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                <span style={{ fontSize: '0.75rem', color: '#64748b', display: 'block' }}>UNIQUE RECIPIENTS</span>
                <strong style={{ fontSize: '1.4rem', color: '#7c3aed' }}>{stats?.uniqueRecipients ?? 0}</strong>
              </div>

              <div style={{ backgroundColor: '#f8fafc', padding: '0.85rem', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                <span style={{ fontSize: '0.75rem', color: '#64748b', display: 'block' }}>UNIQUE LOCATIONS</span>
                <strong style={{ fontSize: '1.4rem', color: '#059669' }}>{stats?.uniqueLocations ?? 0}</strong>
              </div>

              <div style={{ backgroundColor: '#f8fafc', padding: '0.85rem', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                <span style={{ fontSize: '0.75rem', color: '#64748b', display: 'block' }}>TOTAL TRANSACTIONS</span>
                <strong style={{ fontSize: '1.4rem', color: '#0f172a' }}>{stats?.totalTransactions ?? 0}</strong>
              </div>

              <div style={{ backgroundColor: '#f8fafc', padding: '0.85rem', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                <span style={{ fontSize: '0.75rem', color: '#64748b', display: 'block' }}>NETWORK EDGES</span>
                <strong style={{ fontSize: '1.4rem', color: '#0284c7' }}>{stats?.totalEdges ?? 0}</strong>
              </div>
            </div>

            <div style={{ marginTop: '1rem', fontSize: '0.8rem', color: '#64748b' }}>
              <div>Primary City: <strong style={{ color: '#0f172a' }}>{stats?.mostFrequentLocation || 'N/A'}</strong></div>
              <div>Most Connected Counterparty: <strong style={{ color: '#0f172a' }}>{stats?.mostFrequentRecipient || 'N/A'}</strong></div>
            </div>
          </div>

          {/* Card 2: Graph-Based Risk Indicator */}
          <div style={{ backgroundColor: '#ffffff', borderRadius: '10px', border: '1px solid #e2e8f0', padding: '1.5rem', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.25rem', borderBottom: '1px solid #f1f5f9', paddingBottom: '0.75rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Activity size={18} color="#003366" />
                <h2 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#0f172a', margin: 0 }}>Graph Risk Indicator</h2>
              </div>
              {analysis && getRiskBadge(analysis.graphRiskLevel, analysis.graphScore)}
            </div>

            {analysis ? (
              <div>
                <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px', marginBottom: '0.75rem' }}>
                  <span style={{ fontSize: '2rem', fontWeight: 800, color: '#0f172a' }}>
                    {analysis.graphScore}
                  </span>
                  <span style={{ fontSize: '0.85rem', color: '#64748b' }}>/ 100 Graph Score</span>
                </div>

                {/* Badges for New / Shared Recipient */}
                <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '1rem' }}>
                  {analysis.newRecipient && (
                    <span style={{ backgroundColor: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '2px 8px', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600 }}>
                      New Recipient
                    </span>
                  )}
                  {analysis.sharedRecipient && (
                    <span style={{ backgroundColor: '#fdf4ff', color: '#a21caf', border: '1px solid #f0abfc', padding: '2px 8px', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600 }}>
                      Shared Across Accounts
                    </span>
                  )}
                  {!analysis.newRecipient && !analysis.sharedRecipient && (
                    <span style={{ backgroundColor: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', padding: '2px 8px', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600 }}>
                      Established Relationship
                    </span>
                  )}
                </div>

                {/* Explanations */}
                <div>
                  <div style={{ fontSize: '0.78rem', fontWeight: 700, color: '#475569', textTransform: 'uppercase', marginBottom: '6px' }}>Network Reasons:</div>
                  <ul style={{ margin: 0, paddingLeft: '1.25rem', fontSize: '0.82rem', color: '#334155' }}>
                    {analysis.reasons.map((reason, idx) => (
                      <li key={idx} style={{ marginBottom: '4px' }}>{reason}</li>
                    ))}
                  </ul>
                </div>
              </div>
            ) : (
              <div style={{ color: '#64748b', fontSize: '0.85rem' }}>
                Select a transaction from the dropdown to view its graph anomaly analysis.
              </div>
            )}
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
};
