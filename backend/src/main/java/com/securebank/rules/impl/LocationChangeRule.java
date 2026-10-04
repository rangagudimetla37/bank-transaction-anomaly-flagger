package com.securebank.rules.impl;

import com.securebank.model.Account;
import com.securebank.model.Transaction;
import com.securebank.rules.Rule;
import com.securebank.rules.RuleResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocationChangeRule implements Rule {

    public static final String RULE_ID = "RULE_LOCATION_CHANGE";
    public static final String RULE_NAME = "Location Change";
    public static final int RISK_POINTS = 15;

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public String getRuleName() {
        return RULE_NAME;
    }

    @Override
    public int getRiskPoints() {
        return RISK_POINTS;
    }

    @Override
    public RuleResult evaluate(Account account, Transaction transaction, List<Transaction> history) {
        String usualLocation = (account != null && account.getUsualLocation() != null)
                ? account.getUsualLocation().trim()
                : "Hyderabad";

        String txnLocation = transaction.getLocation() != null ? transaction.getLocation().trim() : "";

        // Propositional logic: IF location != usualLocation THEN trigger
        boolean triggered = !txnLocation.isEmpty() && !txnLocation.equalsIgnoreCase(usualLocation);

        String explanation = triggered
                ? String.format("Transaction location ('%s') differs from the customer's usual location ('%s').", txnLocation, usualLocation)
                : String.format("Transaction location matches the customer's usual registered location ('%s').", usualLocation);

        return new RuleResult(RULE_ID, RULE_NAME, triggered, triggered ? RISK_POINTS : 0, explanation);
    }
}
