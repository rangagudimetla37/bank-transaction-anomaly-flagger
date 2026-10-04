import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../services/api';
import type { SecuritySummary } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { Header } from '../components/Header';
import { Footer } from '../components/Footer';
import {
  ShieldCheck,
  Lock,
  KeyRound,
  Clock,
  CheckCircle2,
  AlertCircle,
  Eye,
  EyeOff,
  Shield,
  ArrowLeft,
  Check
} from 'lucide-react';

export const SecurityPage: React.FC = () => {
  const { user } = useAuth();
  const [securityData, setSecurityData] = useState<SecuritySummary | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [errorMessage, setErrorMessage] = useState<string>('');

  // Password Change Form State
  const [currentPassword, setCurrentPassword] = useState<string>('');
  const [newPassword, setNewPassword] = useState<string>('');
  const [confirmPassword, setConfirmPassword] = useState<string>('');
  const [showCurrentPassword, setShowCurrentPassword] = useState<boolean>(false);
  const [showNewPassword, setShowNewPassword] = useState<boolean>(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState<boolean>(false);

  const [isChangingPassword, setIsChangingPassword] = useState<boolean>(false);
  const [changePasswordSuccess, setChangePasswordSuccess] = useState<string>('');
  const [changePasswordError, setChangePasswordError] = useState<string>('');

  // Password Policy Validation Indicators
  const hasMinLength = newPassword.length >= 8;
  const hasUppercase = /[A-Z]/.test(newPassword);
  const hasLowercase = /[a-z]/.test(newPassword);
  const hasNumber = /[0-9]/.test(newPassword);
  const passwordsMatch = newPassword.length > 0 && newPassword === confirmPassword;
  const isFormValid = currentPassword.length > 0 && hasMinLength && hasUppercase && hasLowercase && hasNumber && passwordsMatch;

  const fetchSecurityInfo = async () => {
    try {
      const data = await api.getSecurityActivity();
      setSecurityData(data);
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to retrieve security activity.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchSecurityInfo();
  }, []);

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    setChangePasswordSuccess('');
    setChangePasswordError('');

    if (!currentPassword) {
      setChangePasswordError('Please enter your current password.');
      return;
    }
    if (!isFormValid) {
      setChangePasswordError('New password must satisfy all security policy requirements.');
      return;
    }

    setIsChangingPassword(true);
    try {
      const res = await api.changePassword(currentPassword, newPassword, confirmPassword);
      if (res.success) {
        setChangePasswordSuccess('Password changed successfully.');
        setCurrentPassword('');
        setNewPassword('');
        setConfirmPassword('');
        // Refresh security activity list
        fetchSecurityInfo();
      } else {
        setChangePasswordError(res.message || 'Password change failed.');
      }
    } catch (err: any) {
      setChangePasswordError(err.message || 'Error occurred while updating password.');
    } finally {
      setIsChangingPassword(false);
    }
  };

  const getEventBadgeClass = (eventType: string) => {
    const type = eventType.toUpperCase();
    if (type.includes('SUCCESS') || type === 'VERIFIED') return 'risk-badge-normal';
    if (type.includes('FAILURE') || type.includes('BLOCKED') || type.includes('DENIED')) return 'risk-badge-high_risk';
    if (type.includes('RESET') || type.includes('CHANGED')) return 'risk-badge-watch';
    return 'risk-badge-watch';
  };

  const backLink = user?.role === 'REVIEWER' ? '/review' : '/dashboard';
  const backLabel = user?.role === 'REVIEWER' ? 'Back to Review Portal' : 'Back to Dashboard';

  return (
    <div className="auth-wrapper">
      <Header />

      <main className="main-content" style={{ maxWidth: '960px' }}>
        <div className="page-header-row">
          <div className="page-title-block">
            <h1>Security Center</h1>
            <p>Your account security overview, credential controls, and audit logs.</p>
          </div>
          <Link to={backLink} className="btn-secondary" style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
            <ArrowLeft size={16} />
            <span>{backLabel}</span>
          </Link>
        </div>

        {errorMessage && (
          <div className="alert-box alert-error" style={{ marginBottom: '20px' }}>
            <AlertCircle size={18} />
            <span>{errorMessage}</span>
          </div>
        )}

        {isLoading ? (
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '300px', gap: '12px' }}>
            <div className="spinner" style={{ borderColor: '#003366', borderTopColor: 'transparent', width: '32px', height: '32px', borderWidth: '3px' }}></div>
            <span style={{ fontSize: '0.85rem', color: '#64748b' }}>Retrieving account security diagnostics...</span>
          </div>
        ) : (
          <>
            {/* SECTION 1: SECURITY STATUS */}
            <div className="metric-cards-grid" style={{ marginBottom: '24px' }}>
              <div className="metric-card">
                <div className="metric-card-top">
                  <span className="metric-label">Password Storage</span>
                  <div className="metric-icon-wrap metric-icon-green">
                    <ShieldCheck size={18} />
                  </div>
                </div>
                <div className="metric-value" style={{ fontSize: '1.25rem', color: '#16a34a' }}>
                  Protected
                </div>
                <div className="metric-subtext">
                  BCrypt (Cost 12 Salted Hash)
                </div>
              </div>

              <div className="metric-card">
                <div className="metric-card-top">
                  <span className="metric-label">Authentication</span>
                  <div className="metric-icon-wrap metric-icon-blue">
                    <Lock size={18} />
                  </div>
                </div>
                <div className="metric-value" style={{ fontSize: '1.25rem', color: '#003366' }}>
                  Active
                </div>
                <div className="metric-subtext">
                  Session Token: Validated
                </div>
              </div>

              <div className="metric-card">
                <div className="metric-card-top">
                  <span className="metric-label">Account Protection</span>
                  <div className="metric-icon-wrap metric-icon-purple">
                    <Shield size={18} />
                  </div>
                </div>
                <div className="metric-value" style={{ fontSize: '1.25rem', color: '#7c3aed' }}>
                  Enabled
                </div>
                <div className="metric-subtext">
                  RBAC &amp; Object Authorization
                </div>
              </div>

              <div className="metric-card">
                <div className="metric-card-top">
                  <span className="metric-label">Recent Activity</span>
                  <div className="metric-icon-wrap metric-icon-green">
                    <Clock size={18} />
                  </div>
                </div>
                <div className="metric-value" style={{ fontSize: '1.25rem' }}>
                  {securityData?.activities?.length ?? 0} Events
                </div>
                <div className="metric-subtext">
                  Recorded in Audit Trail
                </div>
              </div>
            </div>

            {/* SECTION 2: PASSWORD & AUTHENTICATION (CHANGE PASSWORD) */}
            <div className="content-card" style={{ marginBottom: '24px' }}>
              <div className="card-title-row" style={{ borderBottom: '1px solid #e2e8f0', paddingBottom: '12px', marginBottom: '18px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <KeyRound size={20} color="#003366" />
                  <h2 style={{ fontSize: '1.15rem', color: '#003366', margin: 0, fontWeight: 700 }}>
                    Password &amp; Authentication
                  </h2>
                </div>
                <span style={{ fontSize: '0.8rem', color: '#64748b' }}>
                  Role: <strong style={{ color: '#003366' }}>{user?.role}</strong>
                </span>
              </div>

              {changePasswordSuccess && (
                <div style={{ padding: '12px 16px', backgroundColor: '#f0fdf4', border: '1px solid #bbf7d0', borderRadius: '8px', color: '#166534', fontSize: '0.88rem', display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
                  <CheckCircle2 size={18} color="#16a34a" />
                  <span>{changePasswordSuccess}</span>
                </div>
              )}

              {changePasswordError && (
                <div style={{ padding: '12px 16px', backgroundColor: '#fef2f2', border: '1px solid #fecaca', borderRadius: '8px', color: '#991b1b', fontSize: '0.88rem', display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
                  <AlertCircle size={18} color="#dc2626" />
                  <span>{changePasswordError}</span>
                </div>
              )}

              <form onSubmit={handleChangePassword} style={{ maxWidth: '600px' }}>
                {/* Current Password */}
                <div className="form-group">
                  <label className="form-label" htmlFor="currentPassword">Current Password</label>
                  <div className="input-container">
                    <input
                      id="currentPassword"
                      type={showCurrentPassword ? 'text' : 'password'}
                      className="form-input"
                      placeholder="Enter your current password"
                      value={currentPassword}
                      onChange={(e) => setCurrentPassword(e.target.value)}
                      disabled={isChangingPassword}
                      autoComplete="current-password"
                    />
                    <button
                      type="button"
                      className="toggle-pwd-btn"
                      onClick={() => setShowCurrentPassword(!showCurrentPassword)}
                      aria-label="Toggle current password visibility"
                      tabIndex={-1}
                    >
                      {showCurrentPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </div>
                </div>

                {/* New Password */}
                <div className="form-group">
                  <label className="form-label" htmlFor="newPassword">New Password</label>
                  <div className="input-container">
                    <input
                      id="newPassword"
                      type={showNewPassword ? 'text' : 'password'}
                      className="form-input"
                      placeholder="Create a new secure password"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      disabled={isChangingPassword}
                      autoComplete="new-password"
                    />
                    <button
                      type="button"
                      className="toggle-pwd-btn"
                      onClick={() => setShowNewPassword(!showNewPassword)}
                      aria-label="Toggle new password visibility"
                      tabIndex={-1}
                    >
                      {showNewPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </div>
                </div>

                {/* Confirm New Password */}
                <div className="form-group">
                  <label className="form-label" htmlFor="confirmPassword">Confirm New Password</label>
                  <div className="input-container">
                    <input
                      id="confirmPassword"
                      type={showConfirmPassword ? 'text' : 'password'}
                      className="form-input"
                      placeholder="Re-enter new password"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      disabled={isChangingPassword}
                      autoComplete="new-password"
                    />
                    <button
                      type="button"
                      className="toggle-pwd-btn"
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      aria-label="Toggle confirm password visibility"
                      tabIndex={-1}
                    >
                      {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </div>
                </div>

                {/* Password Policy Requirements Checklist */}
                <div style={{ backgroundColor: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '8px', padding: '12px 16px', marginBottom: '18px' }}>
                  <div style={{ fontSize: '0.8rem', fontWeight: 700, color: '#334155', marginBottom: '8px' }}>
                    Password must contain:
                  </div>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '6px', fontSize: '0.8rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: hasMinLength ? '#15803d' : '#64748b' }}>
                      <Check size={14} color={hasMinLength ? '#16a34a' : '#cbd5e1'} strokeWidth={3} />
                      <span>At least 8 characters</span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: hasUppercase ? '#15803d' : '#64748b' }}>
                      <Check size={14} color={hasUppercase ? '#16a34a' : '#cbd5e1'} strokeWidth={3} />
                      <span>One uppercase letter (A-Z)</span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: hasLowercase ? '#15803d' : '#64748b' }}>
                      <Check size={14} color={hasLowercase ? '#16a34a' : '#cbd5e1'} strokeWidth={3} />
                      <span>One lowercase letter (a-z)</span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: hasNumber ? '#15803d' : '#64748b' }}>
                      <Check size={14} color={hasNumber ? '#16a34a' : '#cbd5e1'} strokeWidth={3} />
                      <span>One number (0-9)</span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: passwordsMatch ? '#15803d' : '#64748b' }}>
                      <Check size={14} color={passwordsMatch ? '#16a34a' : '#cbd5e1'} strokeWidth={3} />
                      <span>Passwords match</span>
                    </div>
                  </div>
                </div>

                <button
                  type="submit"
                  className="btn-primary"
                  disabled={isChangingPassword || !isFormValid}
                  style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}
                >
                  <Lock size={15} />
                  <span>{isChangingPassword ? 'Updating Password...' : 'Change Password'}</span>
                </button>
              </form>
            </div>

            {/* SECTION 3: RECENT SECURITY ACTIVITY */}
            <div className="content-card" style={{ marginBottom: '24px' }}>
              <div className="card-title-row" style={{ borderBottom: '1px solid #e2e8f0', paddingBottom: '12px', marginBottom: '14px' }}>
                <div>
                  <h3 style={{ fontSize: '1.15rem', color: '#003366', margin: 0, fontWeight: 700 }}>
                    Recent Security Activity
                  </h3>
                  <p style={{ fontSize: '0.8rem', color: '#64748b', marginTop: '2px' }}>
                    Audit trail of recent authentication, authorization, and password events for your account
                  </p>
                </div>
              </div>

              {securityData?.activities && securityData.activities.length > 0 ? (
                <div className="table-responsive">
                  <table className="banking-table">
                    <thead>
                      <tr>
                        <th>Date / Time</th>
                        <th>Event</th>
                        <th>Description</th>
                        <th>Origin</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {securityData.activities.map((act) => (
                        <tr key={act.id}>
                          <td style={{ fontSize: '0.8rem', color: '#64748b', whiteSpace: 'nowrap' }}>
                            {act.timestamp}
                          </td>
                          <td>
                            <span className={`risk-badge ${getEventBadgeClass(act.activityType)}`} style={{ fontSize: '0.74rem', padding: '3px 8px' }}>
                              {act.activityType}
                            </span>
                          </td>
                          <td style={{ fontWeight: 500, color: '#1e293b', fontSize: '0.84rem' }}>
                            {act.description}
                          </td>
                          <td style={{ fontFamily: 'monospace', fontSize: '0.78rem', color: '#64748b' }}>
                            {act.ipAddress}
                          </td>
                          <td>
                            <span style={{ fontSize: '0.76rem', fontWeight: 700, color: act.activityType.includes('FAIL') || act.activityType.includes('DENIED') ? '#dc2626' : '#16a34a' }}>
                              {act.activityType.includes('FAIL') || act.activityType.includes('DENIED') ? 'ALERT' : 'RECORDED'}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <div style={{ padding: '24px', textAlign: 'center', color: '#64748b', fontSize: '0.85rem' }}>
                  No security events recorded yet.
                </div>
              )}
            </div>

            {/* SECTION 4: SECURITY RECOMMENDATIONS */}
            <div className="content-card">
              <div className="card-title-row" style={{ borderBottom: '1px solid #e2e8f0', paddingBottom: '12px', marginBottom: '14px' }}>
                <h3 style={{ fontSize: '1.15rem', color: '#003366', margin: 0, fontWeight: 700 }}>
                  Security Recommendations
                </h3>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '14px' }}>
                <div style={{ padding: '14px', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                  <div style={{ fontWeight: 700, color: '#003366', fontSize: '0.88rem', marginBottom: '4px' }}>
                    • Use a Strong Password
                  </div>
                  <p style={{ fontSize: '0.8rem', color: '#475569', margin: 0 }}>
                    Select a unique combination of uppercase, lowercase letters, numbers, and symbols. Avoid common words or personal birthdays.
                  </p>
                </div>

                <div style={{ padding: '14px', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                  <div style={{ fontWeight: 700, color: '#003366', fontSize: '0.88rem', marginBottom: '4px' }}>
                    • Never Share Credentials
                  </div>
                  <p style={{ fontSize: '0.8rem', color: '#475569', margin: 0 }}>
                    Keep your Bank ID, password, and security verification PIN confidential. Bank staff will never request your login password.
                  </p>
                </div>

                <div style={{ padding: '14px', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                  <div style={{ fontWeight: 700, color: '#003366', fontSize: '0.88rem', marginBottom: '4px' }}>
                    • Review Transaction Alerts
                  </div>
                  <p style={{ fontSize: '0.8rem', color: '#475569', margin: 0 }}>
                    Check your alerts dashboard regularly for unusual transaction signals, outstation locations, or high-velocity retry bursts.
                  </p>
                </div>

                <div style={{ padding: '14px', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                  <div style={{ fontWeight: 700, color: '#003366', fontSize: '0.88rem', marginBottom: '4px' }}>
                    • Log Out on Shared Devices
                  </div>
                  <p style={{ fontSize: '0.8rem', color: '#475569', margin: 0 }}>
                    Always click the LOGOUT button when working on college laboratory or shared workstations to revoke the active session token.
                  </p>
                </div>

                <div style={{ padding: '14px', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                  <div style={{ fontWeight: 700, color: '#003366', fontSize: '0.88rem', marginBottom: '4px' }}>
                    • Report Suspicious Activity
                  </div>
                  <p style={{ fontSize: '0.8rem', color: '#475569', margin: 0 }}>
                    If you detect an unrecognized transaction or repeated failed login alerts in your security audit trail, contact bank security immediately.
                  </p>
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
