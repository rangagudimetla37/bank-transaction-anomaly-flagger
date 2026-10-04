package com.securebank.controller;

import com.securebank.dto.*;
import com.securebank.service.ManualReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final ManualReviewService manualReviewService;

    public ReviewController(ManualReviewService manualReviewService) {
        this.manualReviewService = manualReviewService;
    }

    /**
     * Dashboard summary metrics for reviewer portal.
     */
    @GetMapping("/cases/summary")
    public ResponseEntity<?> getReviewStats(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Reviewer authentication required."));
        }
        ReviewStatsDto stats = manualReviewService.getReviewStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * List all review cases for investigation.
     */
    @GetMapping("/cases")
    public ResponseEntity<?> getAllCases(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Reviewer authentication required."));
        }
        List<ReviewCaseSummaryDto> cases = manualReviewService.getAllCases();
        return ResponseEntity.ok(cases);
    }

    /**
     * Investigation workbench for a specific review case.
     * Auto-assigns to reviewer and transitions status from NEW to IN_REVIEW.
     */
    @GetMapping("/cases/{caseId}")
    public ResponseEntity<?> getCaseDetail(Authentication authentication, @PathVariable String caseId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Reviewer authentication required."));
        }

        String reviewerId = authentication.getName();
        Optional<ReviewCaseDetailDto> detailOpt = manualReviewService.getCaseDetail(caseId, reviewerId);

        if (detailOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Review case not found: " + caseId));
        }

        return ResponseEntity.ok(detailOpt.get());
    }

    /**
     * Updates notes on an active review case.
     */
    @PostMapping("/cases/{caseId}/notes")
    public ResponseEntity<?> updateNotes(Authentication authentication,
                                         @PathVariable String caseId,
                                         @RequestBody ReviewNoteRequest request) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Reviewer authentication required."));
        }

        if (request == null || request.getNotes() == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Notes content is required."));
        }

        String reviewerId = authentication.getName();
        boolean updated = manualReviewService.updateReviewNotes(caseId, reviewerId, request.getNotes());

        if (!updated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Review case not found: " + caseId));
        }

        return ResponseEntity.ok(new ApiResponse(true, "Notes updated successfully and audit log recorded."));
    }

    /**
     * Resolves the case with an analytical disposition.
     */
    @PostMapping("/cases/{caseId}/decision")
    public ResponseEntity<?> submitDecision(Authentication authentication,
                                            @PathVariable String caseId,
                                            @RequestBody ReviewDecisionRequest request) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Reviewer authentication required."));
        }

        if (request == null || request.getDecision() == null || request.getDecision().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Decision disposition is required."));
        }

        String decision = request.getDecision().toUpperCase();
        if (!decision.equals("LEGITIMATE") &&
            !decision.equals("SUSPICIOUS_CONFIRMED") &&
            !decision.equals("REQUIRES_FURTHER_REVIEW")) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Invalid decision type. Valid options: LEGITIMATE, SUSPICIOUS_CONFIRMED, REQUIRES_FURTHER_REVIEW."));
        }

        String reviewerId = authentication.getName();
        boolean resolved = manualReviewService.submitDecision(caseId, reviewerId, decision, request.getNotes());

        if (!resolved) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Review case not found: " + caseId));
        }

        return ResponseEntity.ok(new ApiResponse(true, "Case resolved successfully with decision: " + decision));
    }

    /**
     * Escalates a case for senior evaluation.
     */
    @PostMapping("/cases/{caseId}/escalate")
    public ResponseEntity<?> escalateCase(Authentication authentication,
                                          @PathVariable String caseId,
                                          @RequestBody(required = false) Map<String, String> body) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Reviewer authentication required."));
        }

        String reviewerId = authentication.getName();
        String reason = (body != null && body.containsKey("reason")) ? body.get("reason") : "Manual escalation requested by reviewer";

        boolean escalated = manualReviewService.escalateCase(caseId, reviewerId, reason);

        if (!escalated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Review case not found: " + caseId));
        }

        return ResponseEntity.ok(new ApiResponse(true, "Case escalated successfully and logged to audit trail."));
    }
}
