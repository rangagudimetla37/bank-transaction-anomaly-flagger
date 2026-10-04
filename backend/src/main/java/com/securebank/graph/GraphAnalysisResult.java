package com.securebank.graph;

import java.util.ArrayList;
import java.util.List;

public class GraphAnalysisResult {

    private String transactionId;
    private int graphScore;
    private String graphRiskLevel; // NORMAL, WATCH, SUSPICIOUS, HIGH_RISK
    private boolean newRecipient;
    private boolean sharedRecipient;
    private boolean unusualLocation;
    private int recipientTransactionCount;
    private int locationTransactionCount;
    private int uniqueRecipients;
    private int uniqueLocations;
    private int totalAccountTransactions;
    private List<String> reasons = new ArrayList<>();
    private String evaluatedAt;

    public GraphAnalysisResult() {
    }

    public GraphAnalysisResult(String transactionId, int graphScore, String graphRiskLevel,
                               boolean newRecipient, boolean sharedRecipient,
                               int recipientTransactionCount,
                               int locationTransactionCount, int uniqueRecipients, int uniqueLocations,
                               int totalAccountTransactions, List<String> reasons, String evaluatedAt) {
        this(transactionId, graphScore, graphRiskLevel, newRecipient, sharedRecipient, false,
             recipientTransactionCount, locationTransactionCount, uniqueRecipients, uniqueLocations,
             totalAccountTransactions, reasons, evaluatedAt);
    }

    public GraphAnalysisResult(String transactionId, int graphScore, String graphRiskLevel,
                               boolean newRecipient, boolean sharedRecipient, boolean unusualLocation,
                               int recipientTransactionCount,
                               int locationTransactionCount, int uniqueRecipients, int uniqueLocations,
                               int totalAccountTransactions, List<String> reasons, String evaluatedAt) {
        this.transactionId = transactionId;
        this.graphScore = graphScore;
        this.graphRiskLevel = graphRiskLevel;
        this.newRecipient = newRecipient;
        this.sharedRecipient = sharedRecipient;
        this.unusualLocation = unusualLocation;
        this.recipientTransactionCount = recipientTransactionCount;
        this.locationTransactionCount = locationTransactionCount;
        this.uniqueRecipients = uniqueRecipients;
        this.uniqueLocations = uniqueLocations;
        this.totalAccountTransactions = totalAccountTransactions;
        this.reasons = reasons != null ? reasons : new ArrayList<>();
        this.evaluatedAt = evaluatedAt;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public int getGraphScore() {
        return graphScore;
    }

    public void setGraphScore(int graphScore) {
        this.graphScore = graphScore;
    }

    public String getGraphRiskLevel() {
        return graphRiskLevel;
    }

    public void setGraphRiskLevel(String graphRiskLevel) {
        this.graphRiskLevel = graphRiskLevel;
    }

    public boolean isNewRecipient() {
        return newRecipient;
    }

    public void setNewRecipient(boolean newRecipient) {
        this.newRecipient = newRecipient;
    }

    public boolean isSharedRecipient() {
        return sharedRecipient;
    }

    public void setSharedRecipient(boolean sharedRecipient) {
        this.sharedRecipient = sharedRecipient;
    }

    public boolean isUnusualLocation() {
        return unusualLocation;
    }

    public void setUnusualLocation(boolean unusualLocation) {
        this.unusualLocation = unusualLocation;
    }

    public int getRecipientTransactionCount() {
        return recipientTransactionCount;
    }

    public void setRecipientTransactionCount(int recipientTransactionCount) {
        this.recipientTransactionCount = recipientTransactionCount;
    }

    public int getLocationTransactionCount() {
        return locationTransactionCount;
    }

    public void setLocationTransactionCount(int locationTransactionCount) {
        this.locationTransactionCount = locationTransactionCount;
    }

    public int getUniqueRecipients() {
        return uniqueRecipients;
    }

    public void setUniqueRecipients(int uniqueRecipients) {
        this.uniqueRecipients = uniqueRecipients;
    }

    public int getUniqueLocations() {
        return uniqueLocations;
    }

    public void setUniqueLocations(int uniqueLocations) {
        this.uniqueLocations = uniqueLocations;
    }

    public int getTotalAccountTransactions() {
        return totalAccountTransactions;
    }

    public void setTotalAccountTransactions(int totalAccountTransactions) {
        this.totalAccountTransactions = totalAccountTransactions;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public String getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(String evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }
}
