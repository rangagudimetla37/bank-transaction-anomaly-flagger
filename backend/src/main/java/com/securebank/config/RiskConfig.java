package com.securebank.config;

import com.securebank.model.RiskLevel;

/**
 * Central configuration and calculation rules for Risk Scoring and Alert Thresholds (Phase 5).
 * Consolidates all weights, thresholds, and recommendations in one logical location.
 */
public final class RiskConfig {

    private RiskConfig() {
        // Prevent instantiation
    }

    // Weights: Rule Score 60%, Python Statistical Outlier Score 40%
    public static final double RULE_SCORE_WEIGHT = 0.60;
    public static final double STATISTICAL_SCORE_WEIGHT = 0.40;

    // Risk Classification Score Ranges
    // 0–29: NORMAL, 30–59: WATCH, 60–79: SUSPICIOUS, 80–100: HIGH_RISK
    public static final int NORMAL_MIN = 0;
    public static final int NORMAL_MAX = 29;

    public static final int WATCH_MIN = 30;
    public static final int WATCH_MAX = 59;

    public static final int SUSPICIOUS_MIN = 60;
    public static final int SUSPICIOUS_MAX = 79;

    public static final int HIGH_RISK_MIN = 80;
    public static final int HIGH_RISK_MAX = 100;

    // Minimum final score required to generate a formal fraud alert
    public static final int ALERT_GENERATION_THRESHOLD = 60;

    /**
     * Calculates the Final Combined Risk Score:
     * finalRiskScore = round((ruleScore * 0.60) + (statisticalScore * 0.40))
     * Result is normalized and capped between 0 and 100.
     */
    public static int calculateFinalScore(int ruleScore, int statisticalScore) {
        int boundedRule = Math.max(0, Math.min(100, ruleScore));
        int boundedStat = Math.max(0, Math.min(100, statisticalScore));
        double combined = (boundedRule * RULE_SCORE_WEIGHT) + (boundedStat * STATISTICAL_SCORE_WEIGHT);
        return (int) Math.max(0, Math.min(100, Math.round(combined)));
    }

    /**
     * Maps a final risk score to its corresponding RiskLevel enum.
     */
    public static RiskLevel determineRiskLevel(int finalScore) {
        if (finalScore >= HIGH_RISK_MIN) {
            return RiskLevel.HIGH_RISK;
        } else if (finalScore >= SUSPICIOUS_MIN) {
            return RiskLevel.SUSPICIOUS;
        } else if (finalScore >= WATCH_MIN) {
            return RiskLevel.WATCH;
        } else {
            return RiskLevel.NORMAL;
        }
    }

    /**
     * Produces standard non-accusatory operational recommendations for review teams.
     */
    public static String determineRecommendation(RiskLevel level) {
        return switch (level) {
            case HIGH_RISK -> "Recommended for manual review.";
            case SUSPICIOUS -> "Additional verification is recommended.";
            case WATCH -> "Monitor transaction activity.";
            case NORMAL -> "No immediate anomaly detected.";
        };
    }
}
