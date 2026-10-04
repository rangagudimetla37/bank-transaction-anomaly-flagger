package com.securebank.graph;

import com.securebank.model.Account;
import com.securebank.model.Transaction;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionGraphServiceTest {

    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;
    private TransactionGraphService graphService;
    private Account account;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        graphService = new TransactionGraphService(accountRepository, transactionRepository);

        account = new Account();
        account.setId(1L);
        account.setAccountNumber("ACC100001");
        account.setCustomerName("Rajesh Sharma");
        account.setBankId("SB100001");
        account.setBalance(50000.0);
    }

    private Transaction createTxn(Long id, String txnId, String recipient, String location, Double amount) {
        Transaction t = new Transaction();
        t.setId(id);
        t.setTransactionId(txnId);
        t.setAccountId(1L);
        t.setRecipient(recipient);
        t.setLocation(location);
        t.setAmount(amount);
        t.setTransactionDirection("DEBIT");
        t.setTransactionType("PAYMENT");
        t.setStatus("COMPLETED");
        t.setTimestamp("2026-10-04 10:00:00");
        return t;
    }

    @Test
    void testBuildGraphDeduplicatesRecipientsAndLocations() {
        List<Transaction> txns = new ArrayList<>();
        txns.add(createTxn(1L, "TXN001", "Merchant A", "Hyderabad", 500.0));
        txns.add(createTxn(2L, "TXN002", "Merchant A", "Hyderabad", 600.0));
        txns.add(createTxn(3L, "TXN003", "Merchant B", "Vijayawada", 700.0));

        when(transactionRepository.findByAccountId(1L)).thenReturn(txns);

        TransactionGraph graph = graphService.buildGraph(account, "TXN001");

        assertNotNull(graph);
        assertNotNull(graph.getNodes());
        assertNotNull(graph.getEdges());

        // Nodes: 1 Account + 3 Transactions + 2 unique Recipients (A, B) + 2 unique Locations (Hyd, Vij) = 8
        assertEquals(8, graph.getNodes().size());

        // Edges: 3 Account->Txn + 3 Txn->Rec + 3 Txn->Loc = 9 edges
        assertEquals(9, graph.getEdges().size());

        // Stats
        assertEquals(2, graph.getStats().getUniqueRecipients());
        assertEquals(2, graph.getStats().getUniqueLocations());
        assertEquals(3, graph.getStats().getTotalTransactions());
    }

    @Test
    void testNewRecipientIndicator() {
        List<Transaction> txns = new ArrayList<>();
        txns.add(createTxn(1L, "TXN001", "Merchant A", "Hyderabad", 500.0));
        txns.add(createTxn(2L, "TXN002", "Merchant A", "Hyderabad", 600.0));
        // TXN003 introduces "New Merchant X"
        txns.add(createTxn(3L, "TXN003", "New Merchant X", "Hyderabad", 700.0));

        when(transactionRepository.findByAccountId(1L)).thenReturn(txns);

        TransactionGraph graph = graphService.buildGraph(account, "TXN003");
        GraphAnalysisResult analysis = graph.getAnalysis();

        assertNotNull(analysis);
        assertTrue(analysis.isNewRecipient());
        assertTrue(analysis.getGraphScore() >= 25);
        assertTrue(analysis.getReasons().stream().anyMatch(r -> r.contains("New recipient relationship detected")));
    }

    @Test
    void testSharedRecipientIndicator() {
        List<Transaction> txns = new ArrayList<>();
        txns.add(createTxn(1L, "TXN001", "Shared Merchant", "Hyderabad", 500.0));
        txns.add(createTxn(2L, "TXN002", "Shared Merchant", "Hyderabad", 600.0));

        when(transactionRepository.findByAccountId(1L)).thenReturn(txns);
        when(transactionRepository.isRecipientSharedWithOtherAccounts("Shared Merchant", 1L)).thenReturn(true);

        TransactionGraph graph = graphService.buildGraph(account, "TXN001");
        GraphAnalysisResult analysis = graph.getAnalysis();

        assertNotNull(analysis);
        assertTrue(analysis.isSharedRecipient());
        assertTrue(analysis.getReasons().stream().anyMatch(r -> r.contains("connected to multiple distinct accounts")));
    }

    @Test
    void testUnusualLocationIndicator() {
        List<Transaction> txns = new ArrayList<>();
        txns.add(createTxn(1L, "TXN001", "Merchant A", "Hyderabad", 500.0));
        txns.add(createTxn(2L, "TXN002", "Merchant A", "Hyderabad", 600.0));
        txns.add(createTxn(3L, "TXN003", "Merchant B", "Hyderabad", 700.0));
        txns.add(createTxn(4L, "TXN004", "Merchant C", "Mumbai", 800.0)); // Mumbai is 1 occurrence out of 4

        when(transactionRepository.findByAccountId(1L)).thenReturn(txns);

        TransactionGraph graph = graphService.buildGraph(account, "TXN004");
        GraphAnalysisResult analysis = graph.getAnalysis();

        assertNotNull(analysis);
        assertEquals(1, analysis.getLocationTransactionCount());
        assertTrue(analysis.getReasons().stream().anyMatch(r -> r.contains("Geographic anomaly")));
    }

    @Test
    void testRiskScoreCappingAndLevels() {
        List<Transaction> txns = new ArrayList<>();
        txns.add(createTxn(1L, "TXN001", "Merchant A", "Hyderabad", 500.0));
        txns.add(createTxn(2L, "TXN002", "Merchant A", "Hyderabad", 600.0));
        txns.add(createTxn(3L, "TXN003", "Merchant A", "Hyderabad", 700.0));
        txns.add(createTxn(4L, "TXN004", "Merchant A", "Hyderabad", 800.0));
        txns.add(createTxn(5L, "TXN005", "Brand New", "Unusual Remote City", 900.0));

        when(transactionRepository.findByAccountId(1L)).thenReturn(txns);
        when(transactionRepository.isRecipientSharedWithOtherAccounts("Brand New", 1L)).thenReturn(true);

        TransactionGraph graph = graphService.buildGraph(account, "TXN005");
        GraphAnalysisResult analysis = graph.getAnalysis();

        assertNotNull(analysis);
        assertTrue(analysis.getGraphScore() <= 100);
        // New recipient (+25) + Shared recipient (+20) + Unusual location (+20) = 65 -> SUSPICIOUS
        assertEquals(65, analysis.getGraphScore());
        assertEquals("SUSPICIOUS", analysis.getGraphRiskLevel());
    }
}
