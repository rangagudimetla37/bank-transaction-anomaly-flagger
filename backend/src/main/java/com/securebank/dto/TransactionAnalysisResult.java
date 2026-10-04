package com.securebank.dto;

import com.securebank.model.RiskLevel;
import com.securebank.rules.RuleResult;

import java.util.List;

public class TransactionAnalysisResult {
    private String transactionId;
    private int ruleScore;
    private RiskLevel riskLevel;
    private List<String> triggeredRules;
    private List<RuleResult> ruleResults;
    private List<String> explanations;
    private String evaluatedAt;

    // Phase 4: Statistical Anomaly Detection fields (Z-Score)
    private Double zScore;
    private Integer statisticalScore;
    private Boolean statisticalAnomaly;
    private Double historicalMean;
    private Double historicalStdDev;
    private String statisticalExplanation;
    private Integer compositeScore;

    public TransactionAnalysisResult() {
    }

    public TransactionAnalysisResult(String transactionId, int ruleScore, RiskLevel riskLevel,
                                      List<String> triggeredRules, List<RuleResult> ruleResults,
                                      List<String> explanations, String evaluatedAt) {
        this.transactionId = transactionId;
        this.ruleScore = ruleScore;
        this.riskLevel = riskLevel;
        this.triggeredRules = triggeredRules;
        this.ruleResults = ruleResults;
        this.explanations = explanations;
        this.evaluatedAt = evaluatedAt;
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

    public List<RuleResult> getRuleResults() {
        return ruleResults;
    }

    public void setRuleResults(List<RuleResult> ruleResults) {
        this.ruleResults = ruleResults;
    }

    public List<String> getExplanations() {
        return explanations;
    }

    public void setExplanations(List<String> explanations) {
        this.explanations = explanations;
    }

    public String getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(String evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }

    public Double getZScore() {
        return zScore;
    }

    public void setZScore(Double zScore) {
        this.zScore = zScore;
    }

    public Integer getStatisticalScore() {
        return statisticalScore;
    }

    public void setStatisticalScore(Integer statisticalScore) {
        this.statisticalScore = statisticalScore;
    }

    public Boolean getStatisticalAnomaly() {
        return statisticalAnomaly;
    }

    public void setStatisticalAnomaly(Boolean statisticalAnomaly) {
        this.statisticalAnomaly = statisticalAnomaly;
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

    public String getStatisticalExplanation() {
        return statisticalExplanation;
    }

    public void setStatisticalExplanation(String statisticalExplanation) {
        this.statisticalExplanation = statisticalExplanation;
    }

    public Integer getCompositeScore() {
        return compositeScore;
    }

    public void setCompositeScore(Integer compositeScore) {
        this.compositeScore = compositeScore;
    }
}
