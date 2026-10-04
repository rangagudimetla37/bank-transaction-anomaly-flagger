package com.securebank.service;

import com.securebank.dto.TransactionAnalysisResult;
import com.securebank.model.Account;
import com.securebank.model.Transaction;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.TransactionRepository;
import com.securebank.rules.RuleEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Rational Decision Agent for Rule-Based Transaction Anomaly Screening.
 *
 * Implements the Rational Agent Decision Loop:
 * 1. PERCEIVE: Observe current transaction environment and historical account ledger.
 * 2. REASON:   Evaluate propositional rules via the RuleEngine.
 * 3. DECIDE:   Formulate a preliminary risk classification and score.
 * 4. ACT:      Return structured analysis result with explainability.
 */
@Service
public class TransactionAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionAnalysisService.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final RuleEngine ruleEngine;
    private final PythonAnomalyService pythonAnomalyService;

    public TransactionAnalysisService(AccountRepository accountRepository,
                                      TransactionRepository transactionRepository,
                                      RuleEngine ruleEngine,
                                      PythonAnomalyService pythonAnomalyService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ruleEngine = ruleEngine;
        this.pythonAnomalyService = pythonAnomalyService;
    }

    /**
     * Executes the rational agent cycle for a single customer transaction.
     * Enforces strict customer ownership isolation.
     *
     * @param bankId authenticated customer Bank ID
     * @param transactionId unique transaction identifier
     * @return Optional containing TransactionAnalysisResult if authorized and found
     */
    public Optional<TransactionAnalysisResult> analyzeTransaction(String bankId, String transactionId) {
        // Step 1: PERCEIVE - Verify customer account and load records
        Optional<Account> accountOpt = accountRepository.findByBankId(bankId);
        if (accountOpt.isEmpty()) {
            logger.warn("Analysis requested for unknown bankId: {}", bankId);
            return Optional.empty();
        }

        Account account = accountOpt.get();

        // Customer isolation: Must belong to customer's account
        Optional<Transaction> transactionOpt = transactionRepository.findByTransactionIdAndAccountId(transactionId, account.getId());
        if (transactionOpt.isEmpty()) {
            logger.warn("Transaction {} not found or does not belong to accountId {}", transactionId, account.getId());
            return Optional.empty();
        }

        Transaction transaction = transactionOpt.get();
        List<Transaction> history = transactionRepository.findByAccountId(account.getId());

        // Calculate dynamic average transaction amount if history exists
        if (!history.isEmpty()) {
            double sum = 0.0;
            int count = 0;
            for (Transaction past : history) {
                if (past.getAmount() != null && past.getAmount() > 0) {
                    sum += past.getAmount();
                    count++;
                }
            }
            if (count > 0) {
                account.setAverageTransactionAmount(sum / count);
                account.setTransactionCount(count);
            }
        }

        // Step 2: REASON - Evaluate propositional rules & Python statistical outlier detector
        logger.info("Evaluating propositional rules for transaction: {}", transactionId);
        TransactionAnalysisResult result = ruleEngine.evaluate(account, transaction, history);

        logger.info("Invoking Python statistical anomaly detector (Z-score) for transaction: {}", transactionId);
        var statResult = pythonAnomalyService.detectAnomaly(account, transaction, history);

        // Step 3: DECIDE - Integrate statistical insights
        result.setZScore(statResult.getZScore());
        result.setStatisticalScore(statResult.getStatisticalScore());
        result.setStatisticalAnomaly(statResult.getIsAnomaly());
        result.setHistoricalMean(statResult.getHistoricalMean());
        result.setHistoricalStdDev(statResult.getHistoricalStdDev());
        result.setStatisticalExplanation(statResult.getExplanation());

        // Composite risk score: weighted combination (60% rule logic + 40% statistical outlier)
        int composite = (int) Math.min(100, Math.round(0.6 * result.getRuleScore() + 0.4 * statResult.getStatisticalScore()));
        result.setCompositeScore(composite);

        // Step 4: ACT - Return unified analysis result
        return Optional.of(result);
    }

    /**
     * Batch evaluates the recent transactions for dashboard preview.
     */
    public List<TransactionAnalysisResult> analyzeRecentTransactions(String bankId, int limit) {
        Optional<Account> accountOpt = accountRepository.findByBankId(bankId);
        if (accountOpt.isEmpty()) {
            return Collections.emptyList();
        }

        Account account = accountOpt.get();
        List<Transaction> history = transactionRepository.findByAccountId(account.getId());

        List<TransactionAnalysisResult> results = new ArrayList<>();
        int count = Math.min(limit, history.size());

        for (int i = 0; i < count; i++) {
            Transaction txn = history.get(i);
            TransactionAnalysisResult res = ruleEngine.evaluate(account, txn, history);
            var statResult = pythonAnomalyService.detectAnomaly(account, txn, history);
            res.setZScore(statResult.getZScore());
            res.setStatisticalScore(statResult.getStatisticalScore());
            res.setStatisticalAnomaly(statResult.getIsAnomaly());
            res.setHistoricalMean(statResult.getHistoricalMean());
            res.setHistoricalStdDev(statResult.getHistoricalStdDev());
            res.setStatisticalExplanation(statResult.getExplanation());
            int composite = (int) Math.min(100, Math.round(0.6 * res.getRuleScore() + 0.4 * statResult.getStatisticalScore()));
            res.setCompositeScore(composite);
            results.add(res);
        }

        return results;
    }
}
