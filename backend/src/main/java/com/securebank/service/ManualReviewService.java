package com.securebank.service;

import com.securebank.dto.*;
import com.securebank.model.ReviewAuditLog;
import com.securebank.model.ReviewCase;
import com.securebank.model.Transaction;
import com.securebank.repository.ReviewAuditLogRepository;
import com.securebank.repository.ReviewCaseRepository;
import com.securebank.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.securebank.graph.GraphAnalysisResult;
import com.securebank.graph.TransactionGraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ManualReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ManualReviewService.class);
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ReviewCaseRepository reviewCaseRepository;
    private final ReviewAuditLogRepository auditLogRepository;
    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;
    private final TransactionGraphService transactionGraphService;

    public ManualReviewService(ReviewCaseRepository reviewCaseRepository,
                               ReviewAuditLogRepository auditLogRepository,
                               TransactionRepository transactionRepository,
                               FraudDetectionService fraudDetectionService,
                               TransactionGraphService transactionGraphService) {
        this.reviewCaseRepository = reviewCaseRepository;
        this.auditLogRepository = auditLogRepository;
        this.transactionRepository = transactionRepository;
        this.fraudDetectionService = fraudDetectionService;
        this.transactionGraphService = transactionGraphService;
    }

    public ReviewStatsDto getReviewStats() {
        int total = reviewCaseRepository.findAllCases().size();
        int newCases = reviewCaseRepository.countByStatus("NEW");
        int inReview = reviewCaseRepository.countByStatus("IN_REVIEW");
        int escalated = reviewCaseRepository.countByStatus("ESCALATED");
        int resolved = reviewCaseRepository.countByStatus("RESOLVED");
        int highRisk = reviewCaseRepository.countByRiskLevel("HIGH_RISK");

        return new ReviewStatsDto(total, newCases, inReview, escalated, resolved, highRisk);
    }

    public List<ReviewCaseSummaryDto> getAllCases() {
        List<ReviewCase> cases = reviewCaseRepository.findAllCases();
        List<ReviewCaseSummaryDto> dtos = new ArrayList<>();

        for (ReviewCase rc : cases) {
            Double amount = null;
            String merchant = null;
            String transactionTime = null;

            Optional<Transaction> txnOpt = transactionRepository.findByTransactionId(rc.getTransactionId());
            if (txnOpt.isPresent()) {
                Transaction txn = txnOpt.get();
                amount = txn.getAmount();
                merchant = txn.getRecipient() != null ? txn.getRecipient() : txn.getDescription();
                transactionTime = txn.getTimestamp();
            }

            dtos.add(new ReviewCaseSummaryDto(
                    rc.getCaseId(),
                    rc.getAlertId(),
                    rc.getTransactionId(),
                    rc.getAccountId(),
                    amount,
                    merchant,
                    transactionTime,
                    rc.getRiskScore(),
                    rc.getRiskLevel(),
                    rc.getStatus(),
                    rc.getAssignedReviewerId(),
                    rc.getDecision(),
                    rc.getCreatedAt(),
                    rc.getUpdatedAt()
            ));
        }

        return dtos;
    }

    public Optional<ReviewCaseDetailDto> getCaseDetail(String caseId, String reviewerId) {
        Optional<ReviewCase> caseOpt = reviewCaseRepository.findByCaseId(caseId);
        if (caseOpt.isEmpty()) {
            return Optional.empty();
        }

        ReviewCase rc = caseOpt.get();

        // If newly opened, transition to IN_REVIEW and assign reviewer
        if ("NEW".equalsIgnoreCase(rc.getStatus())) {
            rc.setStatus("IN_REVIEW");
            rc.setAssignedReviewerId(reviewerId);
            reviewCaseRepository.updateStatusAndAssignee(caseId, "IN_REVIEW", reviewerId);

            ReviewAuditLog audit = new ReviewAuditLog();
            audit.setCaseId(caseId);
            audit.setReviewerId(reviewerId);
            audit.setAction("CASE_OPENED");
            audit.setDetails("Case opened and assigned to reviewer " + reviewerId);
            audit.setCreatedAt(LocalDateTime.now().format(DISPLAY_FORMATTER));
            auditLogRepository.save(audit);
            logger.info("Case {} auto-assigned to {} and set to IN_REVIEW", caseId, reviewerId);
        }

        TransactionDto txnDto = null;
        Optional<Transaction> txnOpt = transactionRepository.findByTransactionId(rc.getTransactionId());
        if (txnOpt.isPresent()) {
            Transaction t = txnOpt.get();
            txnDto = new TransactionDto(
                    t.getTransactionId(),
                    t.getTransactionType(),
                    t.getDescription(),
                    t.getAmount(),
                    t.getTransactionDirection(),
                    t.getRecipient(),
                    t.getLocation(),
                    t.getTimestamp(),
                    t.getStatus()
            );
        }

        FinalRiskAnalysis riskAnalysis = fraudDetectionService.evaluateFinalRiskByTransactionId(rc.getTransactionId()).orElse(null);
        List<ReviewAuditLog> auditLogs = auditLogRepository.findByCaseId(caseId);

        ReviewCaseDetailDto detailDto = new ReviewCaseDetailDto(
                rc.getCaseId(),
                rc.getAlertId(),
                rc.getTransactionId(),
                rc.getAccountId(),
                rc.getRiskScore(),
                rc.getRiskLevel(),
                rc.getStatus(),
                rc.getAssignedReviewerId(),
                rc.getReviewerNotes(),
                rc.getDecision(),
                rc.getCreatedAt(),
                rc.getUpdatedAt(),
                rc.getResolvedAt(),
                txnDto,
                riskAnalysis,
                auditLogs
        );

        GraphAnalysisResult graphAnalysis = transactionGraphService.getReviewerGraphAnalysis(rc.getTransactionId()).orElse(null);
        detailDto.setGraphAnalysis(graphAnalysis);

        return Optional.of(detailDto);
    }

    public boolean updateReviewNotes(String caseId, String reviewerId, String notes) {
        Optional<ReviewCase> caseOpt = reviewCaseRepository.findByCaseId(caseId);
        if (caseOpt.isEmpty()) {
            return false;
        }

        reviewCaseRepository.updateNotes(caseId, notes);

        ReviewAuditLog audit = new ReviewAuditLog();
        audit.setCaseId(caseId);
        audit.setReviewerId(reviewerId);
        audit.setAction("NOTE_ADDED");
        audit.setDetails("Reviewer updated investigation notes: " + (notes.length() > 50 ? notes.substring(0, 47) + "..." : notes));
        audit.setCreatedAt(LocalDateTime.now().format(DISPLAY_FORMATTER));
        auditLogRepository.save(audit);

        return true;
    }

    public boolean escalateCase(String caseId, String reviewerId, String reason) {
        Optional<ReviewCase> caseOpt = reviewCaseRepository.findByCaseId(caseId);
        if (caseOpt.isEmpty()) {
            return false;
        }

        reviewCaseRepository.updateStatusAndAssignee(caseId, "ESCALATED", reviewerId);

        ReviewAuditLog audit = new ReviewAuditLog();
        audit.setCaseId(caseId);
        audit.setReviewerId(reviewerId);
        audit.setAction("CASE_ESCALATED");
        audit.setDetails("Case escalated by " + reviewerId + ": " + (reason != null && !reason.isBlank() ? reason : "Complex risk profile requires senior evaluation"));
        audit.setCreatedAt(LocalDateTime.now().format(DISPLAY_FORMATTER));
        auditLogRepository.save(audit);

        return true;
    }

    public boolean submitDecision(String caseId, String reviewerId, String decision, String notes) {
        Optional<ReviewCase> caseOpt = reviewCaseRepository.findByCaseId(caseId);
        if (caseOpt.isEmpty()) {
            return false;
        }

        String now = LocalDateTime.now().format(DISPLAY_FORMATTER);

        if (notes != null && !notes.isBlank()) {
            reviewCaseRepository.updateNotes(caseId, notes);
        }

        reviewCaseRepository.updateDecision(caseId, decision, "RESOLVED", now);

        ReviewAuditLog audit = new ReviewAuditLog();
        audit.setCaseId(caseId);
        audit.setReviewerId(reviewerId);
        audit.setAction("DECISION_SUBMITTED");
        audit.setDetails("Disposition submitted: " + decision + (notes != null && !notes.isBlank() ? " | Notes: " + notes : ""));
        audit.setCreatedAt(now);
        auditLogRepository.save(audit);

        return true;
    }
}
