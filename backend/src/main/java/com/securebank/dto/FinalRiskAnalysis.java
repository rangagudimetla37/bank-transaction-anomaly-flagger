package com.securebank.dto;

import com.securebank.model.RiskLevel;

import java.util.ArrayList;
import java.util.List;

public class FinalRiskAnalysis {

    private String transactionId;
    private int ruleScore;
    private int statisticalScore;
    private int finalRiskScore;
    private RiskLevel riskLevel;
    private List<String> triggeredRules = new ArrayList<>();
    private boolean statisticalAnomaly;
    private String statisticalClassification;
    private List<String> reasons = new ArrayList<>();
    private String recommendation;
    private String analyzedAt;

    // Supplementary context
    private Double amount;
    private Double zScore;
    private Double historicalMean;
    private Double historicalStdDev;
    private String alertId;
    private String alertStatus;

    public FinalRiskAnalysis() {
    }

    public FinalRiskAnalysis(String transactionId, int ruleScore, int statisticalScore,
                             int finalRiskScore, RiskLevel riskLevel, List<String> triggeredRules,
                             boolean statisticalAnomaly, String statisticalClassification,
                             List<String> reasons, String recommendation, String analyzedAt) {
        this.transactionId = transactionId;
        this.ruleScore = ruleScore;
        this.statisticalScore = statisticalScore;
        this.finalRiskScore = finalRiskScore;
        this.riskLevel = riskLevel;
        this.triggeredRules = triggeredRules;
        this.statisticalAnomaly = statisticalAnomaly;
        this.statisticalClassification = statisticalClassification;
        this.reasons = reasons;
        this.recommendation = recommendation;
        this.analyzedAt = analyzedAt;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public int getRuleScore() {
        return ruleScore;
    }

    public void setRuleScore(int ruleScore) {
        this.ruleScore = ruleScore;
    }

    public int getStatisticalScore() {
        return statisticalScore;
    }

    public void setStatisticalScore(int statisticalScore) {
        this.statisticalScore = statisticalScore;
    }

    public int getFinalRiskScore() {
        return finalRiskScore;
    }

    public void setFinalRiskScore(int finalRiskScore) {
        this.finalRiskScore = finalRiskScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public List<String> getTriggeredRules() {
        return triggeredRules;
    }

    public void setTriggeredRules(List<String> triggeredRules) {
        this.triggeredRules = triggeredRules;
    }

    public boolean isStatisticalAnomaly() {
        return statisticalAnomaly;
    }

    public void setStatisticalAnomaly(boolean statisticalAnomaly) {
        this.statisticalAnomaly = statisticalAnomaly;
    }

    public String getStatisticalClassification() {
        return statisticalClassification;
    }

    public void setStatisticalClassification(String statisticalClassification) {
        this.statisticalClassification = statisticalClassification;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(String analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getZScore() {
        return zScore;
    }

    public void setZScore(Double zScore) {
        this.zScore = zScore;
    }

    public Double getHistoricalMean() {
        return historicalMean;
    }

    public void setHistoricalMean(Double historicalMean) {
        this.historicalMean = historicalMean;
    }

    public Double getHistoricalStdDev() {
        return historicalStdDev;
    }

    public void setHistoricalStdDev(Double historicalStdDev) {
        this.historicalStdDev = historicalStdDev;
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public String getAlertStatus() {
        return alertStatus;
    }

    public void setAlertStatus(String alertStatus) {
        this.alertStatus = alertStatus;
    }
}
