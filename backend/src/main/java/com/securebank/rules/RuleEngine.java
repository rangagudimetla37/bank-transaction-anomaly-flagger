package com.securebank.rules;

import com.securebank.dto.TransactionAnalysisResult;
import com.securebank.model.Account;
import com.securebank.model.RiskLevel;
import com.securebank.model.Transaction;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Core Rule Engine for propositional-logic-based transaction anomaly screening.
 * Demonstrates OOP composition, collections, and polymorphic rule evaluation.
 */
@Component
public class RuleEngine {

    private final List<Rule> rules;
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public RuleEngine(List<Rule> rules) {
        this.rules = rules != null ? new ArrayList<>(rules) : new ArrayList<>();
    }

    public List<Rule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    /**
     * Polymorphically executes all configured propositional rules on a transaction.
     *
     * @param account customer account profile
     * @param transaction target transaction
     * @param history historical transactions for context
     * @return TransactionAnalysisResult containing cumulative score, level, and breakdown
     */
    public TransactionAnalysisResult evaluate(Account account, Transaction transaction, List<Transaction> history) {
        List<RuleResult> ruleResults = new ArrayList<>();
        List<String> triggeredRules = new ArrayList<>();
        List<String> explanations = new ArrayList<>();

        int cumulativeScore = 0;

        // Polymorphic evaluation across all Rule implementations
        for (Rule rule : rules) {
            RuleResult result = rule.evaluate(account, transaction, history);
            ruleResults.add(result);

            if (result.isTriggered()) {
                cumulativeScore += result.getRiskPoints();
                triggeredRules.add(result.getRuleId());
                explanations.add(result.getExplanation());
            }
        }

        // Propositional logic combination:
        // Score is capped at 100 for normalization
        int normalizedScore = Math.min(100, Math.max(0, cumulativeScore));

        // Rational agent risk categorization
        RiskLevel riskLevel = RiskLevel.fromScore(normalizedScore);

        String evaluatedAt = LocalDateTime.now().format(DISPLAY_FORMATTER);

        return new TransactionAnalysisResult(
                transaction.getTransactionId(),
                normalizedScore,
                riskLevel,
                triggeredRules,
                ruleResults,
                explanations,
                evaluatedAt
        );
    }
}
