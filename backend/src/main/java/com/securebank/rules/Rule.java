package com.securebank.rules;

import com.securebank.model.Account;
import com.securebank.model.Transaction;
import java.util.List;

/**
 * Common abstraction for all transaction anomaly screening rules.
 * Demonstrates polymorphism, encapsulation, and propositional logic screening.
 */
public interface Rule {

    /**
     * Unique identifier for the rule (e.g. RULE_HIGH_AMOUNT).
     */
    String getRuleId();

    /**
     * Human-readable rule name.
     */
    String getRuleName();

    /**
     * Risk points contributed when the rule's propositional condition is satisfied.
     */
    int getRiskPoints();

    /**
     * Evaluates the rule against the target transaction, customer account profile, and historical activity.
     *
     * @param account customer account profile
     * @param transaction target transaction under analysis
     * @param history prior transactions for context (new recipient, burst detection, etc.)
     * @return RuleResult containing evaluation outcome, points, and explanation
     */
    RuleResult evaluate(Account account, Transaction transaction, List<Transaction> history);
}
