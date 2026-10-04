import React from 'react';

export const Footer: React.FC = () => {
  return (
    <footer className="banking-footer">
      <p>© 2026 SECUREBANK. College Mini-Project: Bank Transaction Anomaly Flagger (Phase 1).</p>
      <p style={{ marginTop: '4px', fontSize: '0.72rem', color: '#94a3b8' }}>
        Localhost prototype only • Synthetic demo data • No real banking APIs
      </p>
    </footer>
  );
};
