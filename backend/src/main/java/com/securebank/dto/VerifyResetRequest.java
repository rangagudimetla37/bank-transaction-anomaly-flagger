package com.securebank.dto;

import jakarta.validation.constraints.NotBlank;

public class VerifyResetRequest {
    @NotBlank(message = "Bank ID cannot be empty.")
    private String bankId;

    @NotBlank(message = "Security PIN cannot be empty.")
    private String securityPin;

    public VerifyResetRequest() {
    }

    public VerifyResetRequest(String bankId, String securityPin) {
        this.bankId = bankId;
        this.securityPin = securityPin;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getSecurityPin() {
        return securityPin;
    }

    public void setSecurityPin(String securityPin) {
        this.securityPin = securityPin;
    }
}
