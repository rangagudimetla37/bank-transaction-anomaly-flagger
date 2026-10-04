package com.securebank.rules.impl;

import com.securebank.model.Account;
import com.securebank.model.Transaction;
import com.securebank.rules.Rule;
import com.securebank.rules.RuleResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Component
public class RapidTransactionRule implements Rule {

    public static final String RULE_ID = "RULE_RAPID_TRANSACTION";
    public static final String RULE_NAME = "Rapid Transactions";
    public static final int RISK_POINTS = 15;

    private static final DateTimeFormatter FORMATTER_12 = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter FORMATTER_24 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

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

    private LocalDateTime parseTime(String ts) {
        if (ts == null || ts.isBlank()) return null;
        try {
            return LocalDateTime.parse(ts.trim(), FORMATTER_12);
        } catch (Exception e1) {
            try {
                return LocalDateTime.parse(ts.trim(), FORMATTER_24);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    @Override
    public RuleResult evaluate(Account account, Transaction transaction, List<Transaction> history) {
        LocalDateTime targetTime = parseTime(transaction.getTimestamp());

        boolean triggered = false;
        int nearbyCount = 0;

        if (targetTime != null && history != null) {
            for (Transaction other : history) {
                if (transaction.getTransactionId() != null &&
                        transaction.getTransactionId().equalsIgnoreCase(other.getTransactionId())) {
                    continue;
                }
                LocalDateTime otherTime = parseTime(other.getTimestamp());
                if (otherTime != null) {
                    long diffMinutes = Math.abs(ChronoUnit.MINUTES.between(targetTime, otherTime));
                    // Check if another transaction occurred within 5 minutes
                    if (diffMinutes <= 5) {
                        nearbyCount++;
                    }
                }
            }
            triggered = nearbyCount >= 1; // At least one other transaction within 5 minutes indicates rapid activity
        }

        String explanation = triggered
                ? String.format("Multiple transactions were detected within a short time interval (%d other transaction(s) within 5 minutes).", nearbyCount)
                : "Transaction cadence is within standard operational intervals.";

        return new RuleResult(RULE_ID, RULE_NAME, triggered, triggered ? RISK_POINTS : 0, explanation);
    }
}
