package com.securebank.controller;

import com.securebank.dto.ApiResponse;
import com.securebank.dto.FinalRiskAnalysis;
import com.securebank.dto.TransactionAnalysisResult;
import com.securebank.service.FraudDetectionService;
import com.securebank.service.TransactionAnalysisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
public class TransactionAnalysisController {

    private final TransactionAnalysisService transactionAnalysisService;
    private final FraudDetectionService fraudDetectionService;

    public TransactionAnalysisController(TransactionAnalysisService transactionAnalysisService,
                                         FraudDetectionService fraudDetectionService) {
        this.transactionAnalysisService = transactionAnalysisService;
        this.fraudDetectionService = fraudDetectionService;
    }

    /**
     * Phase 3 / 4 Analysis endpoint (preserved for backward compatibility).
     */
    @GetMapping("/{transactionId}/analysis")
    public ResponseEntity<?> getTransactionAnalysis(Authentication authentication, @PathVariable String transactionId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required."));
        }

        String bankId = authentication.getName();
        Optional<TransactionAnalysisResult> resultOpt = transactionAnalysisService.analyzeTransaction(bankId, transactionId);

        if (resultOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Transaction not found."));
        }

        return ResponseEntity.ok(resultOpt.get());
    }

    /**
     * Phase 5 Final Composite Risk Analysis endpoint.
     * Combines Rule Score (60%) + Python Statistical Score (40%) into Final Risk Score.
     */
    @GetMapping("/{transactionId}/risk-analysis")
    public ResponseEntity<?> getFinalRiskAnalysis(Authentication authentication, @PathVariable String transactionId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required."));
        }

        String bankId = authentication.getName();
        Optional<FinalRiskAnalysis> analysisOpt = fraudDetectionService.evaluateFinalRisk(bankId, transactionId);

        if (analysisOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Transaction not found."));
        }

        return ResponseEntity.ok(analysisOpt.get());
    }

    @GetMapping("/recent-risk")
    public ResponseEntity<?> getRecentRiskActivity(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required."));
        }

        String bankId = authentication.getName();
        List<TransactionAnalysisResult> results = transactionAnalysisService.analyzeRecentTransactions(bankId, 5);
        return ResponseEntity.ok(results);
    }
}
