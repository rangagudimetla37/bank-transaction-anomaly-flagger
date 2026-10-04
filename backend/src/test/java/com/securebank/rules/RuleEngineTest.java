package com.securebank.rules;

import com.securebank.dto.TransactionAnalysisResult;
import com.securebank.model.Account;
import com.securebank.model.RiskLevel;
import com.securebank.model.Transaction;
import com.securebank.rules.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RuleEngineTest {

    private RuleEngine ruleEngine;
    private Account testAccount;
    private List<Transaction> testHistory;

    @BeforeEach
    public void setUp() {
        // Construct engine with all 5 polymorphic rules
        List<Rule> rules = List.of(
                new HighAmountRule(),
                new NewRecipientRule(),
                new UnusualTimeRule(),
                new LocationChangeRule(),
                new RapidTransactionRule()
        );
        ruleEngine = new RuleEngine(rules);

        // Standard customer test account
        testAccount = new Account();
        testAccount.setAccountNumber("ACC100001");
        testAccount.setBankId("SB100001");
        testAccount.setCustomerName("Rajesh Sharma");
        testAccount.setUsualLocation("Hyderabad");
        testAccount.setAverageTransactionAmount(3500.0);
        testAccount.setTransactionCount(20);

        // Historical ledger for the account
        testHistory = new ArrayList<>();
        Transaction past1 = new Transaction();
        past1.setTransactionId("TXN-HIST-001");
        past1.setAmount(1200.0);
        past1.setRecipient("More Megastore");
        past1.setLocation("Hyderabad");
        past1.setTimestamp("01 Oct 2026, 11:30 AM");
        past1.setStatus("COMPLETED");

        Transaction past2 = new Transaction();
        past2.setTransactionId("TXN-HIST-002");
        past2.setAmount(2500.0);
        past2.setRecipient("Jio Prepaid");
        past2.setLocation("Hyderabad");
        past2.setTimestamp("02 Oct 2026, 04:15 PM");
        past2.setStatus("COMPLETED");

        testHistory.add(past1);
        testHistory.add(past2);
    }

    @Test
    @DisplayName("1. Normal transaction should trigger no rules and result in NORMAL risk")
    public void testNormalTransaction() {
        Transaction txn = new Transaction();
        txn.setTransactionId("TXN-NORMAL-001");
        txn.setAmount(1500.0); // well under 3x avg (10,500)
        txn.setRecipient("More Megastore"); // recognized recipient
        txn.setLocation("Hyderabad"); // usual location
        txn.setTimestamp("04 Oct 2026, 02:30 PM"); // daytime
        txn.setStatus("COMPLETED");

        TransactionAnalysisResult result = ruleEngine.evaluate(testAccount, txn, testHistory);

        assertNotNull(result);
        assertEquals(0, result.getRuleScore(), "Score should be 0 for standard transaction");
        assertEquals(RiskLevel.NORMAL, result.getRiskLevel());
        assertTrue(result.getTriggeredRules().isEmpty(), "No rules should trigger");
    }

    @Test
    @DisplayName("2. High amount rule triggers when amount >= 3x average")
    public void testHighAmountRule() {
        Transaction txn = new Transaction();
        txn.setTransactionId("TXN-HIGH-001");
        txn.setAmount(15000.0); // 15,000 >= 3,500 * 3 = 10,500
        txn.setRecipient("More Megastore");
        txn.setLocation("Hyderabad");
        txn.setTimestamp("04 Oct 2026, 11:00 AM");

        TransactionAnalysisResult result = ruleEngine.evaluate(testAccount, txn, testHistory);

        assertTrue(result.getTriggeredRules().contains(HighAmountRule.RULE_ID));
        assertTrue(result.getRuleScore() >= 25);
    }

    @Test
    @DisplayName("3. New recipient rule triggers for unrecognized merchant")
    public void testNewRecipientRule() {
        Transaction txn = new Transaction();
        txn.setTransactionId("TXN-NEW-REC-001");
        txn.setAmount(1000.0);
        txn.setRecipient("Unknown Offshore Electronics"); // Never seen in history
        txn.setLocation("Hyderabad");
        txn.setTimestamp("04 Oct 2026, 03:00 PM");

        TransactionAnalysisResult result = ruleEngine.evaluate(testAccount, txn, testHistory);

        assertTrue(result.getTriggeredRules().contains(NewRecipientRule.RULE_ID));
        assertTrue(result.getRuleScore() >= 20);
    }

    @Test
    @DisplayName("4. Unusual time rule triggers between 00:00 and 05:00")
    public void testUnusualTimeRule() {
        Transaction txn = new Transaction();
        txn.setTransactionId("TXN-NIGHT-001");
        txn.setAmount(800.0);
        txn.setRecipient("More Megastore");
        txn.setLocation("Hyderabad");
        txn.setTimestamp("04 Oct 2026, 02:41 AM"); // 02:41 AM late night

        TransactionAnalysisResult result = ruleEngine.evaluate(testAccount, txn, testHistory);

        assertTrue(result.getTriggeredRules().contains(UnusualTimeRule.RULE_ID));
        assertTrue(result.getRuleScore() >= 15);
    }

    @Test
    @DisplayName("5. Location change rule triggers when city differs from usual location")
    public void testLocationChangeRule() {
        Transaction txn = new Transaction();
        txn.setTransactionId("TXN-LOC-001");
        txn.setAmount(950.0);
        txn.setRecipient("More Megastore");
        txn.setLocation("Mumbai"); // Account usual is Hyderabad
        txn.setTimestamp("04 Oct 2026, 01:15 PM");

        TransactionAnalysisResult result = ruleEngine.evaluate(testAccount, txn, testHistory);

        assertTrue(result.getTriggeredRules().contains(LocationChangeRule.RULE_ID));
        assertTrue(result.getRuleScore() >= 15);
    }

    @Test
    @DisplayName("6. Rapid transactions rule triggers within short time interval")
    public void testRapidTransactionRule() {
        // Add a recent transaction at 03:10 PM
        Transaction rapidPast = new Transaction();
        rapidPast.setTransactionId("TXN-BURST-1");
        rapidPast.setAmount(1000.0);
        rapidPast.setRecipient("More Megastore");
        rapidPast.setLocation("Hyderabad");
        rapidPast.setTimestamp("04 Oct 2026, 03:10 PM");
        testHistory.add(rapidPast);

        // Target transaction occurs at 03:12 PM (2 minutes diff)
        Transaction txn = new Transaction();
        txn.setTransactionId("TXN-BURST-2");
        txn.setAmount(1200.0);
        txn.setRecipient("More Megastore");
        txn.setLocation("Hyderabad");
        txn.setTimestamp("04 Oct 2026, 03:12 PM");

        TransactionAnalysisResult result = ruleEngine.evaluate(testAccount, txn, testHistory);

        assertTrue(result.getTriggeredRules().contains(RapidTransactionRule.RULE_ID));
        assertTrue(result.getRuleScore() >= 15);
    }

    @Test
    @DisplayName("7. Multiple rules triggering accumulates points and raises risk level")
    public void testMultipleRulesTriggered() {
        // Combination: High Amount (25) + New Recipient (20) + Unusual Time (15) + Location Change (15) = 75 points
        Transaction txn = new Transaction();
        txn.setTransactionId("TXN-MULTI-001");
        txn.setAmount(48500.0); // High amount
        txn.setRecipient("Global Luxury Imports"); // New recipient
        txn.setLocation("Mumbai"); // Location change
        txn.setTimestamp("04 Oct 2026, 02:41 AM"); // Late night

        TransactionAnalysisResult result = ruleEngine.evaluate(testAccount, txn, testHistory);

        assertEquals(75, result.getRuleScore());
        assertEquals(RiskLevel.SUSPICIOUS, result.getRiskLevel());
        assertEquals(4, result.getTriggeredRules().size());
        assertEquals(4, result.getExplanations().size());
    }

    @Test
    @DisplayName("8. Risk classification thresholds match specification boundaries")
    public void testRiskClassificationBoundaries() {
        assertEquals(RiskLevel.NORMAL, RiskLevel.fromScore(0));
        assertEquals(RiskLevel.NORMAL, RiskLevel.fromScore(29));

        assertEquals(RiskLevel.WATCH, RiskLevel.fromScore(30));
        assertEquals(RiskLevel.WATCH, RiskLevel.fromScore(59));

        assertEquals(RiskLevel.SUSPICIOUS, RiskLevel.fromScore(60));
        assertEquals(RiskLevel.SUSPICIOUS, RiskLevel.fromScore(79));

        assertEquals(RiskLevel.HIGH_RISK, RiskLevel.fromScore(80));
        assertEquals(RiskLevel.HIGH_RISK, RiskLevel.fromScore(100));
    }
}
