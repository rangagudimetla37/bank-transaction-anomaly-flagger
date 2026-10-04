package com.securebank.graph;

public class GraphNetworkStats {

    private int totalTransactions;
    private int uniqueRecipients;
    private int uniqueLocations;
    private int totalEdges;
    private String mostFrequentLocation;
    private String mostFrequentRecipient;
    private double averageTransactionsPerRecipient;

    public GraphNetworkStats() {
    }

    public GraphNetworkStats(int totalTransactions, int uniqueRecipients, int uniqueLocations,
                             int totalEdges, String mostFrequentLocation, String mostFrequentRecipient,
                             double averageTransactionsPerRecipient) {
        this.totalTransactions = totalTransactions;
        this.uniqueRecipients = uniqueRecipients;
        this.uniqueLocations = uniqueLocations;
        this.totalEdges = totalEdges;
        this.mostFrequentLocation = mostFrequentLocation;
        this.mostFrequentRecipient = mostFrequentRecipient;
        this.averageTransactionsPerRecipient = averageTransactionsPerRecipient;
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(int totalTransactions) {
        this.totalTransactions = totalTransactions;
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

    public int getTotalEdges() {
        return totalEdges;
    }

    public void setTotalEdges(int totalEdges) {
        this.totalEdges = totalEdges;
    }

    public String getMostFrequentLocation() {
        return mostFrequentLocation;
    }

    public void setMostFrequentLocation(String mostFrequentLocation) {
        this.mostFrequentLocation = mostFrequentLocation;
    }

    public String getMostFrequentRecipient() {
        return mostFrequentRecipient;
    }

    public void setMostFrequentRecipient(String mostFrequentRecipient) {
        this.mostFrequentRecipient = mostFrequentRecipient;
    }

    public double getAverageTransactionsPerRecipient() {
        return averageTransactionsPerRecipient;
    }

    public void setAverageTransactionsPerRecipient(double averageTransactionsPerRecipient) {
        this.averageTransactionsPerRecipient = averageTransactionsPerRecipient;
    }
}
