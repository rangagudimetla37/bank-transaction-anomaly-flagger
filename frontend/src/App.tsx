import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { LoginPage } from './pages/LoginPage';
import { ForgotPasswordPage } from './pages/ForgotPasswordPage';
import { DashboardPage } from './pages/DashboardPage';
import { TransactionsPage } from './pages/TransactionsPage';
import { TransactionDetailsPage } from './pages/TransactionDetailsPage';
import { AccountPage } from './pages/AccountPage';
import { SecurityPage } from './pages/SecurityPage';
import { AlertsPage } from './pages/AlertsPage';
import { AlertDetailsPage } from './pages/AlertDetailsPage';
import { ReviewDashboardPage } from './pages/ReviewDashboardPage';
import { ReviewCasesPage } from './pages/ReviewCasesPage';
import { ReviewCaseDetailPage } from './pages/ReviewCaseDetailPage';
import { TransactionNetworkPage } from './pages/TransactionNetworkPage';

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Public Authentication Routes */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ForgotPasswordPage />} />

          {/* Protected Customer Banking Routes */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <DashboardPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/transactions"
            element={
              <ProtectedRoute>
                <TransactionsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/transactions/:transactionId"
            element={
              <ProtectedRoute>
                <TransactionDetailsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/transaction-network"
            element={
              <ProtectedRoute>
                <TransactionNetworkPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/alerts"
            element={
              <ProtectedRoute>
                <AlertsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/alerts/:alertId"
            element={
              <ProtectedRoute>
                <AlertDetailsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/account"
            element={
              <ProtectedRoute>
                <AccountPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/security"
            element={
              <ProtectedRoute>
                <SecurityPage />
              </ProtectedRoute>
            }
          />

          {/* Protected Reviewer Investigation Routes (Phase 6) */}
          <Route
            path="/review"
            element={
              <ProtectedRoute requiredRole="REVIEWER">
                <ReviewDashboardPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/review/cases"
            element={
              <ProtectedRoute requiredRole="REVIEWER">
                <ReviewCasesPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/review/cases/:caseId"
            element={
              <ProtectedRoute requiredRole="REVIEWER">
                <ReviewCaseDetailPage />
              </ProtectedRoute>
            }
          />

          {/* Fallback route */}
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
};

export default App;
