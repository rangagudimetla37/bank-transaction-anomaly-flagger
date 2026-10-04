package com.securebank.controller;

import com.securebank.dto.ApiResponse;
import com.securebank.graph.GraphAnalysisResult;
import com.securebank.graph.TransactionGraph;
import com.securebank.graph.TransactionGraphService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
public class TransactionGraphController {

    private final TransactionGraphService transactionGraphService;

    public TransactionGraphController(TransactionGraphService transactionGraphService) {
        this.transactionGraphService = transactionGraphService;
    }

    private boolean isReviewer(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_REVIEWER".equals(a.getAuthority()));
    }

    /**
     * Endpoint 1: Graph-based anomaly analysis for a transaction.
     * Accessible by authenticated customer (for own transaction) or reviewer.
     */
    @GetMapping("/{transactionId}/graph-analysis")
    public ResponseEntity<?> getTransactionGraphAnalysis(Authentication authentication,
                                                         @PathVariable String transactionId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required."));
        }

        Optional<GraphAnalysisResult> resultOpt;
        if (isReviewer(authentication)) {
            resultOpt = transactionGraphService.getReviewerGraphAnalysis(transactionId);
        } else {
            String bankId = authentication.getName();
            resultOpt = transactionGraphService.getCustomerGraphAnalysis(bankId, transactionId);
        }

        if (resultOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Transaction not found or unauthorized for graph analysis."));
        }

        return ResponseEntity.ok(resultOpt.get());
    }

    /**
     * Endpoint 2: Graph structure (nodes & edges) for a specific transaction.
     */
    @GetMapping("/{transactionId}/graph")
    public ResponseEntity<?> getTransactionGraph(Authentication authentication,
                                                 @PathVariable String transactionId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required."));
        }

        Optional<TransactionGraph> graphOpt;
        if (isReviewer(authentication)) {
            graphOpt = transactionGraphService.getReviewerGraph(transactionId);
        } else {
            String bankId = authentication.getName();
            graphOpt = transactionGraphService.getCustomerGraph(bankId, transactionId);
        }

        if (graphOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Transaction not found or unauthorized for graph network access."));
        }

        return ResponseEntity.ok(graphOpt.get());
    }

    /**
     * Endpoint 3: Complete transaction network graph for the customer's account.
     */
    @GetMapping("/network")
    public ResponseEntity<?> getAccountNetwork(Authentication authentication,
                                              @RequestParam(required = false) String transactionId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required."));
        }

        if (isReviewer(authentication) && transactionId != null) {
            Optional<TransactionGraph> graphOpt = transactionGraphService.getReviewerGraph(transactionId);
            if (graphOpt.isPresent()) {
                return ResponseEntity.ok(graphOpt.get());
            }
        }

        String bankId = authentication.getName();
        Optional<TransactionGraph> graphOpt = transactionGraphService.getCustomerGraph(bankId, transactionId);

        if (graphOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Account network not found or unauthorized."));
        }

        return ResponseEntity.ok(graphOpt.get());
    }
}
