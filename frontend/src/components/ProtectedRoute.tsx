import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface ProtectedRouteProps {
  children: React.ReactElement;
  requiredRole?: string;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, requiredRole }) => {
  const { isAuthenticated, isLoading, user } = useAuth();

  if (isLoading) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', flexDirection: 'column', gap: '12px' }}>
        <div className="spinner" style={{ borderColor: '#003366', borderTopColor: 'transparent', width: '28px', height: '28px', borderWidth: '3px' }}></div>
        <span style={{ fontSize: '0.85rem', color: '#64748b' }}>Verifying secure session...</span>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole && user?.role !== requiredRole) {
    // Role-based protection: if customer tries to access reviewer route, redirect to customer dashboard
    return <Navigate to="/dashboard" replace />;
  }

  return children;
};
