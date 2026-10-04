package com.securebank.model;

public enum RiskLevel {
    NORMAL("NORMAL", "Preliminary Rule-Based Assessment: Low anomaly probability. Standard banking pattern."),
    WATCH("WATCH", "Preliminary Rule-Based Assessment: Minor atypical parameters. Flagged for observation."),
    SUSPICIOUS("SUSPICIOUS", "Preliminary Rule-Based Assessment: Multiple rule anomalies detected. Recommended for review."),
    HIGH_RISK("HIGH_RISK", "Preliminary Rule-Based Assessment: High cumulative risk score. High priority screening.");

    private final String code;
    private final String description;

    RiskLevel(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static RiskLevel fromScore(int score) {
        if (score >= 80) {
            return HIGH_RISK;
        } else if (score >= 60) {
            return SUSPICIOUS;
        } else if (score >= 30) {
            return WATCH;
        } else {
            return NORMAL;
        }
    }
}
