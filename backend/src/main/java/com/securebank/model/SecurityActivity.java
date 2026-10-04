package com.securebank.model;

public class SecurityActivity {
    private Long id;
    private String bankId;
    private String activityType;
    private String description;
    private String ipAddress;
    private String timestamp;

    public SecurityActivity() {
    }

    public SecurityActivity(Long id, String bankId, String activityType, String description, String ipAddress, String timestamp) {
        this.id = id;
        this.bankId = bankId;
        this.activityType = activityType;
        this.description = description;
        this.ipAddress = ipAddress;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
