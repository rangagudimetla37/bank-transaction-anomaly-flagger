package com.securebank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {
    @NotBlank(message = "Reset token is required.")
    private String resetToken;

    @NotBlank(message = "Bank ID cannot be empty.")
    private String bankId;

    @NotBlank(message = "New password cannot be empty.")
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    private String newPassword;

    @NotBlank(message = "Confirm password cannot be empty.")
    private String confirmPassword;

    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(String resetToken, String bankId, String newPassword, String confirmPassword) {
        this.resetToken = resetToken;
        this.bankId = bankId;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
