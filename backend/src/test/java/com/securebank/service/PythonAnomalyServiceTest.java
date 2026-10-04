package com.securebank.service;

import com.securebank.dto.StatisticalAnomalyResult;
import com.securebank.model.Account;
import com.securebank.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PythonAnomalyServiceTest {

    private PythonAnomalyService pythonAnomalyService;
    private Account account;
    private List<Transaction> history;

    @BeforeEach
    void setUp() {
        pythonAnomalyService = new PythonAnomalyService();
        account = new Account();
        account.setId(1L);
        account.setBankId("SB100001");
        account.setCustomerName("Rajesh Kumar");

        history = new ArrayList<>();
        history.add(createTransaction("TXN001", 2500.0, "2026-10-01 10:15"));
        history.add(createTransaction("TXN002", 3200.0, "2026-10-02 11:20"));
        history.add(createTransaction("TXN003", 2800.0, "2026-10-03 09:15"));
    }

    private Transaction createTransaction(String id, double amount, String timestamp) {
        Transaction t = new Transaction();
        t.setTransactionId(id);
        t.setAccountId(1L);
        t.setAmount(amount);
        t.setTimestamp(timestamp);
        return t;
    }

    @Test
    void testNormalTransactionZScore() {
        Transaction target = createTransaction("TXN004", 2900.0, "2026-10-04 12:00");
        StatisticalAnomalyResult result = pythonAnomalyService.detectAnomaly(account, target, history);

        assertNotNull(result);
        assertFalse(result.getIsAnomaly());
        assertTrue(result.getAbsZScore() < 2.5);
        assertTrue(result.getStatisticalScore() < 50);
        assertTrue(result.getHistoricalMean() > 0);
    }

    @Test
    void testOutlierTransactionZScore() {
        Transaction target = createTransaction("TXN_OUTLIER", 95000.0, "2026-10-04 02:41");
        StatisticalAnomalyResult result = pythonAnomalyService.detectAnomaly(account, target, history);

        assertNotNull(result);
        assertTrue(result.getIsAnomaly(), "Expected statistical anomaly flag to be true for amount 95,000");
        assertTrue(result.getAbsZScore() >= 2.5, "Z-score should exceed threshold");
        assertEquals(100, result.getStatisticalScore(), "Extreme outlier should cap at score 100");
    }

    @Test
    void testInsufficientHistoryEdgeCase() {
        List<Transaction> shortHistory = List.of(createTransaction("TXN001", 2000.0, "2026-10-01 10:00"));
        Transaction target = createTransaction("TXN002", 50000.0, "2026-10-02 10:00");

        StatisticalAnomalyResult result = pythonAnomalyService.calculateJavaFallback(account, target, shortHistory);
        assertNotNull(result);
        assertFalse(result.getIsAnomaly());
        assertEquals(0, result.getStatisticalScore());
        assertEquals(0.0, result.getZScore());
        assertTrue(result.getExplanation().contains("Insufficient transaction history"));
    }

    @Test
    void testZeroStandardDeviationEdgeCase() {
        List<Transaction> uniformHistory = List.of(
                createTransaction("TXN001", 1000.0, "2026-10-01 10:00"),
                createTransaction("TXN002", 1000.0, "2026-10-02 10:00"),
                createTransaction("TXN003", 1000.0, "2026-10-03 10:00")
        );
        Transaction matchingTarget = createTransaction("TXN004", 1000.0, "2026-10-04 10:00");

        StatisticalAnomalyResult result = pythonAnomalyService.calculateJavaFallback(account, matchingTarget, uniformHistory);
        assertNotNull(result);
        assertFalse(result.getIsAnomaly());
        assertEquals(0, result.getStatisticalScore());
        assertEquals(0.0, result.getZScore());
    }
}
