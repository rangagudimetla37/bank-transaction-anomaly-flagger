package com.securebank.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank(message = "Bank ID cannot be empty.")
    private String bankId;

    @NotBlank(message = "Password cannot be empty.")
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String bankId, String password) {
        this.bankId = bankId;
        this.password = password;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
