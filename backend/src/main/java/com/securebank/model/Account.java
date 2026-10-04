package com.securebank.model;

import java.time.LocalDateTime;

public class Account {
    private Long id;
    private String accountNumber;
    private String bankId;
    private String customerName;
    private String accountType;
    private Double balance;
    private String usualLocation = "Hyderabad";
    private Double averageTransactionAmount = 3500.0;
    private Integer transactionCount = 25;
    private LocalDateTime createdAt;

    public Account() {
    }

    public Account(Long id, String accountNumber, String bankId, String customerName, 
                   String accountType, Double balance, String usualLocation, 
                   Double averageTransactionAmount, Integer transactionCount, LocalDateTime createdAt) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.bankId = bankId;
        this.customerName = customerName;
        this.accountType = accountType;
        this.balance = balance;
        this.usualLocation = usualLocation != null ? usualLocation : "Hyderabad";
        this.averageTransactionAmount = averageTransactionAmount != null ? averageTransactionAmount : 3500.0;
        this.transactionCount = transactionCount != null ? transactionCount : 25;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getUsualLocation() {
        return usualLocation;
    }

    public void setUsualLocation(String usualLocation) {
        this.usualLocation = usualLocation;
    }

    public Double getAverageTransactionAmount() {
        return averageTransactionAmount;
    }

    public void setAverageTransactionAmount(Double averageTransactionAmount) {
        this.averageTransactionAmount = averageTransactionAmount;
    }

    public Integer getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Integer transactionCount) {
        this.transactionCount = transactionCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
