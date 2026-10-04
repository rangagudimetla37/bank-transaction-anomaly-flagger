package com.securebank.dto;

public class TransactionDto {
    private String transactionId;
    private String transactionType;
    private String description;
    private Double amount;
    private String transactionDirection;
    private String recipient;
    private String location;
    private String timestamp;
    private String status;

    public TransactionDto() {
    }

    public TransactionDto(String transactionId, String transactionType, String description,
                          Double amount, String transactionDirection, String recipient,
                          String location, String timestamp, String status) {
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.description = description;
        this.amount = amount;
        this.transactionDirection = transactionDirection;
        this.recipient = recipient;
        this.location = location;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getTransactionDirection() {
        return transactionDirection;
    }

    public void setTransactionDirection(String transactionDirection) {
        this.transactionDirection = transactionDirection;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
