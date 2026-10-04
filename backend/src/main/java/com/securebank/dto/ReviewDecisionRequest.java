package com.securebank.dto;

public class ReviewDecisionRequest {

    private String decision; // LEGITIMATE, SUSPICIOUS_CONFIRMED, REQUIRES_FURTHER_REVIEW
    private String notes;

    public ReviewDecisionRequest() {
    }

    public ReviewDecisionRequest(String decision, String notes) {
        this.decision = decision;
        this.notes = notes;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
