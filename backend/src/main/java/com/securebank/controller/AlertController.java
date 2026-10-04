package com.securebank.controller;

import com.securebank.dto.ApiResponse;
import com.securebank.model.FraudAlert;
import com.securebank.service.FraudDetectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final FraudDetectionService fraudDetectionService;

    public AlertController(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    /**
     * Retrieves all fraud & anomaly alerts for the authenticated customer.
     */
    @GetMapping
    public ResponseEntity<?> getCustomerAlerts(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required."));
        }

        String bankId = authentication.getName();
        List<FraudAlert> alerts = fraudDetectionService.getCustomerAlerts(bankId);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Retrieves alert details by alert ID with strict customer ownership checks.
     */
    @GetMapping("/{alertId}")
    public ResponseEntity<?> getAlertDetail(Authentication authentication, @PathVariable String alertId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required."));
        }

        String bankId = authentication.getName();
        Optional<FraudAlert> alertOpt = fraudDetectionService.getAlertDetail(bankId, alertId);

        if (alertOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Alert not found or unauthorized."));
        }

        return ResponseEntity.ok(alertOpt.get());
    }

    /**
     * Updates an alert status (e.g. VIEWED, REVIEW_REQUIRED).
     */
    @RequestMapping(value = "/{alertId}/status", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<?> updateAlertStatus(Authentication authentication,
                                               @PathVariable String alertId,
                                               @RequestBody Map<String, String> payload) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required."));
        }

        String bankId = authentication.getName();
        String newStatus = payload != null ? payload.get("status") : null;

        if (newStatus == null || newStatus.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Status is required."));
        }

        boolean updated = fraudDetectionService.updateAlertStatus(bankId, alertId, newStatus);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Unable to update alert status. Verify alert ID and authorization."));
        }

        return ResponseEntity.ok(new ApiResponse(true, "Alert status updated successfully to " + newStatus.toUpperCase()));
    }

    /**
     * Retrieves summary counts for header notification badge and dashboard cards.
     */
    @GetMapping("/summary")
    public ResponseEntity<?> getAlertSummary(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required."));
        }

        String bankId = authentication.getName();
        Map<String, Object> summary = fraudDetectionService.getAlertSummary(bankId);
        return ResponseEntity.ok(summary);
    }
}
