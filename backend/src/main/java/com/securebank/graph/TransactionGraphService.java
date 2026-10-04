package com.securebank.graph;

import com.securebank.model.Account;
import com.securebank.model.Transaction;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TransactionGraphService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionGraphService.class);
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionGraphService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Builds complete transaction relationship graph for an account.
     */
    public TransactionGraph buildGraph(Account account, String focusTransactionId) {
        List<Transaction> transactions = transactionRepository.findByAccountId(account.getId());
        // Sort chronologically ascending for analytical reasoning
        transactions.sort(Comparator.comparing(Transaction::getId));

        List<GraphNode> nodes = new ArrayList<>();
        List<GraphEdge> edges = new ArrayList<>();

        // 1. Account Node (central root)
        String accountNodeId = "ACC:" + account.getAccountNumber();
        GraphNode accountNode = new GraphNode(accountNodeId, "Account " + account.getAccountNumber(), GraphNodeType.ACCOUNT);
        accountNode.addProperty("accountNumber", account.getAccountNumber());
        accountNode.addProperty("customerName", account.getCustomerName());
        accountNode.addProperty("bankId", account.getBankId());
        accountNode.addProperty("balance", account.getBalance());
        nodes.add(accountNode);

        // Deduplication structures
        Map<String, GraphNode> recipientNodes = new HashMap<>();
        Map<String, Integer> recipientTxCount = new HashMap<>();

        Map<String, GraphNode> locationNodes = new HashMap<>();
        Map<String, Integer> locationTxCount = new HashMap<>();

        for (Transaction txn : transactions) {
            String txnId = txn.getTransactionId();
            String txnNodeId = "TXN:" + txnId;

            // 2. Transaction Node
            GraphNode txnNode = new GraphNode(txnNodeId, txnId, GraphNodeType.TRANSACTION);
            txnNode.addProperty("transactionId", txnId);
            txnNode.addProperty("amount", txn.getAmount());
            txnNode.addProperty("direction", txn.getTransactionDirection());
            txnNode.addProperty("timestamp", txn.getTimestamp());
            txnNode.addProperty("category", txn.getTransactionType());
            txnNode.addProperty("status", txn.getStatus());
            txnNode.addProperty("recipient", txn.getRecipient());
            txnNode.addProperty("location", txn.getLocation());
            if (focusTransactionId != null && txnId.equalsIgnoreCase(focusTransactionId)) {
                txnNode.addProperty("isFocused", true);
            }
            nodes.add(txnNode);

            // Edge: Account -> Transaction
            edges.add(new GraphEdge(
                    accountNodeId,
                    txnNodeId,
                    "ACCOUNT_HAS_TRANSACTION",
                    txn.getAmount() != null ? "₹" + String.format("%,.0f", txn.getAmount()) : "Txn"
            ));

            // 3. Recipient Node (Deduplicated)
            String recipient = txn.getRecipient() != null ? txn.getRecipient().trim() : "Unknown Recipient";
            String recipientNodeId = "REC:" + recipient;
            recipientTxCount.put(recipient, recipientTxCount.getOrDefault(recipient, 0) + 1);

            if (!recipientNodes.containsKey(recipientNodeId)) {
                GraphNode recNode = new GraphNode(recipientNodeId, recipient, GraphNodeType.RECIPIENT);
                recNode.addProperty("recipientName", recipient);
                boolean isShared = transactionRepository.isRecipientSharedWithOtherAccounts(recipient, account.getId());
                recNode.addProperty("isShared", isShared);
                recipientNodes.put(recipientNodeId, recNode);
            }

            // Edge: Transaction -> Recipient
            edges.add(new GraphEdge(
                    txnNodeId,
                    recipientNodeId,
                    "TRANSACTION_TO_RECIPIENT",
                    "Paid to"
            ));

            // 4. Location Node (Deduplicated)
            String location = txn.getLocation() != null ? txn.getLocation().trim() : "Unspecified";
            String locationNodeId = "LOC:" + location;
            locationTxCount.put(location, locationTxCount.getOrDefault(location, 0) + 1);

            if (!locationNodes.containsKey(locationNodeId)) {
                GraphNode locNode = new GraphNode(locationNodeId, location, GraphNodeType.LOCATION);
                locNode.addProperty("locationName", location);
                locationNodes.put(locationNodeId, locNode);
            }

            // Edge: Transaction -> Location
            edges.add(new GraphEdge(
                    txnNodeId,
                    locationNodeId,
                    "TRANSACTION_AT_LOCATION",
                    "Location"
            ));
        }

        // Attach counts to recipient and location nodes
        for (Map.Entry<String, GraphNode> entry : recipientNodes.entrySet()) {
            String recName = (String) entry.getValue().getProperties().get("recipientName");
            entry.getValue().addProperty("transactionCount", recipientTxCount.getOrDefault(recName, 1));
            nodes.add(entry.getValue());
        }

        for (Map.Entry<String, GraphNode> entry : locationNodes.entrySet()) {
            String locName = (String) entry.getValue().getProperties().get("locationName");
            entry.getValue().addProperty("transactionCount", locationTxCount.getOrDefault(locName, 1));
            nodes.add(entry.getValue());
        }

        // Network Statistics
        String mostFreqLoc = locationTxCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("N/A");

        String mostFreqRec = recipientTxCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("N/A");

        double avgTxPerRec = recipientNodes.isEmpty() ? 0.0 :
                (double) transactions.size() / recipientNodes.size();

        GraphNetworkStats stats = new GraphNetworkStats(
                transactions.size(),
                recipientNodes.size(),
                locationNodes.size(),
                edges.size(),
                mostFreqLoc,
                mostFreqRec,
                avgTxPerRec
        );

        // Analysis if focus transaction requested
        GraphAnalysisResult analysis = null;
        if (focusTransactionId != null) {
            analysis = analyzeTransaction(account, focusTransactionId, transactions, recipientTxCount, locationTxCount);
        } else if (!transactions.isEmpty()) {
            // Default to analyzing the most recent transaction
            Transaction latest = transactions.get(transactions.size() - 1);
            analysis = analyzeTransaction(account, latest.getTransactionId(), transactions, recipientTxCount, locationTxCount);
        }

        return new TransactionGraph(nodes, edges, analysis, stats);
    }

    /**
     * Conducts explainable graph anomaly analysis for a single transaction.
     */
    public GraphAnalysisResult analyzeTransaction(Account account, String transactionId,
                                                  List<Transaction> transactions,
                                                  Map<String, Integer> recipientTxCount,
                                                  Map<String, Integer> locationTxCount) {
        Optional<Transaction> targetOpt = transactions.stream()
                .filter(t -> t.getTransactionId().equalsIgnoreCase(transactionId))
                .findFirst();

        if (targetOpt.isEmpty()) {
            return new GraphAnalysisResult(transactionId, 0, "NORMAL", false, false,
                    0, 0, recipientTxCount.size(), locationTxCount.size(),
                    transactions.size(), List.of("Transaction not found in account history."),
                    LocalDateTime.now().format(DISPLAY_FORMATTER));
        }

        Transaction target = targetOpt.get();
        String recipient = target.getRecipient() != null ? target.getRecipient().trim() : "";
        String location = target.getLocation() != null ? target.getLocation().trim() : "";

        // Check if recipient appeared before this transaction in chronological sequence
        int targetIdx = transactions.indexOf(target);
        boolean appearsBefore = false;
        for (int i = 0; i < targetIdx; i++) {
            Transaction prev = transactions.get(i);
            if (prev.getRecipient() != null && prev.getRecipient().trim().equalsIgnoreCase(recipient)) {
                appearsBefore = true;
                break;
            }
        }

        boolean isNewRecipient = !appearsBefore;
        boolean isSharedRecipient = transactionRepository.isRecipientSharedWithOtherAccounts(recipient, account.getId());

        int recCount = recipientTxCount.getOrDefault(recipient, 1);
        int locCount = locationTxCount.getOrDefault(location, 1);
        int totalTx = transactions.size();

        int score = 0;
        List<String> reasons = new ArrayList<>();

        // 1. New Recipient Relationship Indicator (+25)
        if (isNewRecipient) {
            score += 25;
            reasons.add("New recipient relationship detected: \"" + recipient + "\" has not appeared in prior transaction history.");
        }

        // 2. Shared Recipient Across Multiple Demo Accounts (+20)
        if (isSharedRecipient) {
            score += 20;
            reasons.add("Network anomaly: Recipient \"" + recipient + "\" is connected to multiple distinct accounts in the banking system.");
        }

        // 3. Unusual Location Indicator (+20)
        // If location has <= 1 occurrence and account has multiple transactions
        boolean unusualLocation = (locCount <= 1 && totalTx >= 3);
        if (unusualLocation) {
            score += 20;
            reasons.add("Geographic anomaly: Location \"" + location + "\" represents an infrequent node for this account (" + locCount + " transaction).");
        }

        // 4. Recipient Concentration Indicator (+20)
        // If a single recipient accounts for > 35% of account transactions
        double recPercentage = totalTx > 0 ? ((double) recCount / totalTx) * 100.0 : 0.0;
        if (recPercentage >= 35.0 && recCount >= 4) {
            score += 20;
            reasons.add(String.format("High counterparty concentration: \"%s\" accounts for %.1f%% of all account activity.", recipient, recPercentage));
        }

        // 5. Multiple Connected Transactions Indicator (+15)
        if (recCount >= 3) {
            score += 15;
            reasons.add("Multiple connected transactions (" + recCount + " transactions) converge on counterparty node \"" + recipient + "\".");
        }

        int finalScore = Math.min(100, score);
        String riskLevel = determineGraphRiskLevel(finalScore);

        if (reasons.isEmpty()) {
            reasons.add("Transaction conforms to established relationship network and standard location baseline.");
        }

        return new GraphAnalysisResult(
                transactionId,
                finalScore,
                riskLevel,
                isNewRecipient,
                isSharedRecipient,
                unusualLocation,
                recCount,
                locCount,
                recipientTxCount.size(),
                locationTxCount.size(),
                totalTx,
                reasons,
                LocalDateTime.now().format(DISPLAY_FORMATTER)
        );
    }

    private String determineGraphRiskLevel(int score) {
        if (score >= 80) return "HIGH_RISK";
        if (score >= 60) return "SUSPICIOUS";
        if (score >= 30) return "WATCH";
        return "NORMAL";
    }

    /**
     * Customer access: Verifies ownership of account and returns graph.
     */
    public Optional<TransactionGraph> getCustomerGraph(String bankId, String focusTransactionId) {
        Optional<Account> accountOpt = accountRepository.findByBankId(bankId);
        if (accountOpt.isEmpty()) {
            return Optional.empty();
        }

        Account account = accountOpt.get();

        if (focusTransactionId != null) {
            Optional<Transaction> txnOpt = transactionRepository.findByTransactionIdAndAccountId(focusTransactionId, account.getId());
            if (txnOpt.isEmpty()) {
                logger.warn("Customer {} attempted unauthorized graph access for txn {}", bankId, focusTransactionId);
                return Optional.empty();
            }
        }

        return Optional.of(buildGraph(account, focusTransactionId));
    }

    /**
     * Customer access: Returns graph analysis for specific transaction.
     */
    public Optional<GraphAnalysisResult> getCustomerGraphAnalysis(String bankId, String transactionId) {
        Optional<Account> accountOpt = accountRepository.findByBankId(bankId);
        if (accountOpt.isEmpty()) {
            return Optional.empty();
        }

        Account account = accountOpt.get();
        Optional<Transaction> txnOpt = transactionRepository.findByTransactionIdAndAccountId(transactionId, account.getId());
        if (txnOpt.isEmpty()) {
            return Optional.empty();
        }

        TransactionGraph graph = buildGraph(account, transactionId);
        return Optional.ofNullable(graph.getAnalysis());
    }

    /**
     * Reviewer access: Authorizes investigation of any transaction across accounts.
     */
    public Optional<TransactionGraph> getReviewerGraph(String transactionId) {
        Optional<Transaction> txnOpt = transactionRepository.findByTransactionId(transactionId);
        if (txnOpt.isEmpty()) {
            return Optional.empty();
        }

        Transaction txn = txnOpt.get();
        Optional<Account> accountOpt = accountRepository.findById(txn.getAccountId());
        if (accountOpt.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(buildGraph(accountOpt.get(), transactionId));
    }

    /**
     * Reviewer access: Returns graph analysis for review case.
     */
    public Optional<GraphAnalysisResult> getReviewerGraphAnalysis(String transactionId) {
        Optional<TransactionGraph> graphOpt = getReviewerGraph(transactionId);
        return graphOpt.map(TransactionGraph::getAnalysis);
    }
}
