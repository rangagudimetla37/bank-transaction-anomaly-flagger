package com.securebank.rules;

public class RuleResult {
    private final String ruleId;
    private final String ruleName;
    private final boolean triggered;
    private final int riskPoints;
    private final String explanation;

    public RuleResult(String ruleId, String ruleName, boolean triggered, int riskPoints, String explanation) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.triggered = triggered;
        this.riskPoints = riskPoints;
        this.explanation = explanation;
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public int getRiskPoints() {
        return riskPoints;
    }

    public String getExplanation() {
        return explanation;
    }
}
