package com.securebank.dto;

import java.util.List;

public class TransactionSummaryDto {
    private Double balance;
    private Integer totalTransactions;
    private Double moneyIn;
    private Double moneyOut;
    private String customerName;
    private String accountNumber;
    private List<TransactionDto> transactions;

    public TransactionSummaryDto() {
    }

    public TransactionSummaryDto(Double balance, Integer totalTransactions, Double moneyIn, Double moneyOut,
                                 String customerName, String accountNumber, List<TransactionDto> transactions) {
        this.balance = balance;
        this.totalTransactions = totalTransactions;
        this.moneyIn = moneyIn;
        this.moneyOut = moneyOut;
        this.customerName = customerName;
        this.accountNumber = accountNumber;
        this.transactions = transactions;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Integer getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(Integer totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public Double getMoneyIn() {
        return moneyIn;
    }

    public void setMoneyIn(Double moneyIn) {
        this.moneyIn = moneyIn;
    }

    public Double getMoneyOut() {
        return moneyOut;
    }

    public void setMoneyOut(Double moneyOut) {
        this.moneyOut = moneyOut;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public List<TransactionDto> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionDto> transactions) {
        this.transactions = transactions;
    }
}
