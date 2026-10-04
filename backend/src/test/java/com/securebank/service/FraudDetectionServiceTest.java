package com.securebank.service;

import com.securebank.config.RiskConfig;
import com.securebank.model.RiskLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FraudDetectionServiceTest {

    @Test
    void testRiskFormulaCalculation() {
        // Example 1 from prompt: 80 * 0.60 + 90 * 0.40 = 48 + 36 = 84
        int score1 = RiskConfig.calculateFinalScore(80, 90);
        assertEquals(84, score1);

        // Example 2 from prompt: 75 * 0.60 + 92 * 0.40 = 45 + 36.8 = 81.8 -> 82
        int score2 = RiskConfig.calculateFinalScore(75, 92);
        assertEquals(82, score2);

        // Boundary: 0 and 100
        assertEquals(0, RiskConfig.calculateFinalScore(0, 0));
        assertEquals(100, RiskConfig.calculateFinalScore(100, 100));
    }

    @Test
    void testRiskLevelThresholds() {
        assertEquals(RiskLevel.NORMAL, RiskConfig.determineRiskLevel(0));
        assertEquals(RiskLevel.NORMAL, RiskConfig.determineRiskLevel(29));

        assertEquals(RiskLevel.WATCH, RiskConfig.determineRiskLevel(30));
        assertEquals(RiskLevel.WATCH, RiskConfig.determineRiskLevel(59));

        assertEquals(RiskLevel.SUSPICIOUS, RiskConfig.determineRiskLevel(60));
        assertEquals(RiskLevel.SUSPICIOUS, RiskConfig.determineRiskLevel(79));

        assertEquals(RiskLevel.HIGH_RISK, RiskConfig.determineRiskLevel(80));
        assertEquals(RiskLevel.HIGH_RISK, RiskConfig.determineRiskLevel(100));
    }

    @Test
    void testOperationalRecommendations() {
        assertEquals("Recommended for manual review.", RiskConfig.determineRecommendation(RiskLevel.HIGH_RISK));
        assertEquals("Additional verification is recommended.", RiskConfig.determineRecommendation(RiskLevel.SUSPICIOUS));
        assertEquals("Monitor transaction activity.", RiskConfig.determineRecommendation(RiskLevel.WATCH));
        assertEquals("No immediate anomaly detected.", RiskConfig.determineRecommendation(RiskLevel.NORMAL));
    }

    @Test
    void testAlertGenerationThreshold() {
        assertTrue(82 >= RiskConfig.ALERT_GENERATION_THRESHOLD);
        assertTrue(60 >= RiskConfig.ALERT_GENERATION_THRESHOLD);
        assertFalse(59 >= RiskConfig.ALERT_GENERATION_THRESHOLD);
        assertFalse(25 >= RiskConfig.ALERT_GENERATION_THRESHOLD);
    }
}
