import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Header } from '../components/Header';
import { Footer } from '../components/Footer';
import { getRememberedBankId, setRememberedBankId, clearRememberedBankId } from '../services/api';
import { Lock, Eye, EyeOff, AlertCircle } from 'lucide-react';

export const LoginPage: React.FC = () => {
  const { login, isAuthenticated, user } = useAuth();
  const navigate = useNavigate();

  const [bankId, setBankId] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [showPassword, setShowPassword] = useState<boolean>(false);
  const [rememberBankId, setRememberBankId] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);

  useEffect(() => {
    if (isAuthenticated) {
      if (user?.role === 'REVIEWER') {
        navigate('/review', { replace: true });
      } else {
        navigate('/dashboard', { replace: true });
      }
    }
    const savedBankId = getRememberedBankId();
    if (savedBankId) {
      setBankId(savedBankId);
      setRememberBankId(true);
    }
  }, [isAuthenticated, user, navigate]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage('');

    const trimmedBankId = bankId.trim();
    if (!trimmedBankId) {
      setErrorMessage('Bank ID cannot be empty.');
      return;
    }

    if (!password) {
      setErrorMessage('Password cannot be empty.');
      return;
    }

    setIsSubmitting(true);

    try {
      const result = await login(trimmedBankId, password);
      if (result.success) {
        if (rememberBankId) {
          setRememberedBankId(trimmedBankId);
        } else {
          clearRememberedBankId();
        }
        if (result.user?.role === 'REVIEWER') {
          navigate('/review', { replace: true });
        } else {
          navigate('/dashboard', { replace: true });
        }
      } else {
        setErrorMessage(result.message || 'Invalid Bank ID or password.');
      }
    } catch {
      setErrorMessage('Unable to connect to the server. Please verify the backend is running.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="auth-wrapper">
      <Header />

      <main className="main-content">
        <div className="auth-card">
          <div className="auth-card-header">
            <h2>SECUREBANK</h2>
            <p>Secure Digital Banking</p>
          </div>

          <div className="auth-card-body">
            {errorMessage && (
              <div className="alert-box alert-error" role="alert">
                <AlertCircle size={18} style={{ flexShrink: 0, marginTop: '1px' }} />
                <span>{errorMessage}</span>
              </div>
            )}

            <form onSubmit={handleSubmit} noValidate>
              <div className="form-group">
                <label className="form-label" htmlFor="bankId">Bank ID</label>
                <div className="input-container">
                  <input
                    id="bankId"
                    type="text"
                    className={`form-input ${errorMessage && !bankId ? 'has-error' : ''}`}
                    placeholder="Enter your Bank ID (e.g. SB100001)"
                    value={bankId}
                    onChange={(e) => {
                      setBankId(e.target.value);
                      if (errorMessage) setErrorMessage('');
                    }}
                    disabled={isSubmitting}
                    autoComplete="username"
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="password">Password</label>
                <div className="input-container">
                  <input
                    id="password"
                    type={showPassword ? 'text' : 'password'}
                    className={`form-input ${errorMessage && !password ? 'has-error' : ''}`}
                    placeholder="Enter your password"
                    value={password}
                    onChange={(e) => {
                      setPassword(e.target.value);
                      if (errorMessage) setErrorMessage('');
                    }}
                    disabled={isSubmitting}
                    autoComplete="current-password"
                  />
                  <button
                    type="button"
                    className="toggle-pwd-btn"
                    onClick={() => setShowPassword(!showPassword)}
                    aria-label={showPassword ? 'Hide password' : 'Show password'}
                    tabIndex={-1}
                  >
                    {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
              </div>

              <div className="form-options">
                <label className="checkbox-label">
                  <input
                    type="checkbox"
                    checked={rememberBankId}
                    onChange={(e) => setRememberBankId(e.target.checked)}
                    disabled={isSubmitting}
                  />
                  <span>Remember Bank ID</span>
                </label>
                <Link to="/forgot-password" className="forgot-password-link">
                  Forgot Password?
                </Link>
              </div>

              <button
                type="submit"
                className="btn-primary"
                disabled={isSubmitting}
              >
                {isSubmitting ? (
                  <>
                    <span className="spinner"></span>
                    <span>AUTHENTICATING...</span>
                  </>
                ) : (
                  'LOGIN'
                )}
              </button>
            </form>
          </div>

          {/* Quick Demo Credentials Panel for College Presentation */}
          <div style={{
            margin: '0.5rem 2rem 1.25rem',
            padding: '0.85rem 1rem',
            backgroundColor: '#f8fafc',
            border: '1px dashed #cbd5e1',
            borderRadius: '8px',
            fontSize: '0.82rem'
          }}>
            <div style={{ fontWeight: 600, color: '#334155', marginBottom: '6px' }}>Quick Demo Fill:</div>
            <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
              <button
                type="button"
                onClick={() => { setBankId('SB100001'); setPassword('Demo@12345'); }}
                style={{
                  backgroundColor: '#e0f2fe',
                  color: '#0369a1',
                  border: '1px solid #bae6fd',
                  borderRadius: '4px',
                  padding: '4px 8px',
                  cursor: 'pointer',
                  fontSize: '0.75rem',
                  fontWeight: 600
                }}
              >
                Customer: SB100001
              </button>
              <button
                type="button"
                onClick={() => { setBankId('REV100001'); setPassword('Review@12345'); }}
                style={{
                  backgroundColor: '#f3e8ff',
                  color: '#6b21a8',
                  border: '1px solid #e9d5ff',
                  borderRadius: '4px',
                  padding: '4px 8px',
                  cursor: 'pointer',
                  fontSize: '0.75rem',
                  fontWeight: 600
                }}
              >
                Reviewer: REV100001
              </button>
            </div>
          </div>

          <div className="auth-card-footer">
            <Lock size={14} />
            <span>🔒 Protected by Secure Authentication</span>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
};
