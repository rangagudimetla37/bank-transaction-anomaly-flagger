package com.securebank.dto;

public class UserProfileDto {
    private String bankId;
    private String role;
    private String lastLogin;

    public UserProfileDto() {
    }

    public UserProfileDto(String bankId, String role, String lastLogin) {
        this.bankId = bankId;
        this.role = role;
        this.lastLogin = lastLogin;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }
}
