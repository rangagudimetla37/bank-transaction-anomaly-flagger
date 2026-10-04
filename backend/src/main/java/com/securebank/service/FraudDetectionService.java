package com.securebank.service;

import com.securebank.config.RiskConfig;
import com.securebank.dto.FinalRiskAnalysis;
import com.securebank.dto.StatisticalAnomalyResult;
import com.securebank.model.Account;
import com.securebank.model.FraudAlert;
import com.securebank.model.RiskLevel;
import com.securebank.model.Transaction;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.FraudAlertRepository;
import com.securebank.repository.TransactionRepository;
import com.securebank.rules.RuleEngine;
import com.securebank.rules.RuleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Phase 5: Central Fraud and Anomaly Detection Orchestrator.
 *
 * Responsibilities:
 * 1. Enforce strict authenticated customer ownership.
 * 2. Execute Java Propositional RuleEngine (60% weight).
 * 3. Execute Python Statistical Anomaly Detector (40% weight).
 * 4. Combine into Final Risk Score and Risk Level.
 * 5. Synthesize explainable reasons and review recommendations.
 * 6. Generate/update persistent fraud alerts for Suspicious and High-Risk transactions.
 * 7. Provide alert management (VIEWED, REVIEW_REQUIRED).
 */
@Service
public class FraudDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(FraudDetectionService.class);
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final FraudAlertRepository fraudAlertRepository;
    private final com.securebank.repository.UserRepository userRepository;
    private final RuleEngine ruleEngine;
    private final PythonAnomalyService pythonAnomalyService;

    // Simple cache to prevent redundant Python executions on repeated page refreshes
    private final Map<String, FinalRiskAnalysis> analysisCache = new ConcurrentHashMap<>();

    public FraudDetectionService(AccountRepository accountRepository,
                                 TransactionRepository transactionRepository,
                                 FraudAlertRepository fraudAlertRepository,
                                 com.securebank.repository.UserRepository userRepository,
                                 RuleEngine ruleEngine,
                                 PythonAnomalyService pythonAnomalyService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.fraudAlertRepository = fraudAlertRepository;
        this.userRepository = userRepository;
        this.ruleEngine = ruleEngine;
        this.pythonAnomalyService = pythonAnomalyService;
    }

    /**
     * Conducts complete composite risk analysis for a customer's transaction.
     */
    public Optional<FinalRiskAnalysis> evaluateFinalRisk(String bankId, String transactionId) {
        // Customer isolation verification
        Optional<Account> accountOpt = accountRepository.findByBankId(bankId);
        if (accountOpt.isEmpty()) {
            logger.warn("Risk analysis failed: unknown bankId {}", bankId);
            return Optional.empty();
        }

        Account account = accountOpt.get();
        Optional<Transaction> transactionOpt = transactionRepository.findByTransactionIdAndAccountId(transactionId, account.getId());
        if (transactionOpt.isEmpty()) {
            logger.warn("Risk analysis failed: txn {} does not belong to bankId {}", transactionId, bankId);
            return Optional.empty();
        }

        return evaluateFinalRiskInternal(account, transactionOpt.get());
    }

    /**
     * Internal/Reviewer risk analysis by transactionId regardless of bankId.
     */
    public Optional<FinalRiskAnalysis> evaluateFinalRiskByTransactionId(String transactionId) {
        if (analysisCache.containsKey(transactionId)) {
            return Optional.of(analysisCache.get(transactionId));
        }
        Optional<Transaction> txnOpt = transactionRepository.findByTransactionId(transactionId);
        if (txnOpt.isEmpty()) {
            return Optional.empty();
        }
        Transaction txn = txnOpt.get();
        Optional<Account> accOpt = accountRepository.findById(txn.getAccountId());
        if (accOpt.isEmpty()) {
            return Optional.empty();
        }
        return evaluateFinalRiskInternal(accOpt.get(), txn);
    }

    private Optional<FinalRiskAnalysis> evaluateFinalRiskInternal(Account account, Transaction transaction) {
        String transactionId = transaction.getTransactionId();
        if (analysisCache.containsKey(transactionId)) {
            logger.debug("Returning cached risk analysis for transaction {}", transactionId);
            return Optional.of(analysisCache.get(transactionId));
        }

        List<Transaction> history = transactionRepository.findByAccountId(account.getId());

        // Dynamic average transaction update
        if (!history.isEmpty()) {
            double sum = 0.0;
            int count = 0;
            for (Transaction t : history) {
                if (t.getAmount() != null && t.getAmount() > 0) {
                    sum += t.getAmount();
                    count++;
                }
            }
            if (count > 0) {
                account.setAverageTransactionAmount(sum / count);
                account.setTransactionCount(count);
            }
        }

        // 1. Run Phase 3 Java Propositional Rule Engine
        logger.info("Executing Phase 3 Rule Engine for transaction: {}", transactionId);
        var ruleAnalysis = ruleEngine.evaluate(account, transaction, history);
        int ruleScore = ruleAnalysis.getRuleScore();

        // 2. Run Phase 4 Python Statistical Anomaly Detector
        logger.info("Executing Phase 4 Python Statistical Anomaly Detector for transaction: {}", transactionId);
        StatisticalAnomalyResult statResult = pythonAnomalyService.detectAnomaly(account, transaction, history);
        int statisticalScore = statResult != null && statResult.getStatisticalScore() != null
                ? statResult.getStatisticalScore() : 0;

        // 3. Calculate Final Combined Risk Score: (Rule * 0.60) + (Stat * 0.40)
        int finalRiskScore = RiskConfig.calculateFinalScore(ruleScore, statisticalScore);
        RiskLevel finalRiskLevel = RiskConfig.determineRiskLevel(finalRiskScore);

        // 4. Synthesize diagnostic reasons
        List<String> reasons = new ArrayList<>();
        List<String> triggeredRuleIds = new ArrayList<>();

        if (ruleAnalysis.getRuleResults() != null) {
            for (RuleResult rr : ruleAnalysis.getRuleResults()) {
                if (rr.isTriggered()) {
                    reasons.add(rr.getExplanation());
                    triggeredRuleIds.add(rr.getRuleId());
                }
            }
        }

        boolean isStatAnomaly = statResult != null && Boolean.TRUE.equals(statResult.getIsAnomaly());
        String statClassification = isStatAnomaly ? "STATISTICAL_OUTLIER" : "STATISTICALLY_NORMAL";

        if (isStatAnomaly && statResult.getExplanation() != null) {
            reasons.add(statResult.getExplanation());
        }

        if (reasons.isEmpty()) {
            reasons.add("Transaction conforms to standard customer spending baseline and location profile.");
        }

        // 5. Determine Operational Recommendation
        String recommendation = RiskConfig.determineRecommendation(finalRiskLevel);
        String analyzedAt = LocalDateTime.now().format(DISPLAY_FORMATTER);

        FinalRiskAnalysis finalAnalysis = new FinalRiskAnalysis(
                transactionId,
                ruleScore,
                statisticalScore,
                finalRiskScore,
                finalRiskLevel,
                triggeredRuleIds,
                isStatAnomaly,
                statClassification,
                reasons,
                recommendation,
                analyzedAt
        );

        finalAnalysis.setAmount(transaction.getAmount());
        if (statResult != null) {
            finalAnalysis.setZScore(statResult.getZScore());
            finalAnalysis.setHistoricalMean(statResult.getHistoricalMean());
            finalAnalysis.setHistoricalStdDev(statResult.getHistoricalStdDev());
        }

        // 6. Automated Alert Generation (if finalRiskScore >= 60: SUSPICIOUS or HIGH_RISK)
        if (finalRiskScore >= RiskConfig.ALERT_GENERATION_THRESHOLD) {
            FraudAlert alert = processAlertForTransaction(account, transaction, finalRiskScore, finalRiskLevel, reasons);
            finalAnalysis.setAlertId(alert.getAlertId());
            finalAnalysis.setAlertStatus(alert.getStatus());
        } else {
            // Check if alert previously existed
            Optional<FraudAlert> existing = fraudAlertRepository.findByTransactionId(transactionId);
            existing.ifPresent(a -> {
                finalAnalysis.setAlertId(a.getAlertId());
                finalAnalysis.setAlertStatus(a.getStatus());
            });
        }

        // Store in cache
        analysisCache.put(transactionId, finalAnalysis);

        return Optional.of(finalAnalysis);
    }

    /**
     * Creates or updates a fraud alert for transactions exceeding the alert threshold.
     * Guarantees duplicate prevention.
     */
    private FraudAlert processAlertForTransaction(Account account, Transaction txn, int riskScore, RiskLevel riskLevel, List<String> reasons) {
        String txnId = txn.getTransactionId();
        Optional<FraudAlert> existingOpt = fraudAlertRepository.findByTransactionId(txnId);

        String title = riskLevel == RiskLevel.HIGH_RISK
                ? "HIGH-RISK TRANSACTION ALERT"
                : "SUSPICIOUS TRANSACTION ALERT";

        String reasonSummary = !reasons.isEmpty() ? reasons.get(0) : "Atypical transaction parameters";
        String description = String.format("Transaction %s received a composite risk score of %d/100 (%s). Reason: %s",
                txnId, riskScore, riskLevel.name().replace('_', ' '), reasonSummary);

        FraudAlert alert;
        if (existingOpt.isPresent()) {
            alert = existingOpt.get();
            alert.setRiskScore(riskScore);
            alert.setRiskLevel(riskLevel.name());
            alert.setTitle(title);
            alert.setDescription(description);
            fraudAlertRepository.save(alert);
            logger.info("Updated existing fraud alert {} for transaction {}", alert.getAlertId(), txnId);
        } else {
            String alertId = "ALT-" + txnId.replace("TXN-", "");
            alert = new FraudAlert();
            alert.setAlertId(alertId);
            alert.setTransactionId(txnId);
            alert.setAccountId(account.getId());
            alert.setRiskScore(riskScore);
            alert.setRiskLevel(riskLevel.name());
            alert.setTitle(title);
            alert.setDescription(description);
            alert.setStatus("NEW");
            alert.setCreatedAt(LocalDateTime.now().format(DISPLAY_FORMATTER));
            fraudAlertRepository.save(alert);
            logger.info("Generated new fraud alert {} for transaction {}", alertId, txnId);
        }

        return alert;
    }

    /**
     * Retrieves all fraud alerts for the authenticated customer.
     */
    public List<FraudAlert> getCustomerAlerts(String bankId) {
        Optional<Account> accountOpt = accountRepository.findByBankId(bankId);
        if (accountOpt.isEmpty()) {
            Optional<com.securebank.model.User> userOpt = userRepository.findByBankId(bankId);
            if (userOpt.isPresent() && "REVIEWER".equalsIgnoreCase(userOpt.get().getRole())) {
                return fraudAlertRepository.findAll();
            }
            return Collections.emptyList();
        }
        return fraudAlertRepository.findByAccountId(accountOpt.get().getId());
    }

    /**
     * Retrieves a single alert with customer ownership or reviewer verification.
     */
    public Optional<FraudAlert> getAlertDetail(String bankId, String alertId) {
        Optional<Account> accountOpt = accountRepository.findByBankId(bankId);
        if (accountOpt.isEmpty()) {
            Optional<com.securebank.model.User> userOpt = userRepository.findByBankId(bankId);
            if (userOpt.isPresent() && "REVIEWER".equalsIgnoreCase(userOpt.get().getRole())) {
                return fraudAlertRepository.findByAlertId(alertId);
            }
            return Optional.empty();
        }
        return fraudAlertRepository.findByAlertIdAndAccountId(alertId, accountOpt.get().getId());
    }

    /**
     * Updates alert status (VIEWED, REVIEW_REQUIRED) with customer verification.
     */
    public boolean updateAlertStatus(String bankId, String alertId, String newStatus) {
        Optional<Account> accountOpt = accountRepository.findByBankId(bankId);
        if (accountOpt.isEmpty()) {
            Optional<com.securebank.model.User> userOpt = userRepository.findByBankId(bankId);
            if (userOpt.isPresent() && "REVIEWER".equalsIgnoreCase(userOpt.get().getRole())) {
                Optional<FraudAlert> alertOpt = fraudAlertRepository.findByAlertId(alertId);
                if (alertOpt.isPresent()) {
                    int rows = fraudAlertRepository.updateStatus(alertId, alertOpt.get().getAccountId(), newStatus != null ? newStatus.trim().toUpperCase() : "");
                    return rows > 0;
                }
            }
            return false;
        }

        // Validate allowed status transitions in Phase 5
        String normalizedStatus = newStatus != null ? newStatus.trim().toUpperCase() : "";
        if (!List.of("VIEWED", "REVIEW_REQUIRED", "RESOLVED", "NEW").contains(normalizedStatus)) {
            logger.warn("Invalid alert status update requested: {}", newStatus);
            return false;
        }

        int rows = fraudAlertRepository.updateStatus(alertId, accountOpt.get().getId(), normalizedStatus);
        return rows > 0;
    }

    /**
     * Retrieves counts for dashboard risk summary and notification badge.
     */
    public Map<String, Object> getAlertSummary(String bankId) {
        Map<String, Object> summary = new HashMap<>();
        Optional<Account> accountOpt = accountRepository.findByBankId(bankId);
        if (accountOpt.isEmpty()) {
            Optional<com.securebank.model.User> userOpt = userRepository.findByBankId(bankId);
            if (userOpt.isPresent() && "REVIEWER".equalsIgnoreCase(userOpt.get().getRole())) {
                List<FraudAlert> all = fraudAlertRepository.findAll();
                long newCount = all.stream().filter(a -> "NEW".equalsIgnoreCase(a.getStatus())).count();
                long highRisk = all.stream().filter(a -> "HIGH_RISK".equalsIgnoreCase(a.getRiskLevel())).count();
                long suspicious = all.stream().filter(a -> "SUSPICIOUS".equalsIgnoreCase(a.getRiskLevel())).count();
                summary.put("totalAlerts", all.size());
                summary.put("newAlerts", (int) newCount);
                summary.put("highRiskCount", (int) highRisk);
                summary.put("suspiciousCount", (int) suspicious);
                return summary;
            }
            summary.put("totalAlerts", 0);
            summary.put("newAlerts", 0);
            summary.put("highRiskCount", 0);
            summary.put("suspiciousCount", 0);
            return summary;
        }

        Long accountId = accountOpt.get().getId();
        int newCount = fraudAlertRepository.countNewAlertsByAccountId(accountId);
        int highRisk = fraudAlertRepository.countByAccountIdAndRiskLevel(accountId, "HIGH_RISK");
        int suspicious = fraudAlertRepository.countByAccountIdAndRiskLevel(accountId, "SUSPICIOUS");
        List<FraudAlert> all = fraudAlertRepository.findByAccountId(accountId);

        summary.put("totalAlerts", all.size());
        summary.put("newAlerts", newCount);
        summary.put("highRiskCount", highRisk);
        summary.put("suspiciousCount", suspicious);
        return summary;
    }

    /**
     * Invalidates cache for a transaction (useful for re-testing).
     */
    public void clearCache() {
        analysisCache.clear();
    }
}
