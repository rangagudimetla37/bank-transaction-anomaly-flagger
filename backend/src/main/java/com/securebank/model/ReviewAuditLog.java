package com.securebank.model;

public class ReviewAuditLog {

    private Long id;
    private String caseId;
    private String reviewerId;
    private String action; // CASE_OPENED, NOTE_ADDED, DECISION_SUBMITTED, CASE_ESCALATED
    private String details;
    private String createdAt;

    public ReviewAuditLog() {
    }

    public ReviewAuditLog(Long id, String caseId, String reviewerId, String action, String details, String createdAt) {
        this.id = id;
        this.caseId = caseId;
        this.reviewerId = reviewerId;
        this.action = action;
        this.details = details;
        this.createdAt = createdAt;
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

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
