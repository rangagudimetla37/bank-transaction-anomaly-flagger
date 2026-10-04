import React, { useEffect, useState, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../services/api';
import type { TransactionData } from '../services/api';
import { Header } from '../components/Header';
import { Footer } from '../components/Footer';
import { Search, Filter, AlertCircle, ArrowLeft, RefreshCw } from 'lucide-react';

export const TransactionsPage: React.FC = () => {
  const [transactions, setTransactions] = useState<TransactionData[]>([]);
  const [customerName, setCustomerName] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [errorMessage, setErrorMessage] = useState<string>('');

  // Search & Filter states
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [typeFilter, setTypeFilter] = useState<string>('ALL');
  const [directionFilter, setDirectionFilter] = useState<string>('ALL');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  const loadTransactions = async () => {
    setIsLoading(true);
    setErrorMessage('');
    try {
      const summary = await api.getTransactions();
      setTransactions(summary.transactions || []);
      setCustomerName(summary.customerName || '');
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to load transactions.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadTransactions();
  }, []);

  const formatCurrency = (val: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2,
    }).format(val);
  };

  // Filtered transactions
  const filteredTransactions = useMemo(() => {
    return transactions.filter((t) => {
      // Search term
      const matchesSearch =
        searchTerm === '' ||
        t.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
        t.recipient.toLowerCase().includes(searchTerm.toLowerCase()) ||
        t.transactionId.toLowerCase().includes(searchTerm.toLowerCase()) ||
        t.location.toLowerCase().includes(searchTerm.toLowerCase());

      // Type filter
      const matchesType = typeFilter === 'ALL' || t.transactionType.toUpperCase() === typeFilter;

      // Direction filter
      const matchesDirection = directionFilter === 'ALL' || t.transactionDirection === directionFilter;

      // Status filter
      const matchesStatus = statusFilter === 'ALL' || t.status === statusFilter;

      return matchesSearch && matchesType && matchesDirection && matchesStatus;
    });
  }, [transactions, searchTerm, typeFilter, directionFilter, statusFilter]);

  // Extract distinct transaction types for filter dropdown
  const distinctTypes = useMemo(() => {
    const types = new Set(transactions.map((t) => t.transactionType));
    return Array.from(types);
  }, [transactions]);

  return (
    <div className="auth-wrapper">
      <Header customerName={customerName} />

      <main className="main-content">
        <div className="page-header-row">
          <div className="page-title-block">
            <h1>My Transactions</h1>
            <p>View and manage your recent banking activity.</p>
          </div>
          <Link to="/dashboard" className="btn-secondary">
            <ArrowLeft size={16} />
            <span>Back to Dashboard</span>
          </Link>
        </div>

        {errorMessage && (
          <div className="alert-box alert-error">
            <AlertCircle size={18} />
            <span>{errorMessage}</span>
          </div>
        )}

        <div className="content-card">
          {/* Filter Controls Bar */}
          <div className="filter-controls-bar">
            {/* Search Input */}
            <div className="search-box-wrap">
              <Search size={18} className="search-icon" />
              <input
                type="text"
                className="search-input"
                placeholder="Search by description, merchant, location, or TXN ID..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>

            {/* Type Filter */}
            <select
              className="filter-select"
              value={typeFilter}
              onChange={(e) => setTypeFilter(e.target.value)}
              aria-label="Filter by type"
            >
              <option value="ALL">All Types</option>
              {distinctTypes.map((type) => (
                <option key={type} value={type}>
                  {type}
                </option>
              ))}
            </select>

            {/* Direction Filter */}
            <select
              className="filter-select"
              value={directionFilter}
              onChange={(e) => setDirectionFilter(e.target.value)}
              aria-label="Filter by direction"
            >
              <option value="ALL">All Flows</option>
              <option value="CREDIT">Money In (+ Credit)</option>
              <option value="DEBIT">Money Out (- Debit)</option>
            </select>

            {/* Status Filter */}
            <select
              className="filter-select"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              aria-label="Filter by status"
            >
              <option value="ALL">All Statuses</option>
              <option value="COMPLETED">Completed</option>
              <option value="PENDING">Pending</option>
              <option value="FAILED">Failed</option>
            </select>

            {/* Reset Filters / Reload button */}
            <button
              onClick={() => {
                setSearchTerm('');
                setTypeFilter('ALL');
                setDirectionFilter('ALL');
                setStatusFilter('ALL');
              }}
              className="btn-secondary btn-sm"
              title="Reset all filters"
            >
              <RefreshCw size={14} />
              <span>Reset</span>
            </button>
          </div>

          {/* Results count label */}
          <div style={{ fontSize: '0.8rem', color: '#64748b', marginBottom: '14px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span>
              Showing <strong>{filteredTransactions.length}</strong> of <strong>{transactions.length}</strong> transactions
            </span>
          </div>

          {isLoading ? (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '260px', gap: '12px' }}>
              <div className="spinner" style={{ borderColor: '#003366', borderTopColor: 'transparent', width: '30px', height: '30px', borderWidth: '3px' }}></div>
              <span style={{ fontSize: '0.85rem', color: '#64748b' }}>Retrieving account transactions...</span>
            </div>
          ) : filteredTransactions.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '48px 16px', color: '#64748b' }}>
              <Filter size={36} color="#cbd5e1" style={{ margin: '0 auto 12px' }} />
              <h3 style={{ fontSize: '1.05rem', color: '#334155', fontWeight: 600 }}>No matching transactions found</h3>
              <p style={{ fontSize: '0.82rem', marginTop: '4px' }}>
                Try adjusting your search query or reset your filters.
              </p>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="banking-table">
                <thead>
                  <tr>
                    <th>Transaction ID</th>
                    <th>Date & Time</th>
                    <th>Description</th>
                    <th>Type</th>
                    <th>Location</th>
                    <th>Amount</th>
                    <th>Status</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredTransactions.map((txn) => (
                    <tr key={txn.transactionId}>
                      <td>
                        <span className="txn-id-tag">{txn.transactionId}</span>
                      </td>
                      <td>
                        <span style={{ fontSize: '0.82rem', color: '#64748b' }}>
                          {txn.timestamp}
                        </span>
                      </td>
                      <td>
                        <div className="txn-desc-cell">
                          <span className="txn-desc-title">{txn.description}</span>
                          <span className="txn-desc-sub">{txn.recipient}</span>
                        </div>
                      </td>
                      <td>
                        <span className="type-pill">{txn.transactionType}</span>
                      </td>
                      <td>
                        <span style={{ fontSize: '0.82rem', color: '#475569' }}>
                          {txn.location}
                        </span>
                      </td>
                      <td className="amount-cell">
                        <span className={txn.transactionDirection === 'CREDIT' ? 'amount-credit' : 'amount-debit'}>
                          {txn.transactionDirection === 'CREDIT' ? '+' : '-'}
                          {formatCurrency(txn.amount)}
                        </span>
                      </td>
                      <td>
                        <span className={`status-badge status-${txn.status.toLowerCase()}`}>
                          {txn.status}
                        </span>
                      </td>
                      <td>
                        <Link
                          to={`/transactions/${txn.transactionId}`}
                          className="btn-secondary btn-sm"
                          style={{ padding: '5px 12px' }}
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
