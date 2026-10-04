package com.securebank.dto;

import com.securebank.model.SecurityActivity;
import java.util.List;

public class SecuritySummaryDto {
    private String securityStatus;
    private String lastLogin;
    private String passwordLastChanged;
    private List<SecurityActivity> activities;

    public SecuritySummaryDto() {
    }

    public SecuritySummaryDto(String securityStatus, String lastLogin, String passwordLastChanged, List<SecurityActivity> activities) {
        this.securityStatus = securityStatus;
        this.lastLogin = lastLogin;
        this.passwordLastChanged = passwordLastChanged;
        this.activities = activities;
    }

    public String getSecurityStatus() {
        return securityStatus;
    }

    public void setSecurityStatus(String securityStatus) {
        this.securityStatus = securityStatus;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getPasswordLastChanged() {
        return passwordLastChanged;
    }

    public void setPasswordLastChanged(String passwordLastChanged) {
        this.passwordLastChanged = passwordLastChanged;
    }

    public List<SecurityActivity> getActivities() {
        return activities;
    }

    public void setActivities(List<SecurityActivity> activities) {
        this.activities = activities;
    }
}
