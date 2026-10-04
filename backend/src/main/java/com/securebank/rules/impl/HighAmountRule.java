package com.securebank.rules.impl;

import com.securebank.model.Account;
import com.securebank.model.Transaction;
import com.securebank.rules.Rule;
import com.securebank.rules.RuleResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HighAmountRule implements Rule {

    public static final String RULE_ID = "RULE_HIGH_AMOUNT";
    public static final String RULE_NAME = "High Transaction Amount";
    public static final int RISK_POINTS = 25;

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
        double avg = (account != null && account.getAverageTransactionAmount() != null)
                ? account.getAverageTransactionAmount()
                : 3500.0;

        double threshold = avg * 3.0;
        double amount = transaction.getAmount() != null ? transaction.getAmount() : 0.0;

        boolean triggered = amount >= threshold;

        String explanation = triggered
                ? String.format("Transaction amount (₹%,.2f) is more than 3 times the account average (₹%,.2f).", amount, avg)
                : String.format("Transaction amount (₹%,.2f) is within normal historical spending range.", amount);

        return new RuleResult(RULE_ID, RULE_NAME, triggered, triggered ? RISK_POINTS : 0, explanation);
    }
}
