package com.securebank.dto;

public class ReviewCaseSummaryDto {

    private String caseId;
    private String alertId;
    private String transactionId;
    private Long accountId;
    private Double amount;
    private String merchant;
    private String transactionTime;
    private Integer riskScore;
    private String riskLevel;
    private String status;
    private String assignedReviewerId;
    private String decision;
    private String createdAt;
    private String updatedAt;

    public ReviewCaseSummaryDto() {
    }

    public ReviewCaseSummaryDto(String caseId, String alertId, String transactionId, Long accountId,
                                Double amount, String merchant, String transactionTime, Integer riskScore,
                                String riskLevel, String status, String assignedReviewerId, String decision,
                                String createdAt, String updatedAt) {
        this.caseId = caseId;
        this.alertId = alertId;
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.amount = amount;
        this.merchant = merchant;
        this.transactionTime = transactionTime;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.status = status;
        this.assignedReviewerId = assignedReviewerId;
        this.decision = decision;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(String transactionTime) {
        this.transactionTime = transactionTime;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignedReviewerId() {
        return assignedReviewerId;
    }

    public void setAssignedReviewerId(String assignedReviewerId) {
        this.assignedReviewerId = assignedReviewerId;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
