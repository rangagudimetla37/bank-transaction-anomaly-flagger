package com.securebank.model;

import java.time.LocalDateTime;

public class Transaction {
    private Long id;
    private String transactionId;
    private Long accountId;
    private String transactionType;
    private String description;
    private Double amount;
    private String transactionDirection; // "CREDIT" or "DEBIT"
    private String recipient;
    private String location;
    private String timestamp;
    private String status; // "COMPLETED", "PENDING", "FAILED"
    private LocalDateTime createdAt;

    public Transaction() {
    }

    public Transaction(Long id, String transactionId, Long accountId, String transactionType,
                       String description, Double amount, String transactionDirection,
                       String recipient, String location, String timestamp, String status, LocalDateTime createdAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.description = description;
        this.amount = amount;
        this.transactionDirection = transactionDirection;
        this.recipient = recipient;
        this.location = location;
        this.timestamp = timestamp;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
