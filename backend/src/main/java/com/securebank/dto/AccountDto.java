package com.securebank.dto;

public class AccountDto {
    private String accountNumber;
    private String customerName;
    private String bankId;
    private String accountType;
    private Double balance;
    private String createdAt;

    public AccountDto() {
    }

    public AccountDto(String accountNumber, String customerName, String bankId, String accountType, Double balance, String createdAt) {
        this.accountNumber = accountNumber;
        this.customerName = customerName;
        this.bankId = bankId;
        this.accountType = accountType;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
