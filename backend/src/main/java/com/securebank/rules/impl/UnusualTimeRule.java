package com.securebank.rules.impl;

import com.securebank.model.Account;
import com.securebank.model.Transaction;
import com.securebank.rules.Rule;
import com.securebank.rules.RuleResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UnusualTimeRule implements Rule {

    public static final String RULE_ID = "RULE_UNUSUAL_TIME";
    public static final String RULE_NAME = "Unusual Transaction Time";
    public static final int RISK_POINTS = 15;

    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{2})\\s*(AM|PM)?", Pattern.CASE_INSENSITIVE);

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

    private int extractHour(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return 12; // default safe daytime hour
        }
        Matcher matcher = TIME_PATTERN.matcher(timestamp);
        if (matcher.find()) {
            try {
                int hour = Integer.parseInt(matcher.group(1));
                String period = matcher.group(3);
                if (period != null) {
                    if ("PM".equalsIgnoreCase(period) && hour < 12) {
                        hour += 12;
                    } else if ("AM".equalsIgnoreCase(period) && hour == 12) {
                        hour = 0;
                    }
                }
                return hour;
            } catch (Exception ignored) {
            }
        }
        return 12;
    }

    @Override
    public RuleResult evaluate(Account account, Transaction transaction, List<Transaction> history) {
        String timestamp = transaction.getTimestamp();
        int hour = extractHour(timestamp);

        // Propositional logic: IF hour in [00:00, 05:00) THEN trigger rule
        boolean triggered = hour >= 0 && hour < 5;

        String explanation = triggered
                ? String.format("Transaction occurred during an unusual late-night time window (%02d:00 hrs, between 00:00 and 05:00).", hour)
                : "Transaction occurred during standard daytime banking hours.";

        return new RuleResult(RULE_ID, RULE_NAME, triggered, triggered ? RISK_POINTS : 0, explanation);
    }
}
