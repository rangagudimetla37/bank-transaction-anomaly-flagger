import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../services/api';
import type { AccountData } from '../services/api';
import { Header } from '../components/Header';
import { Footer } from '../components/Footer';
import {
  User,
  ShieldCheck,
  CreditCard,
  Building,
  KeyRound,
  Calendar,
  Lock,
  ArrowRight,
  AlertCircle
} from 'lucide-react';

export const AccountPage: React.FC = () => {
  const [account, setAccount] = useState<AccountData | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [errorMessage, setErrorMessage] = useState<string>('');

  useEffect(() => {
    const fetchAccount = async () => {
      try {
        const data = await api.getAccount();
        setAccount(data);
      } catch (err: any) {
        setErrorMessage(err.message || 'Failed to retrieve account details.');
      } finally {
        setIsLoading(false);
      }
    };
    fetchAccount();
  }, []);

  const formatCurrency = (val: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2,
    }).format(val);
  };

  return (
    <div className="auth-wrapper">
      <Header customerName={account?.customerName} />

      <main className="main-content" style={{ maxWidth: '880px' }}>
        <div className="page-header-row">
          <div className="page-title-block">
            <h1>My Account</h1>
            <p>Customer profile, account details, and security configuration.</p>
          </div>
          <div className="security-badge-top">
            <ShieldCheck size={16} />
            <span>KYC Verified Account</span>
          </div>
        </div>

        {errorMessage && (
          <div className="alert-box alert-error">
            <AlertCircle size={18} />
            <span>{errorMessage}</span>
          </div>
        )}

        {isLoading ? (
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '300px', gap: '12px' }}>
            <div className="spinner" style={{ borderColor: '#003366', borderTopColor: 'transparent', width: '32px', height: '32px', borderWidth: '3px' }}></div>
            <span style={{ fontSize: '0.85rem', color: '#64748b' }}>Loading customer profile...</span>
          </div>
        ) : (
          <>
            {/* Account Overview Card Banner */}
            <div className="account-overview-card">
              <div className="acc-card-info">
                <span style={{ fontSize: '0.75rem', letterSpacing: '1px', textTransform: 'uppercase', color: '#bfdbfe' }}>
                  SecureBank Digital Savings
                </span>
                <h3>{account?.customerName || 'Rajesh Sharma'}</h3>
                <div className="acc-card-number">
                  {account?.accountNumber || 'XXXX XXXX 0001'}
                </div>
              </div>

              <div className="acc-card-balance-block">
                <div className="acc-bal-label">Available Balance</div>
                <div className="acc-bal-value">
                  {formatCurrency(account?.balance ?? 84250)}
                </div>
              </div>
            </div>

            {/* Account Information Details Grid */}
            <div className="content-card">
              <div className="card-title-row">
                <h3>Account Information</h3>
                <span className="status-badge status-completed">Active Status</span>
              </div>

              <div className="detail-grid" style={{ marginTop: '10px' }}>
                <div className="detail-item">
                  <div className="detail-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <User size={14} />
                    <span>Customer Name</span>
                  </div>
                  <div className="detail-value">{account?.customerName || 'Rajesh Sharma'}</div>
                </div>

                <div className="detail-item">
                  <div className="detail-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <ShieldCheck size={14} />
                    <span>Bank ID</span>
                  </div>
                  <div className="detail-value" style={{ fontFamily: 'monospace' }}>
                    {account?.bankId || 'SB100001'}
                  </div>
                </div>

                <div className="detail-item">
                  <div className="detail-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <CreditCard size={14} />
                    <span>Account Number</span>
                  </div>
                  <div className="detail-value" style={{ fontFamily: 'monospace' }}>
                    {account?.accountNumber || 'XXXX XXXX 0001'}
                  </div>
                </div>

                <div className="detail-item">
                  <div className="detail-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Building size={14} />
                    <span>Account Type</span>
                  </div>
                  <div className="detail-value">{account?.accountType || 'Savings Account'}</div>
                </div>

                <div className="detail-item">
                  <div className="detail-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Calendar size={14} />
                    <span>Account Created Date</span>
                  </div>
                  <div className="detail-value">{account?.createdAt || '15 Jan 2025'}</div>
                </div>

                <div className="detail-item">
                  <div className="detail-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Building size={14} />
                    <span>Branch & IFSC</span>
                  </div>
                  <div className="detail-value">Hyderabad Banjara (SECB0001001)</div>
                </div>
              </div>
            </div>

            {/* Account Security Settings Card */}
            <div className="content-card">
              <div className="card-title-row">
                <h3>Account Security</h3>
                <span className="security-badge-top" style={{ padding: '3px 8px' }}>
                  <Lock size={12} />
                  <span>BCrypt Protected</span>
                </span>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', marginTop: '10px' }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '16px', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0', flexWrap: 'wrap', gap: '12px' }}>
                  <div>
                    <div style={{ fontWeight: 600, fontSize: '0.92rem', color: '#1e293b', display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <KeyRound size={16} color="#003366" />
                      <span>Account Login Password</span>
                    </div>
                    <div style={{ fontSize: '0.8rem', color: '#64748b', marginTop: '4px' }}>
                      Password: <span style={{ letterSpacing: '3px', fontWeight: 700, color: '#334155' }}>••••••••</span> (Stored as salted cryptographic hash)
                    </div>
                  </div>

                  <Link to="/forgot-password" className="btn-primary" style={{ padding: '8px 16px', fontSize: '0.82rem' }}>
                    <span>Change Password</span>
                    <ArrowRight size={14} />
                  </Link>
                </div>

                <div style={{ padding: '12px 16px', backgroundColor: '#eef5fc', borderRadius: '8px', border: '1px solid #d0e2f5', fontSize: '0.8rem', color: '#003366', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Lock size={15} />
                  <span>
                    Passwords cannot be viewed or retrieved. Password changes require two-step demo identity verification.
                  </span>
                </div>
              </div>
            </div>
          </>
        )}
      </main>

      <Footer />
    </div>
  );
};
