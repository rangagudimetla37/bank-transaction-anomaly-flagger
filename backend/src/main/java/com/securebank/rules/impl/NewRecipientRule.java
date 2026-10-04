package com.securebank.rules.impl;

import com.securebank.model.Account;
import com.securebank.model.Transaction;
import com.securebank.rules.Rule;
import com.securebank.rules.RuleResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NewRecipientRule implements Rule {

    public static final String RULE_ID = "RULE_NEW_RECIPIENT";
    public static final String RULE_NAME = "New / Unrecognized Recipient";
    public static final int RISK_POINTS = 20;

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
        String recipient = transaction.getRecipient() != null ? transaction.getRecipient().trim() : "";

        if (recipient.isEmpty()) {
            return new RuleResult(RULE_ID, RULE_NAME, false, 0, "No recipient specified for evaluation.");
        }

        boolean seenBefore = false;
        if (history != null) {
            for (Transaction past : history) {
                // Ignore the transaction under evaluation itself
                if (transaction.getTransactionId() != null &&
                        transaction.getTransactionId().equalsIgnoreCase(past.getTransactionId())) {
                    continue;
                }
                if (past.getRecipient() != null && past.getRecipient().trim().equalsIgnoreCase(recipient)) {
                    seenBefore = true;
                    break;
                }
            }
        }

        boolean triggered = !seenBefore;

        String explanation = triggered
                ? String.format("Recipient '%s' is new for this account and has no prior transaction history.", recipient)
                : String.format("Recipient '%s' is recognized from previous account activity.", recipient);

        return new RuleResult(RULE_ID, RULE_NAME, triggered, triggered ? RISK_POINTS : 0, explanation);
    }
}
