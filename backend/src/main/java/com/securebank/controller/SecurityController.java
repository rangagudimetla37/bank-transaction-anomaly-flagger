package com.securebank.controller;

import com.securebank.dto.ApiResponse;
import com.securebank.dto.SecuritySummaryDto;
import com.securebank.model.SecurityActivity;
import com.securebank.model.User;
import com.securebank.repository.SecurityActivityRepository;
import com.securebank.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/security")
public class SecurityController {

    private final UserRepository userRepository;
    private final SecurityActivityRepository securityActivityRepository;
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public SecurityController(UserRepository userRepository, SecurityActivityRepository securityActivityRepository) {
        this.userRepository = userRepository;
        this.securityActivityRepository = securityActivityRepository;
    }

    @GetMapping("/activity")
    public ResponseEntity<?> getSecurityActivity(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required."));
        }

        String bankId = authentication.getName();
        Optional<User> userOpt = userRepository.findByBankId(bankId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found."));
        }

        User user = userOpt.get();
        List<SecurityActivity> activities = securityActivityRepository.findByBankId(bankId);

        String lastLoginStr = user.getLastLogin() != null
                ? user.getLastLogin().format(DISPLAY_FORMATTER)
                : "Active Now";

        String pwdUpdatedStr = user.getUpdatedAt() != null
                ? user.getUpdatedAt().format(DISPLAY_FORMATTER)
                : "Initial Account Setup";

        SecuritySummaryDto summary = new SecuritySummaryDto(
                "Protected",
                lastLoginStr,
                pwdUpdatedStr,
                activities
        );

        return ResponseEntity.ok(summary);
    }
}
