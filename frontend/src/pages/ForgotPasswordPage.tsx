import React, { useState, useEffect } from 'react';
import { Link, useLocation, useSearchParams } from 'react-router-dom';
import { api } from '../services/api';
import { Header } from '../components/Header';
import { Footer } from '../components/Footer';
import { ShieldAlert, CheckCircle2, AlertCircle, Eye, EyeOff, Lock, ArrowLeft } from 'lucide-react';

export const ForgotPasswordPage: React.FC = () => {
  const location = useLocation();
  const [searchParams] = useSearchParams();

  // Step 1: Verification, Step 2: New Password, Step 3: Success
  const [step, setStep] = useState<'verify' | 'reset' | 'success'>('verify');

  // Step 1 fields
  const [bankId, setBankId] = useState<string>('');
  const [securityPin, setSecurityPin] = useState<string>('');

  // Step 2 fields
  const [resetToken, setResetToken] = useState<string>('');
  const [newPassword, setNewPassword] = useState<string>('');
  const [confirmPassword, setConfirmPassword] = useState<string>('');
  const [showNewPassword, setShowNewPassword] = useState<boolean>(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState<boolean>(false);

  useEffect(() => {
    const qToken = searchParams.get('token');
    const qBankId = searchParams.get('bankId');
    if (qToken) {
      setResetToken(qToken);
      if (qBankId) setBankId(qBankId);
      setStep('reset');
    } else if (location.pathname === '/reset-password') {
      setStep('reset');
    }
  }, [location.pathname, searchParams]);

  // Status & messages
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);

  // Password policy live checks
  const hasMinLength = newPassword.length >= 8;
  const hasUppercase = /[A-Z]/.test(newPassword);
  const hasLowercase = /[a-z]/.test(newPassword);
  const hasNumber = /[0-9]/.test(newPassword);
  const passwordsMatch = newPassword.length > 0 && newPassword === confirmPassword;

  const handleVerifySubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage('');

    const trimmedBankId = bankId.trim();
    const trimmedPin = securityPin.trim();

    if (!trimmedBankId) {
      setErrorMessage('Bank ID cannot be empty.');
      return;
    }
    if (!trimmedPin) {
      setErrorMessage('Security Verification PIN cannot be empty.');
      return;
    }

    setIsSubmitting(true);
    try {
      const result = await api.verifyForgotPassword(trimmedBankId, trimmedPin);
      if (result.success && result.resetToken) {
        setResetToken(result.resetToken);
        setStep('reset');
      } else {
        setErrorMessage(result.message || 'Identity verification failed. Please check your Bank ID and verification PIN.');
      }
    } catch {
      setErrorMessage('Identity verification failed. Please check your Bank ID and verification PIN.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleResetSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage('');

    if (!newPassword) {
      setErrorMessage('New password cannot be empty.');
      return;
    }

    if (!hasMinLength || !hasUppercase || !hasLowercase || !hasNumber) {
      setErrorMessage('Password must meet all complexity requirements listed below.');
      return;
    }

    if (newPassword !== confirmPassword) {
      setErrorMessage('Passwords do not match.');
      return;
    }

    setIsSubmitting(true);
    try {
      const result = await api.resetPassword(resetToken, bankId.trim(), newPassword, confirmPassword);
      if (result.success) {
        setStep('success');
      } else {
        setErrorMessage(result.message || 'Password reset failed. Please try again.');
      }
    } catch {
      setErrorMessage('Password reset failed. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="auth-wrapper">
      <Header />

      <main className="main-content">
        <div className="auth-card" style={{ maxWidth: '480px' }}>
          <div className="auth-card-header">
            <h2>Forgot Password?</h2>
            <p>Your existing password cannot be recovered. You can only create a new password after identity verification.</p>
          </div>

          <div className="auth-card-body">
            {errorMessage && (
              <div className="alert-box alert-error" role="alert">
                <AlertCircle size={18} style={{ flexShrink: 0, marginTop: '1px' }} />
                <span>{errorMessage}</span>
              </div>
            )}

            {/* STEP 1: IDENTITY VERIFICATION */}
            {step === 'verify' && (
              <form onSubmit={handleVerifySubmit} noValidate>
                <div className="alert-box alert-info">
                  <ShieldAlert size={18} style={{ flexShrink: 0, marginTop: '2px' }} />
                  <span>
                    For prototype demonstration, enter your registered Bank ID and demo verification PIN.
                    <br />
                    <strong>Demo PIN: 778899</strong>
                  </span>
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="verifyBankId">Bank ID</label>
                  <input
                    id="verifyBankId"
                    type="text"
                    className="form-input"
                    placeholder="e.g. SB100001"
                    value={bankId}
                    onChange={(e) => {
                      setBankId(e.target.value);
                      if (errorMessage) setErrorMessage('');
                    }}
                    disabled={isSubmitting}
                    autoComplete="username"
                  />
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="verifyPin">Demo Security Verification PIN</label>
                  <input
                    id="verifyPin"
                    type="password"
                    className="form-input"
                    placeholder="Enter Security PIN (Demo: 778899)"
                    value={securityPin}
                    onChange={(e) => {
                      setSecurityPin(e.target.value);
                      if (errorMessage) setErrorMessage('');
                    }}
                    disabled={isSubmitting}
                  />
                </div>

                <button
                  type="submit"
                  className="btn-primary"
                  disabled={isSubmitting}
                >
                  {isSubmitting ? (
                    <>
                      <span className="spinner"></span>
                      <span>VERIFYING IDENTITY...</span>
                    </>
                  ) : (
                    'VERIFY IDENTITY'
                  )}
                </button>

                <div style={{ marginTop: '16px', textAlign: 'center' }}>
                  <Link to="/login" style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', fontSize: '0.82rem' }}>
                    <ArrowLeft size={14} /> Back to Login
                  </Link>
                </div>
              </form>
            )}

            {/* STEP 2: CREATE NEW PASSWORD */}
            {step === 'reset' && (
              <form onSubmit={handleResetSubmit} noValidate>
                <div className="form-group">
                  <label className="form-label" htmlFor="resetBankId">Bank ID</label>
                  <input
                    id="resetBankId"
                    type="text"
                    className="form-input"
                    placeholder="Enter your Bank ID (e.g. SB100001)"
                    value={bankId}
                    onChange={(e) => setBankId(e.target.value)}
                    disabled={isSubmitting}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="resetTokenField">Single-Use Reset Token (15-min expiry)</label>
                  <input
                    id="resetTokenField"
                    type="text"
                    className="form-input"
                    placeholder="Enter or paste your 64-char reset token"
                    value={resetToken}
                    onChange={(e) => setResetToken(e.target.value)}
                    disabled={isSubmitting}
                    style={{ fontFamily: 'monospace', fontSize: '0.8rem' }}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="newPassword">Create New Password</label>
                  <div className="input-container">
                    <input
                      id="newPassword"
                      type={showNewPassword ? 'text' : 'password'}
                      className="form-input"
                      placeholder="Enter new strong password"
                      value={newPassword}
                      onChange={(e) => {
                        setNewPassword(e.target.value);
                        if (errorMessage) setErrorMessage('');
                      }}
                      disabled={isSubmitting}
                      autoComplete="new-password"
                    />
                    <button
                      type="button"
                      className="toggle-pwd-btn"
                      onClick={() => setShowNewPassword(!showNewPassword)}
                      aria-label="Toggle password visibility"
                      tabIndex={-1}
                    >
                      {showNewPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="confirmPassword">Confirm New Password</label>
                  <div className="input-container">
                    <input
                      id="confirmPassword"
                      type={showConfirmPassword ? 'text' : 'password'}
                      className="form-input"
                      placeholder="Re-enter new password"
                      value={confirmPassword}
                      onChange={(e) => {
                        setConfirmPassword(e.target.value);
                        if (errorMessage) setErrorMessage('');
                      }}
                      disabled={isSubmitting}
                      autoComplete="new-password"
                    />
                    <button
                      type="button"
                      className="toggle-pwd-btn"
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      aria-label="Toggle password visibility"
                      tabIndex={-1}
                    >
                      {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </div>
                </div>

                {/* Password Policy Indicator */}
                <div className="password-policy-card">
                  <div className="policy-title">Password Security Requirements:</div>
                  <ul className="policy-list">
                    <li className={`policy-item ${hasMinLength ? 'valid' : ''}`}>
                      <CheckCircle2 size={13} color={hasMinLength ? '#16a34a' : '#94a3b8'} />
                      <span>At least 8 characters</span>
                    </li>
                    <li className={`policy-item ${hasUppercase ? 'valid' : ''}`}>
                      <CheckCircle2 size={13} color={hasUppercase ? '#16a34a' : '#94a3b8'} />
                      <span>At least one uppercase letter (A-Z)</span>
                    </li>
                    <li className={`policy-item ${hasLowercase ? 'valid' : ''}`}>
                      <CheckCircle2 size={13} color={hasLowercase ? '#16a34a' : '#94a3b8'} />
                      <span>At least one lowercase letter (a-z)</span>
                    </li>
                    <li className={`policy-item ${hasNumber ? 'valid' : ''}`}>
                      <CheckCircle2 size={13} color={hasNumber ? '#16a34a' : '#94a3b8'} />
                      <span>At least one number (0-9)</span>
                    </li>
                    <li className={`policy-item ${passwordsMatch ? 'valid' : ''}`}>
                      <CheckCircle2 size={13} color={passwordsMatch ? '#16a34a' : '#94a3b8'} />
                      <span>Passwords match</span>
                    </li>
                  </ul>
                </div>

                <button
                  type="submit"
                  className="btn-primary"
                  disabled={isSubmitting || !hasMinLength || !hasUppercase || !hasLowercase || !hasNumber || !passwordsMatch}
                >
                  {isSubmitting ? (
                    <>
                      <span className="spinner"></span>
                      <span>UPDATING PASSWORD...</span>
                    </>
                  ) : (
                    'CHANGE PASSWORD'
                  )}
                </button>

                <div style={{ marginTop: '16px', textAlign: 'center' }}>
                  <button
                    type="button"
                    className="btn-secondary"
                    onClick={() => {
                      setStep('verify');
                      setErrorMessage('');
                    }}
                    disabled={isSubmitting}
                  >
                    Back to Verification
                  </button>
                </div>
              </form>
            )}

            {/* STEP 3: SUCCESS */}
            {step === 'success' && (
              <div style={{ textAlign: 'center', padding: '12px 0' }}>
                <div style={{ display: 'inline-flex', padding: '12px', background: '#f0fdf4', borderRadius: '50%', color: '#16a34a', marginBottom: '16px' }}>
                  <CheckCircle2 size={36} />
                </div>
                <h3 style={{ fontSize: '1.2rem', color: '#003366', fontWeight: 700, marginBottom: '8px' }}>
                  Password Changed Successfully
                </h3>
                <p style={{ fontSize: '0.85rem', color: '#475569', marginBottom: '24px', lineHeight: 1.5 }}>
                  Password changed successfully. Please log in with your new password.
                  <br />
                  <span style={{ fontSize: '0.78rem', color: '#64748b' }}>
                    Your previous password has been permanently invalidated.
                  </span>
                </p>
                <Link to="/login" className="btn-primary" style={{ textDecoration: 'none' }}>
                  PROCEED TO LOGIN
                </Link>
              </div>
            )}
          </div>

          <div className="auth-card-footer">
            <Lock size={14} />
            <span>🔒 Secure Password Replacement Only</span>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
};
