const API_BASE_URL = (import.meta as any).env?.VITE_API_BASE_URL || '/api';

export interface UserProfile {
  bankId: string;
  role: string;
  lastLogin: string;
}

export interface AuthResponse {
  success: boolean;
  message: string;
  token?: string;
  user?: UserProfile;
}

export interface VerifyResetResponse {
  success: boolean;
  message: string;
  resetToken?: string;
}

export interface ApiResponse {
  success: boolean;
  message: string;
}

export interface AccountData {
  accountNumber: string;
  customerName: string;
  bankId: string;
  accountType: string;
  balance: number;
  createdAt: string;
}

export interface TransactionData {
  transactionId: string;
  transactionType: string;
  description: string;
  amount: number;
  transactionDirection: 'CREDIT' | 'DEBIT';
  recipient: string;
  location: string;
  timestamp: string;
  status: 'COMPLETED' | 'PENDING' | 'FAILED';
}

export interface TransactionSummary {
  balance: number;
  totalTransactions: number;
  moneyIn: number;
  moneyOut: number;
  customerName: string;
  accountNumber: string;
  transactions: TransactionData[];
}

export interface SecurityActivity {
  id: number;
  bankId: string;
  activityType: string;
  description: string;
  ipAddress: string;
  timestamp: string;
}

export interface SecuritySummary {
  securityStatus: string;
  lastLogin: string;
  passwordLastChanged: string;
  activities: SecurityActivity[];
}

// Phase 3: Rule Result and Analysis Interfaces
export interface RuleResultItem {
  ruleId: string;
  ruleName: string;
  triggered: boolean;
  riskPoints: number;
  explanation: string;
}

export type RiskLevelType = 'NORMAL' | 'WATCH' | 'SUSPICIOUS' | 'HIGH_RISK';

export interface TransactionAnalysisResult {
  transactionId: string;
  ruleScore: number;
  riskLevel: RiskLevelType;
  triggeredRules: string[];
  ruleResults: RuleResultItem[];
  explanations: string[];
  evaluatedAt: string;
  // Phase 4: Python Statistical Outlier Detection (Z-Score)
  zScore?: number;
  statisticalScore?: number;
  statisticalAnomaly?: boolean;
  historicalMean?: number;
  historicalStdDev?: number;
  statisticalExplanation?: string;
  compositeScore?: number;
}

// Phase 5: Fraud Alert & Final Risk Analysis Interfaces
export interface FraudAlert {
  id: number;
  alertId: string;
  transactionId: string;
  accountId: number;
  riskScore: number;
  riskLevel: RiskLevelType;
  title: string;
  description: string;
  status: 'NEW' | 'VIEWED' | 'REVIEW_REQUIRED' | 'RESOLVED';
  createdAt: string;
  updatedAt?: string;
}

export interface AlertSummary {
  totalAlerts: number;
  newAlerts: number;
  highRiskCount: number;
  suspiciousCount: number;
}

export interface FinalRiskAnalysis {
  transactionId: string;
  ruleScore: number;
  statisticalScore: number;
  finalRiskScore: number;
  riskLevel: RiskLevelType;
  triggeredRules: string[];
  statisticalAnomaly: boolean;
  statisticalClassification: string;
  reasons: string[];
  recommendation: string;
  analyzedAt: string;
  amount?: number;
  zScore?: number;
  historicalMean?: number;
  historicalStdDev?: number;
  alertId?: string;
  alertStatus?: string;
}

// Phase 6: Manual Review & Investigation Interfaces
export interface ReviewCaseSummary {
  caseId: string;
  alertId: string;
  transactionId: string;
  accountId: number;
  amount?: number;
  merchant?: string;
  transactionTime?: string;
  riskScore: number;
  riskLevel: RiskLevelType;
  status: 'NEW' | 'IN_REVIEW' | 'ESCALATED' | 'RESOLVED' | 'CLOSED';
  assignedReviewerId?: string;
  decision?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ReviewAuditLog {
  id?: number;
  caseId: string;
  reviewerId: string;
  action: string;
  details: string;
  createdAt: string;
}

export interface ReviewCaseDetail {
  caseId: string;
  alertId: string;
  transactionId: string;
  accountId: number;
  riskScore: number;
  riskLevel: RiskLevelType;
  status: 'NEW' | 'IN_REVIEW' | 'ESCALATED' | 'RESOLVED' | 'CLOSED';
  assignedReviewerId?: string;
  reviewerNotes?: string;
  decision?: string;
  createdAt: string;
  updatedAt: string;
  resolvedAt?: string;
  transaction?: TransactionData;
  riskAnalysis?: FinalRiskAnalysis;
  graphAnalysis?: GraphAnalysisResult;
  auditLogs: ReviewAuditLog[];
}

export interface ReviewStats {
  totalCases: number;
  newCases: number;
  inReviewCases: number;
  escalatedCases: number;
  resolvedCases: number;
  highRiskCases: number;
}

// Phase 7: Transaction Relationship & Network Graph Interfaces
export type GraphNodeType = 'ACCOUNT' | 'TRANSACTION' | 'RECIPIENT' | 'LOCATION';

export interface GraphNode {
  id: string;
  label: string;
  type: GraphNodeType;
  properties?: Record<string, any>;
}

export interface GraphEdge {
  source: string;
  target: string;
  relationship: string;
  label?: string;
}

export interface GraphAnalysisResult {
  transactionId: string;
  graphScore: number;
  graphRiskLevel: RiskLevelType;
  newRecipient: boolean;
  sharedRecipient: boolean;
  unusualLocation: boolean;
  recipientTransactionCount: number;
  locationTransactionCount: number;
  uniqueRecipients: number;
  uniqueLocations: number;
  totalAccountTransactions: number;
  reasons: string[];
  evaluatedAt: string;
}

export interface GraphNetworkStats {
  totalTransactions: number;
  uniqueRecipients: number;
  uniqueLocations: number;
  totalEdges: number;
  mostFrequentLocation: string;
  mostFrequentRecipient: string;
  averageTransactionsPerRecipient: number;
}

export interface TransactionGraph {
  nodes: GraphNode[];
  edges: GraphEdge[];
  analysis?: GraphAnalysisResult;
  stats?: GraphNetworkStats;
}

// Token management in sessionStorage (never storing passwords in browser storage)
export const getToken = (): string | null => {
  return sessionStorage.getItem('securebank_auth_token');
};

export const setToken = (token: string): void => {
  sessionStorage.setItem('securebank_auth_token', token);
};

export const clearToken = (): void => {
  sessionStorage.removeItem('securebank_auth_token');
};

// Remember Bank ID only (NEVER stores passwords)
export const getRememberedBankId = (): string => {
  return localStorage.getItem('securebank_remembered_bank_id') || '';
};

export const setRememberedBankId = (bankId: string): void => {
  localStorage.setItem('securebank_remembered_bank_id', bankId);
};

export const clearRememberedBankId = (): void => {
  localStorage.removeItem('securebank_remembered_bank_id');
};

const getAuthHeaders = (): HeadersInit => {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  };
  const token = getToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
};

async function handleJsonResponse<T>(response: Response): Promise<T> {
  if (response.status === 401) {
    clearToken();
    if (typeof window !== 'undefined') {
      const path = window.location.pathname;
      if (!path.startsWith('/login') && !path.startsWith('/forgot-password') && !path.startsWith('/reset-password')) {
        window.location.href = '/login';
      }
    }
    throw new Error('Session expired or unauthorized. Please log in again.');
  }
  if (response.status === 403) {
    throw new Error('You do not have permission to access this resource.');
  }
  if (response.status === 404) {
    throw new Error('Requested information was not found.');
  }
  if (response.status >= 500) {
    throw new Error('Something went wrong. Please try again.');
  }
  if (!response.ok) {
    try {
      const err = await response.json();
      throw new Error(err.message || err.error || 'Something went wrong. Please try again.');
    } catch (e: any) {
      if (e.message && e.message !== 'Something went wrong. Please try again.') throw e;
      throw new Error('Something went wrong. Please try again.');
    }
  }
  return response.json() as Promise<T>;
}

export const api = {
  async login(bankId: string, password: string): Promise<AuthResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        body: JSON.stringify({ bankId, password }),
      });

      const data = await response.json();
      if (!response.ok) {
        return {
          success: false,
          message: data.message || (response.status === 429 ? 'Too many failed attempts. Please try again later.' : 'Invalid Bank ID or password.'),
        };
      }

      if (data.token) {
        setToken(data.token);
      }
      return data;
    } catch {
      return {
        success: false,
        message: 'Backend service is unavailable. Please verify the Java server is running.',
      };
    }
  },

  async logout(): Promise<ApiResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/logout`, {
        method: 'POST',
        headers: getAuthHeaders(),
      });
      const data = await response.json();
      clearToken();
      return data;
    } catch {
      clearToken();
      return { success: true, message: 'Logged out.' };
    }
  },

  async getMe(): Promise<AuthResponse> {
    const token = getToken();
    if (!token) {
      return { success: false, message: 'No authentication token found.' };
    }

    try {
      const response = await fetch(`${API_BASE_URL}/auth/me`, {
        method: 'GET',
        headers: getAuthHeaders(),
      });

      if (!response.ok) {
        clearToken();
        return { success: false, message: 'Session expired or invalid.' };
      }

      const data = await response.json();
      return data;
    } catch {
      return { success: false, message: 'Backend service unavailable.' };
    }
  },

  async verifyForgotPassword(bankId: string, securityPin: string): Promise<VerifyResetResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/forgot-password/verify`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        body: JSON.stringify({ bankId, securityPin }),
      });

      const data = await response.json();
      return {
        success: response.ok && data.success,
        message: data.message || (response.ok ? 'Identity verified successfully.' : 'Verification failed.'),
        resetToken: data.resetToken,
      };
    } catch {
      return {
        success: false,
        message: 'Backend service is unavailable. Please check the server.',
      };
    }
  },

  async resetPassword(
    resetToken: string,
    bankId: string,
    newPassword: string,
    confirmPassword: string
  ): Promise<ApiResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/reset-password`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        body: JSON.stringify({ resetToken, bankId, newPassword, confirmPassword }),
      });

      const data = await response.json();
      return {
        success: response.ok && data.success,
        message: data.message || (response.ok ? 'Password changed successfully.' : 'Password reset failed.'),
      };
    } catch {
      return {
        success: false,
        message: 'Backend service is unavailable. Please check the server.',
      };
    }
  },

  async changePassword(
    currentPassword: string,
    newPassword: string,
    confirmPassword: string
  ): Promise<AuthResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/change-password`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify({ currentPassword, newPassword, confirmPassword }),
      });

      const data = await response.json();
      if (response.ok && data.token) {
        setToken(data.token);
      }
      return {
        success: response.ok && data.success,
        message: data.message || (response.ok ? 'Password changed successfully.' : 'Password change failed.'),
        token: data.token,
        user: data.user,
      };
    } catch {
      return {
        success: false,
        message: 'Backend service is unavailable. Please check the server.',
      };
    }
  },

  async getAccount(): Promise<AccountData> {
    const response = await fetch(`${API_BASE_URL}/account/me`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    return handleJsonResponse<AccountData>(response);
  },

  async getTransactions(): Promise<TransactionSummary> {
    const response = await fetch(`${API_BASE_URL}/transactions`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    return handleJsonResponse<TransactionSummary>(response);
  },

  async getTransactionDetail(transactionId: string): Promise<TransactionData> {
    const response = await fetch(`${API_BASE_URL}/transactions/${encodeURIComponent(transactionId)}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    return handleJsonResponse<TransactionData>(response);
  },

  async getSecurityActivity(): Promise<SecuritySummary> {
    const response = await fetch(`${API_BASE_URL}/security/activity`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    return handleJsonResponse<SecuritySummary>(response);
  },

  // Phase 3: Propositional Logic Rule Analysis Endpoints
  async getTransactionAnalysis(transactionId: string): Promise<TransactionAnalysisResult> {
    const response = await fetch(`${API_BASE_URL}/transactions/${encodeURIComponent(transactionId)}/analysis`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    return handleJsonResponse<TransactionAnalysisResult>(response);
  },

  async getRecentRisk(): Promise<TransactionAnalysisResult[]> {
    const response = await fetch(`${API_BASE_URL}/transactions/recent-risk`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    return handleJsonResponse<TransactionAnalysisResult[]>(response);
  },

  // Phase 5: Final Combined Risk & Fraud Alert Endpoints
  async getFinalRiskAnalysis(transactionId: string): Promise<FinalRiskAnalysis> {
    const response = await fetch(`${API_BASE_URL}/transactions/${encodeURIComponent(transactionId)}/risk-analysis`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    return handleJsonResponse<FinalRiskAnalysis>(response);
  },

  async getAlerts(): Promise<FraudAlert[]> {
    const response = await fetch(`${API_BASE_URL}/alerts`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    return handleJsonResponse<FraudAlert[]>(response);
  },

  async getAlertDetail(alertId: string): Promise<FraudAlert> {
    const response = await fetch(`${API_BASE_URL}/alerts/${encodeURIComponent(alertId)}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    return handleJsonResponse<FraudAlert>(response);
  },

  async updateAlertStatus(alertId: string, status: string): Promise<ApiResponse> {
    const response = await fetch(`${API_BASE_URL}/alerts/${encodeURIComponent(alertId)}/status`, {
      method: 'PATCH',
      headers: getAuthHeaders(),
      body: JSON.stringify({ status }),
    });
    return handleJsonResponse<ApiResponse>(response);
  },

  async getAlertSummary(): Promise<AlertSummary> {
    const response = await fetch(`${API_BASE_URL}/alerts/summary`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    return handleJsonResponse<AlertSummary>(response);
  },

  // Phase 6: Manual Reviewer Investigation Endpoints
  async getReviewStats(): Promise<ReviewStats> {
    const response = await fetch(`${API_BASE_URL}/review/cases/summary`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    return handleJsonResponse<ReviewStats>(response);
  },

  async getReviewCases(): Promise<ReviewCaseSummary[]> {
    const response = await fetch(`${API_BASE_URL}/review/cases`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    return handleJsonResponse<ReviewCaseSummary[]>(response);
  },

  async getReviewCaseDetail(caseId: string): Promise<ReviewCaseDetail> {
    const response = await fetch(`${API_BASE_URL}/review/cases/${encodeURIComponent(caseId)}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    return handleJsonResponse<ReviewCaseDetail>(response);
  },

  async updateReviewNotes(caseId: string, notes: string): Promise<ApiResponse> {
    const response = await fetch(`${API_BASE_URL}/review/cases/${encodeURIComponent(caseId)}/notes`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ notes }),
    });
    return handleJsonResponse<ApiResponse>(response);
  },

  async submitReviewDecision(caseId: string, decision: string, notes?: string): Promise<ApiResponse> {
    const response = await fetch(`${API_BASE_URL}/review/cases/${encodeURIComponent(caseId)}/decision`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ decision, notes }),
    });
    return handleJsonResponse<ApiResponse>(response);
  },

  async escalateReviewCase(caseId: string, reason?: string): Promise<ApiResponse> {
    const response = await fetch(`${API_BASE_URL}/review/cases/${encodeURIComponent(caseId)}/escalate`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ reason: reason || 'Reviewer requested senior escalation' }),
    });
    return handleJsonResponse<ApiResponse>(response);
  },

  // Phase 7: Transaction Network Graph Endpoints
  async getAccountNetwork(transactionId?: string): Promise<TransactionGraph> {
    const url = transactionId
      ? `${API_BASE_URL}/transactions/network?transactionId=${encodeURIComponent(transactionId)}`
      : `${API_BASE_URL}/transactions/network`;
    const response = await fetch(url, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    return handleJsonResponse<TransactionGraph>(response);
  },

  async getTransactionGraph(transactionId: string): Promise<TransactionGraph> {
    const response = await fetch(`${API_BASE_URL}/transactions/${encodeURIComponent(transactionId)}/graph`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    return handleJsonResponse<TransactionGraph>(response);
  },

  async getTransactionGraphAnalysis(transactionId: string): Promise<GraphAnalysisResult> {
    const response = await fetch(`${API_BASE_URL}/transactions/${encodeURIComponent(transactionId)}/graph-analysis`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    return handleJsonResponse<GraphAnalysisResult>(response);
  },
};

