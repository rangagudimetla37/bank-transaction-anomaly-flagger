package com.securebank.service;

import com.securebank.dto.ReviewCaseDetailDto;
import com.securebank.dto.ReviewStatsDto;
import com.securebank.model.ReviewCase;
import com.securebank.model.Transaction;
import com.securebank.repository.ReviewAuditLogRepository;
import com.securebank.repository.ReviewCaseRepository;
import com.securebank.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ManualReviewServiceTest {

    private ReviewCaseRepository reviewCaseRepository;
    private ReviewAuditLogRepository auditLogRepository;
    private TransactionRepository transactionRepository;
    private FraudDetectionService fraudDetectionService;
    private com.securebank.graph.TransactionGraphService transactionGraphService;
    private ManualReviewService manualReviewService;

    @BeforeEach
    void setUp() {
        reviewCaseRepository = mock(ReviewCaseRepository.class);
        auditLogRepository = mock(ReviewAuditLogRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        fraudDetectionService = mock(FraudDetectionService.class);
        transactionGraphService = mock(com.securebank.graph.TransactionGraphService.class);
        manualReviewService = new ManualReviewService(
                reviewCaseRepository,
                auditLogRepository,
                transactionRepository,
                fraudDetectionService,
                transactionGraphService
        );
    }

    @Test
    void testGetReviewStats() {
        when(reviewCaseRepository.findAllCases()).thenReturn(List.of(new ReviewCase(), new ReviewCase(), new ReviewCase()));
        when(reviewCaseRepository.countByStatus("NEW")).thenReturn(1);
        when(reviewCaseRepository.countByStatus("IN_REVIEW")).thenReturn(1);
        when(reviewCaseRepository.countByStatus("ESCALATED")).thenReturn(0);
        when(reviewCaseRepository.countByStatus("RESOLVED")).thenReturn(1);
        when(reviewCaseRepository.countByRiskLevel("HIGH_RISK")).thenReturn(1);

        ReviewStatsDto stats = manualReviewService.getReviewStats();

        assertEquals(3, stats.getTotalCases());
        assertEquals(1, stats.getNewCases());
        assertEquals(1, stats.getInReviewCases());
        assertEquals(0, stats.getEscalatedCases());
        assertEquals(1, stats.getResolvedCases());
        assertEquals(1, stats.getHighRiskCases());
    }

    @Test
    void testOpenNewCaseTransitionsToInReview() {
        ReviewCase rc = new ReviewCase();
        rc.setCaseId("CASE-001");
        rc.setTransactionId("TXN-001");
        rc.setStatus("NEW");

        when(reviewCaseRepository.findByCaseId("CASE-001")).thenReturn(Optional.of(rc));
        when(transactionRepository.findByTransactionId("TXN-001")).thenReturn(Optional.of(new Transaction()));
        when(auditLogRepository.findByCaseId("CASE-001")).thenReturn(List.of());

        Optional<ReviewCaseDetailDto> detail = manualReviewService.getCaseDetail("CASE-001", "REV100001");

        assertTrue(detail.isPresent());
        assertEquals("IN_REVIEW", detail.get().getStatus());
        assertEquals("REV100001", detail.get().getAssignedReviewerId());
        verify(reviewCaseRepository).updateStatusAndAssignee("CASE-001", "IN_REVIEW", "REV100001");
        verify(auditLogRepository).save(any());
    }

    @Test
    void testUpdateReviewNotes() {
        ReviewCase rc = new ReviewCase();
        rc.setCaseId("CASE-002");
        when(reviewCaseRepository.findByCaseId("CASE-002")).thenReturn(Optional.of(rc));

        boolean ok = manualReviewService.updateReviewNotes("CASE-002", "REV100001", "Customer contacted via phone; confirmed travel.");

        assertTrue(ok);
        verify(reviewCaseRepository).updateNotes("CASE-002", "Customer contacted via phone; confirmed travel.");
        verify(auditLogRepository).save(any());
    }

    @Test
    void testSubmitDecisionResolvesCase() {
        ReviewCase rc = new ReviewCase();
        rc.setCaseId("CASE-003");
        when(reviewCaseRepository.findByCaseId("CASE-003")).thenReturn(Optional.of(rc));

        boolean ok = manualReviewService.submitDecision("CASE-003", "REV100001", "LEGITIMATE", "Cardholder confirmed transaction in Mumbai branch.");

        assertTrue(ok);
        verify(reviewCaseRepository).updateNotes("CASE-003", "Cardholder confirmed transaction in Mumbai branch.");
        verify(reviewCaseRepository).updateDecision(eq("CASE-003"), eq("LEGITIMATE"), eq("RESOLVED"), any());
        verify(auditLogRepository).save(any());
    }

    @Test
    void testEscalateCase() {
        ReviewCase rc = new ReviewCase();
        rc.setCaseId("CASE-004");
        when(reviewCaseRepository.findByCaseId("CASE-004")).thenReturn(Optional.of(rc));

        boolean ok = manualReviewService.escalateCase("CASE-004", "REV100001", "Senior approval needed for ₹95,000 transaction");

        assertTrue(ok);
        verify(reviewCaseRepository).updateStatusAndAssignee("CASE-004", "ESCALATED", "REV100001");
        verify(auditLogRepository).save(any());
    }
}
