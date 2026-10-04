import React, { useState, useEffect } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { api } from '../services/api';
import { ShieldCheck, Bell, LogOut, User, Menu, X, ShieldAlert, FolderSearch, Share2 } from 'lucide-react';

interface HeaderProps {
  customerName?: string;
}

export const Header: React.FC<HeaderProps> = ({ customerName }) => {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const [newAlertCount, setNewAlertCount] = useState<number>(0);
  const [newCaseCount, setNewCaseCount] = useState<number>(0);
  const [mobileMenuOpen, setMobileMenuOpen] = useState<boolean>(false);

  const isReviewer = user?.role === 'REVIEWER';

  useEffect(() => {
    if (isAuthenticated) {
      if (isReviewer) {
        api.getReviewStats()
          .then((stats) => {
            if (stats && stats.newCases !== undefined) {
              setNewCaseCount(stats.newCases);
            }
          })
          .catch(() => {});
      } else {
        api.getAlertSummary()
          .then((res) => {
            if (res && res.newAlerts !== undefined) {
              setNewAlertCount(res.newAlerts);
            }
          })
          .catch(() => {});
      }
    }
  }, [isAuthenticated, isReviewer]);

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  const displayName = isReviewer ? 'SecOps Reviewer' : (customerName || 'Rajesh Sharma');
  const defaultHome = isReviewer ? '/review' : '/dashboard';

  return (
    <header className="top-banking-header">
      <div className="header-left">
        <div className="brand-container" onClick={() => navigate(defaultHome)} style={{ cursor: 'pointer' }}>
          <div className="brand-icon">
            <ShieldCheck size={22} color="#003366" />
          </div>
          <div className="brand-text-block">
            <span className="brand-title">SECUREBANK</span>
            <span className="brand-subtitle">{isReviewer ? 'Anomaly Review Portal' : 'Secure Digital Banking'}</span>
          </div>
        </div>

        {isAuthenticated && (
          <nav className="desktop-nav">
            {isReviewer ? (
              // REVIEWER NAVIGATION
              <>
                <NavLink to="/review" end className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                  Review Dashboard
                </NavLink>
                <NavLink to="/review/cases" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`} style={{ position: 'relative' }}>
                  <FolderSearch size={15} style={{ marginRight: '5px', verticalAlign: 'middle' }} />
                  Investigation Cases
                  {newCaseCount > 0 && (
                    <span style={{
                      marginLeft: '6px',
                      backgroundColor: '#dc2626',
                      color: '#ffffff',
                      fontSize: '0.68rem',
                      fontWeight: 700,
                      padding: '1px 6px',
                      borderRadius: '10px'
                    }}>
                      {newCaseCount} NEW
                    </span>
                  )}
                </NavLink>
                <NavLink to="/alerts" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                  Fraud Alerts
                </NavLink>
                <NavLink to="/transaction-network" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                  <Share2 size={15} style={{ marginRight: '5px', verticalAlign: 'middle' }} />
                  Transaction Network
                </NavLink>
                <NavLink to="/security" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                  Security Center
                </NavLink>
              </>
            ) : (
              // CUSTOMER NAVIGATION
              <>
                <NavLink to="/dashboard" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                  Dashboard
                </NavLink>
                <NavLink to="/transactions" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                  My Transactions
                </NavLink>
                <NavLink to="/transaction-network" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                  Transaction Network
                </NavLink>
                <NavLink to="/alerts" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`} style={{ position: 'relative' }}>
                  Alerts
                  {newAlertCount > 0 && (
                    <span style={{
                      marginLeft: '6px',
                      backgroundColor: '#ea580c',
                      color: '#ffffff',
                      fontSize: '0.68rem',
                      fontWeight: 700,
                      padding: '1px 6px',
                      borderRadius: '10px'
                    }}>
                      {newAlertCount}
                    </span>
                  )}
                </NavLink>
                <NavLink to="/account" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                  My Account
                </NavLink>
                <NavLink to="/security" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                  Security Center
                </NavLink>
              </>
            )}
          </nav>
        )}
      </div>

      <div className="header-right">
        {isAuthenticated ? (
          <>
            {/* Quick Action icon */}
            {!isReviewer ? (
              <button
                className="icon-button notification-btn"
                onClick={() => navigate('/alerts')}
                aria-label="View fraud and anomaly alerts"
                title="View Alerts"
                style={{ position: 'relative' }}
              >
                <Bell size={18} />
                {newAlertCount > 0 && (
                  <span className="notification-badge">{newAlertCount}</span>
                )}
              </button>
            ) : (
              <button
                className="icon-button notification-btn"
                onClick={() => navigate('/review/cases')}
                aria-label="View pending investigation cases"
                title="Review Cases"
                style={{ position: 'relative' }}
              >
                <ShieldAlert size={18} />
                {newCaseCount > 0 && (
                  <span className="notification-badge" style={{ backgroundColor: '#dc2626' }}>{newCaseCount}</span>
                )}
              </button>
            )}

            {/* User ID & Profile Chip */}
            <div className="user-profile-chip">
              <div className="user-avatar" style={isReviewer ? { backgroundColor: '#7c3aed', color: '#ffffff' } : {}}>
                <User size={16} />
              </div>
              <div className="user-info-text">
                <span className="user-name">{displayName}</span>
                <span className="user-bankid">
                  {user?.bankId || (isReviewer ? 'REV100001' : 'SB100001')}
                  {isReviewer && (
                    <span style={{
                      marginLeft: '4px',
                      fontSize: '0.65rem',
                      backgroundColor: '#ede9fe',
                      color: '#6d28d9',
                      padding: '1px 4px',
                      borderRadius: '4px',
                      fontWeight: 700
                    }}>
                      REVIEWER
                    </span>
                  )}
                </span>
              </div>
            </div>

            {/* Logout Button */}
            <button onClick={handleLogout} className="btn-logout" title="Sign out of SecureBank">
              <LogOut size={16} />
              <span className="logout-text">LOGOUT</span>
            </button>

            {/* Mobile Hamburger Button */}
            <button
              className="mobile-hamburger"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              aria-label="Toggle navigation"
            >
              {mobileMenuOpen ? <X size={22} /> : <Menu size={22} />}
            </button>
          </>
        ) : (
          <div className="security-badge-top">
            <ShieldCheck size={14} color="#0066cc" />
            <span>Secure Localhost Portal</span>
          </div>
        )}
      </div>

      {/* Mobile Navigation Drawer */}
      {isAuthenticated && mobileMenuOpen && (
        <div className="mobile-nav-drawer">
          {isReviewer ? (
            <>
              <NavLink
                to="/review"
                end
                className={({ isActive }) => `mobile-nav-link ${isActive ? 'active' : ''}`}
                onClick={() => setMobileMenuOpen(false)}
              >
                Review Dashboard
              </NavLink>
              <NavLink
                to="/review/cases"
                className={({ isActive }) => `mobile-nav-link ${isActive ? 'active' : ''}`}
                onClick={() => setMobileMenuOpen(false)}
              >
                Investigation Cases {newCaseCount > 0 ? `(${newCaseCount})` : ''}
              </NavLink>
              <NavLink
                to="/alerts"
                className={({ isActive }) => `mobile-nav-link ${isActive ? 'active' : ''}`}
                onClick={() => setMobileMenuOpen(false)}
              >
                Fraud Alerts
              </NavLink>
              <NavLink
                to="/transaction-network"
                className={({ isActive }) => `mobile-nav-link ${isActive ? 'active' : ''}`}
                onClick={() => setMobileMenuOpen(false)}
              >
                Transaction Network
              </NavLink>
              <NavLink
                to="/security"
                className={({ isActive }) => `mobile-nav-link ${isActive ? 'active' : ''}`}
                onClick={() => setMobileMenuOpen(false)}
              >
                Security Center
              </NavLink>
            </>
          ) : (
            <>
              <NavLink
                to="/dashboard"
                className={({ isActive }) => `mobile-nav-link ${isActive ? 'active' : ''}`}
                onClick={() => setMobileMenuOpen(false)}
              >
                Dashboard
              </NavLink>
              <NavLink
                to="/transactions"
                className={({ isActive }) => `mobile-nav-link ${isActive ? 'active' : ''}`}
                onClick={() => setMobileMenuOpen(false)}
              >
                My Transactions
              </NavLink>
              <NavLink
                to="/transaction-network"
                className={({ isActive }) => `mobile-nav-link ${isActive ? 'active' : ''}`}
                onClick={() => setMobileMenuOpen(false)}
              >
                Transaction Network
              </NavLink>
              <NavLink
                to="/alerts"
                className={({ isActive }) => `mobile-nav-link ${isActive ? 'active' : ''}`}
                onClick={() => setMobileMenuOpen(false)}
              >
                Alerts {newAlertCount > 0 ? `(${newAlertCount})` : ''}
              </NavLink>
              <NavLink
                to="/account"
                className={({ isActive }) => `mobile-nav-link ${isActive ? 'active' : ''}`}
                onClick={() => setMobileMenuOpen(false)}
              >
                My Account
              </NavLink>
              <NavLink
                to="/security"
                className={({ isActive }) => `mobile-nav-link ${isActive ? 'active' : ''}`}
                onClick={() => setMobileMenuOpen(false)}
              >
                Security Center
              </NavLink>
            </>
          )}
          <button
            onClick={() => {
              setMobileMenuOpen(false);
              handleLogout();
            }}
            className="mobile-nav-link logout-link"
          >
            Logout
          </button>
        </div>
      )}
    </header>
  );
};
