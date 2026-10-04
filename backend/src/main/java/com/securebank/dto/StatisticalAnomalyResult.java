package com.securebank.dto;

/**
 * Encapsulates the Python Z-Score statistical anomaly metrics for a transaction.
 */
public class StatisticalAnomalyResult {

    private String transactionId;
    private String accountId;
    private Double amount;
    private Double historicalMean;
    private Double historicalStdDev;
    private Double zScore;
    private Double absZScore;
    private Integer statisticalScore;
    private Boolean isAnomaly;
    private String explanation;

    public StatisticalAnomalyResult() {
    }

    public StatisticalAnomalyResult(String transactionId, String accountId, Double amount,
                                    Double historicalMean, Double historicalStdDev, Double zScore,
                                    Double absZScore, Integer statisticalScore, Boolean isAnomaly,
                                    String explanation) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.amount = amount;
        this.historicalMean = historicalMean;
        this.historicalStdDev = historicalStdDev;
        this.zScore = zScore;
        this.absZScore = absZScore;
        this.statisticalScore = statisticalScore;
        this.isAnomaly = isAnomaly;
        this.explanation = explanation;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
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

    public Double getZScore() {
        return zScore;
    }

    public void setZScore(Double zScore) {
        this.zScore = zScore;
    }

    public Double getAbsZScore() {
        return absZScore;
    }

    public void setAbsZScore(Double absZScore) {
        this.absZScore = absZScore;
    }

    public Integer getStatisticalScore() {
        return statisticalScore;
    }

    public void setStatisticalScore(Integer statisticalScore) {
        this.statisticalScore = statisticalScore;
    }

    public Boolean getIsAnomaly() {
        return isAnomaly;
    }

    public void setIsAnomaly(Boolean isAnomaly) {
        this.isAnomaly = isAnomaly;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
