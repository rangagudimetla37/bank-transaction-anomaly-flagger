package com.securebank.service;

import com.securebank.dto.StatisticalAnomalyResult;
import com.securebank.model.Account;
import com.securebank.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Service bridging Java and the Python Statistical Anomaly Detector (Phase 4).
 *
 * Implements the flow:
 * Java -> Export transactions to CSV -> Python anomaly_detector.py -> Read results -> Return metrics
 *
 * Includes graceful mathematical fallback in pure Java if the Python runtime is inaccessible.
 */
@Service
public class PythonAnomalyService {

    private static final Logger logger = LoggerFactory.getLogger(PythonAnomalyService.class);

    @Value("${securebank.python.executable:python}")
    private String pythonExecutable = "python";

    public void setPythonExecutable(String pythonExecutable) {
        this.pythonExecutable = pythonExecutable;
    }

    private static final double Z_THRESHOLD = 2.5;
    private static final int MIN_HISTORY_REQUIRED = 3;

    /**
     * Evaluates a transaction's statistical outlier metrics using Python Z-score engine.
     */
    public StatisticalAnomalyResult detectAnomaly(Account account, Transaction targetTxn, List<Transaction> history) {
        String txnId = targetTxn.getTransactionId();
        String accountId = account.getId() != null ? String.valueOf(account.getId()) : "ACC001";

        try {
            File scriptFile = resolvePythonScript();
            if (scriptFile == null || !scriptFile.exists()) {
                logger.warn("Python script anomaly_detector.py not found on disk. Falling back to Java statistical engine.");
                return calculateJavaFallback(account, targetTxn, history);
            }

            File dataDir = new File(scriptFile.getParentFile(), "data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            // 1. Export transaction ledger to CSV
            File inputFile = new File(dataDir, "export_" + accountId + ".csv");
            File outputFile = new File(dataDir, "results_" + accountId + ".csv");

            exportTransactionsToCsv(inputFile, targetTxn, history, accountId);

            // 2. Invoke Python process: python anomaly_detector.py <input> <output> <target_txn>
            boolean pythonSuccess = executePythonScript(scriptFile, inputFile, outputFile, txnId);

            if (pythonSuccess && outputFile.exists()) {
                // 3. Read output CSV
                StatisticalAnomalyResult result = parsePythonOutputCsv(outputFile, txnId, accountId, targetTxn.getAmount());
                if (result != null) {
                    logger.info("Python statistical analysis completed for {}: Z={}, Score={}, Outlier={}",
                            txnId, result.getZScore(), result.getStatisticalScore(), result.getIsAnomaly());
                    return result;
                }
            }

            logger.warn("Failed to retrieve result from Python CSV. Falling back to Java statistical engine.");
            return calculateJavaFallback(account, targetTxn, history);

        } catch (Exception e) {
            logger.error("Error invoking Python anomaly detector for txn {}: {}", txnId, e.getMessage(), e);
            return calculateJavaFallback(account, targetTxn, history);
        }
    }

    /**
     * Locates the anomaly_detector.py script across possible local working directory structures.
     */
    private File resolvePythonScript() {
        List<File> candidates = List.of(
                new File("../python/anomaly_detector.py"),
                new File("python/anomaly_detector.py"),
                new File("d:/S B I PROJECT/python/anomaly_detector.py"),
                new File("../../python/anomaly_detector.py")
        );

        for (File candidate : candidates) {
            if (candidate.exists()) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Exports transactions into CSV format matching python/anomaly_detector.py specifications.
     */
    private void exportTransactionsToCsv(File file, Transaction targetTxn, List<Transaction> history, String accountId) throws IOException {
        Set<String> writtenIds = new HashSet<>();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write("transaction_id,account_id,amount,timestamp,recipient,location,type,status\n");

            // Write target transaction first
            if (targetTxn != null) {
                writeTransactionLine(writer, targetTxn, accountId);
                writtenIds.add(targetTxn.getTransactionId());
            }

            // Write historical transactions
            if (history != null) {
                for (Transaction txn : history) {
                    if (!writtenIds.contains(txn.getTransactionId())) {
                        writeTransactionLine(writer, txn, accountId);
                        writtenIds.add(txn.getTransactionId());
                    }
                }
            }
        }
    }

    private void writeTransactionLine(BufferedWriter writer, Transaction txn, String accountId) throws IOException {
        String id = txn.getTransactionId() != null ? txn.getTransactionId() : "UNKNOWN";
        double amt = txn.getAmount() != null ? txn.getAmount() : 0.0;
        String ts = txn.getTimestamp() != null ? txn.getTimestamp().replace(",", "") : "";
        String recipient = txn.getRecipient() != null ? txn.getRecipient().replace(",", " ") : "";
        String location = txn.getLocation() != null ? txn.getLocation().replace(",", " ") : "";
        String type = txn.getTransactionType() != null ? txn.getTransactionType() : "";
        String status = txn.getStatus() != null ? txn.getStatus() : "";

        writer.write(String.format("%s,%s,%.2f,%s,%s,%s,%s,%s\n",
                id, accountId, amt, ts, recipient, location, type, status));
    }

    /**
     * Spawns Python process and waits up to 6 seconds for completion.
     */
    private boolean executePythonScript(File scriptFile, File inputFile, File outputFile, String targetTxnId) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    pythonExecutable,
                    scriptFile.getAbsolutePath(),
                    inputFile.getAbsolutePath(),
                    outputFile.getAbsolutePath(),
                    targetTxnId
            );
            pb.directory(scriptFile.getParentFile());
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // Read output stream in background to avoid pipe blocking
            StringBuilder outputLog = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputLog.append(line).append("\n");
                }
            }

            boolean completed = process.waitFor(6, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                logger.warn("Python anomaly detector timed out after 6 seconds.");
                return false;
            }

            if (process.exitValue() != 0) {
                logger.warn("Python anomaly detector exited with code {}. Log: {}", process.exitValue(), outputLog);
                return false;
            }

            return true;
        } catch (Exception e) {
            logger.warn("Failed executing Python process: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Parses the result CSV written by python/anomaly_detector.py.
     */
    private StatisticalAnomalyResult parsePythonOutputCsv(File outputFile, String targetTxnId, String accountId, Double defaultAmount) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(outputFile), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) return null;

            String[] headers = headerLine.split(",");
            Map<String, Integer> colIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                colIndex.put(headers[i].trim(), i);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                // Split with CSV quote considerations
                List<String> values = parseCsvLine(line);
                if (values.isEmpty()) continue;

                String rowTxnId = getCol(values, colIndex, "transaction_id");
                if (targetTxnId.equals(rowTxnId)) {
                    Double amount = parseDouble(getCol(values, colIndex, "amount"), defaultAmount);
                    Double mean = parseDouble(getCol(values, colIndex, "historical_mean"), 0.0);
                    Double std = parseDouble(getCol(values, colIndex, "historical_std"), 0.0);
                    Double z = parseDouble(getCol(values, colIndex, "z_score"), 0.0);
                    Double absZ = parseDouble(getCol(values, colIndex, "abs_z_score"), Math.abs(z));
                    Integer score = parseInt(getCol(values, colIndex, "statistical_score"), 0);
                    Boolean isAnomaly = Boolean.parseBoolean(getCol(values, colIndex, "is_anomaly"));
                    String explanation = getCol(values, colIndex, "explanation");

                    return new StatisticalAnomalyResult(targetTxnId, accountId, amount, mean, std, z, absZ, score, isAnomaly, explanation);
                }
            }
        } catch (Exception e) {
            logger.error("Error reading Python output CSV: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Pure Java fallback implementing the identical Z-Score calculation.
     */
    public StatisticalAnomalyResult calculateJavaFallback(Account account, Transaction targetTxn, List<Transaction> history) {
        String txnId = targetTxn.getTransactionId();
        String accountId = account.getId() != null ? String.valueOf(account.getId()) : "ACC001";
        double amount = targetTxn.getAmount() != null ? targetTxn.getAmount() : 0.0;

        List<Double> historicalAmounts = new ArrayList<>();
        if (history != null) {
            for (Transaction t : history) {
                if (!txnId.equals(t.getTransactionId()) && t.getAmount() != null && t.getAmount() > 0) {
                    historicalAmounts.add(t.getAmount());
                }
            }
        }

        int n = historicalAmounts.size();
        if (n < MIN_HISTORY_REQUIRED) {
            double mean = historicalAmounts.isEmpty() ? amount : historicalAmounts.stream().mapToDouble(Double::doubleValue).average().orElse(amount);
            return new StatisticalAnomalyResult(
                    txnId, accountId, amount, mean, 0.0, 0.0, 0.0, 0, false,
                    String.format("Insufficient transaction history (n=%d, minimum %d required for baseline).", n, MIN_HISTORY_REQUIRED)
            );
        }

        double mean = historicalAmounts.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double varianceSum = 0.0;
        for (double a : historicalAmounts) {
            varianceSum += Math.pow(a - mean, 2);
        }
        double std = Math.sqrt(varianceSum / (n - 1));

        double z;
        double absZ;
        int statScore;
        boolean isAnomaly;
        String explanation;

        if (std < 1e-4) {
            double diff = Math.abs(amount - mean);
            if (diff < 1e-4) {
                z = 0.0;
                absZ = 0.0;
                statScore = 0;
                isAnomaly = false;
                explanation = String.format("Transaction amount (INR %.2f) matches uniform historical baseline (zero variance).", amount);
            } else {
                double effectiveStd = Math.max(std, mean > 0 ? mean * 0.05 : 1.0);
                z = (amount - mean) / effectiveStd;
                absZ = Math.abs(z);
                statScore = (int) Math.min(100, Math.round((absZ / 4.0) * 100));
                isAnomaly = absZ >= Z_THRESHOLD;
                explanation = String.format("Amount (INR %.2f) deviates from uniform historical baseline of INR %.2f (effective Z-score: %+.2f).", amount, mean, z);
            }
        } else {
            z = (amount - mean) / std;
            absZ = Math.abs(z);
            statScore = (int) Math.min(100, Math.max(0, Math.round((absZ / 4.0) * 100)));
            isAnomaly = absZ >= Z_THRESHOLD;
            if (isAnomaly) {
                explanation = String.format("Statistical outlier detected: Z-Score of %+.2f indicates amount (INR %.2f) is %.2f standard deviations from customer historical mean (INR %.2f, std dev: INR %.2f).",
                        z, amount, absZ, mean, std);
            } else {
                explanation = String.format("Amount (INR %.2f) is within normal statistical distribution (Z-Score: %+.2f, historical mean: INR %.2f, std dev: INR %.2f).",
                        z, amount, mean, std);
            }
        }

        return new StatisticalAnomalyResult(
                txnId, accountId, amount, Math.round(mean * 100.0) / 100.0,
                Math.round(std * 100.0) / 100.0, Math.round(z * 100.0) / 100.0,
                Math.round(absZ * 100.0) / 100.0, statScore, isAnomaly, explanation
        );
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder cur = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        result.add(cur.toString().trim());
        return result;
    }

    private String getCol(List<String> values, Map<String, Integer> colIndex, String colName) {
        Integer idx = colIndex.get(colName);
        if (idx != null && idx < values.size()) {
            return values.get(idx);
        }
        return "";
    }

    private Double parseDouble(String str, Double def) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return def;
        }
    }

    private Integer parseInt(String str, Integer def) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return def;
        }
    }
}
