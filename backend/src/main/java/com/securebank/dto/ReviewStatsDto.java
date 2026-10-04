package com.securebank.dto;

public class ReviewStatsDto {

    private int totalCases;
    private int newCases;
    private int inReviewCases;
    private int escalatedCases;
    private int resolvedCases;
    private int highRiskCases;

    public ReviewStatsDto() {
    }

    public ReviewStatsDto(int totalCases, int newCases, int inReviewCases, int escalatedCases, int resolvedCases, int highRiskCases) {
        this.totalCases = totalCases;
        this.newCases = newCases;
        this.inReviewCases = inReviewCases;
        this.escalatedCases = escalatedCases;
        this.resolvedCases = resolvedCases;
        this.highRiskCases = highRiskCases;
    }

    public int getTotalCases() {
        return totalCases;
    }

    public void setTotalCases(int totalCases) {
        this.totalCases = totalCases;
    }

    public int getNewCases() {
        return newCases;
    }

    public void setNewCases(int newCases) {
        this.newCases = newCases;
    }

    public int getInReviewCases() {
        return inReviewCases;
    }

    public void setInReviewCases(int inReviewCases) {
        this.inReviewCases = inReviewCases;
    }

    public int getEscalatedCases() {
        return escalatedCases;
    }

    public void setEscalatedCases(int escalatedCases) {
        this.escalatedCases = escalatedCases;
    }

    public int getResolvedCases() {
        return resolvedCases;
    }

    public void setResolvedCases(int resolvedCases) {
        this.resolvedCases = resolvedCases;
    }

    public int getHighRiskCases() {
        return highRiskCases;
    }

    public void setHighRiskCases(int highRiskCases) {
        this.highRiskCases = highRiskCases;
    }
}
