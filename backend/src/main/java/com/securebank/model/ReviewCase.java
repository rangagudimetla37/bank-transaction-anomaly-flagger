package com.securebank.model;

public class ReviewCase {

    private Long id;
    private String caseId;
    private String alertId;
    private String transactionId;
    private Long accountId;
    private Integer riskScore;
    private String riskLevel;
    private String status; // NEW, IN_REVIEW, ESCALATED, RESOLVED, CLOSED
    private String assignedReviewerId;
    private String reviewerNotes;
    private String decision; // LEGITIMATE, SUSPICIOUS_CONFIRMED, REQUIRES_FURTHER_REVIEW
    private String createdAt;
    private String updatedAt;
    private String resolvedAt;

    public ReviewCase() {
    }

    public ReviewCase(Long id, String caseId, String alertId, String transactionId, Long accountId,
                      Integer riskScore, String riskLevel, String status, String assignedReviewerId,
                      String reviewerNotes, String decision, String createdAt, String updatedAt, String resolvedAt) {
        this.id = id;
        this.caseId = caseId;
        this.alertId = alertId;
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.status = status;
        this.assignedReviewerId = assignedReviewerId;
        this.reviewerNotes = reviewerNotes;
        this.decision = decision;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.resolvedAt = resolvedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getReviewerNotes() {
        return reviewerNotes;
    }

    public void setReviewerNotes(String reviewerNotes) {
        this.reviewerNotes = reviewerNotes;
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

    public String getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(String resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
