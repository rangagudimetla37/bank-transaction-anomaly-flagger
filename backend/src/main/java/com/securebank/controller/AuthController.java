package com.securebank.controller;

import com.securebank.dto.*;
import com.securebank.model.User;
import com.securebank.repository.SecurityActivityRepository;
import com.securebank.repository.UserRepository;
import com.securebank.security.LoginAttemptService;
import com.securebank.security.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final SecurityActivityRepository securityActivityRepository;
    private final LoginAttemptService loginAttemptService;

    private static final Pattern HAS_UPPER = Pattern.compile("[A-Z]");
    private static final Pattern HAS_LOWER = Pattern.compile("[a-z]");
    private static final Pattern HAS_DIGIT = Pattern.compile("[0-9]");
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          TokenService tokenService,
                          SecurityActivityRepository securityActivityRepository,
                          LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.securityActivityRepository = securityActivityRepository;
        this.loginAttemptService = loginAttemptService;
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return "127.0.0.1";
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String bankId = request.getBankId().trim();
        String password = request.getPassword();
        String ip = getClientIp(httpRequest);

        // Phase 8: Brute Force Protection - check lockout
        if (loginAttemptService.isBlocked(bankId)) {
            securityActivityRepository.insertActivity(bankId, "LOGIN_BLOCKED", "Temporary lockout: 5 consecutive failed attempts", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ApiResponse(false, "Too many failed attempts. Please try again later."));
        }

        Optional<User> userOpt = userRepository.findByBankId(bankId);

        boolean passwordMatches = false;
        if (userOpt.isPresent()) {
            String storedHash = userOpt.get().getPasswordHash();
            passwordMatches = passwordEncoder.matches(password, storedHash);
            if (!passwordMatches && password != null && !password.equals(password.trim())) {
                passwordMatches = passwordEncoder.matches(password.trim(), storedHash);
            }
        }

        // Constant-time generic check to prevent user enumeration
        if (userOpt.isEmpty() || !passwordMatches) {
            loginAttemptService.loginFailed(bankId);
            securityActivityRepository.insertActivity(bankId, "LOGIN_FAILURE", "Failed login attempt (invalid credentials)", ip);

            if (loginAttemptService.isBlocked(bankId)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(new ApiResponse(false, "Too many failed attempts. Please try again later."));
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Invalid Bank ID or password."));
        }

        // Login Succeeded: Reset failed attempts counter
        loginAttemptService.loginSucceeded(bankId);

        User user = userOpt.get();
        LocalDateTime now = LocalDateTime.now();
        userRepository.updateLastLogin(user.getBankId(), now);

        String token = tokenService.createSession(user.getBankId(), user.getRole());
        String lastLoginStr = user.getLastLogin() != null
                ? user.getLastLogin().format(DISPLAY_FORMATTER)
                : now.format(DISPLAY_FORMATTER);

        securityActivityRepository.insertActivity(user.getBankId(), "LOGIN_SUCCESS", "Authenticated via Web Portal", ip);

        UserProfileDto profile = new UserProfileDto(user.getBankId(), user.getRole(), lastLoginStr);
        return ResponseEntity.ok(new AuthResponse(true, "Login successful.", token, profile));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                              HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            Optional<TokenService.SessionData> sessionOpt = tokenService.validateSession(token);
            sessionOpt.ifPresent(session ->
                securityActivityRepository.insertActivity(session.bankId(), "LOGOUT", "Session terminated by user", ip)
            );
            tokenService.invalidateSession(token);
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new ApiResponse(true, "Logged out successfully."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Not authenticated."));
        }

        String bankId = authentication.getName();
        Optional<User> userOpt = userRepository.findByBankId(bankId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "User not found."));
        }

        User user = userOpt.get();
        String lastLoginStr = user.getLastLogin() != null
                ? user.getLastLogin().format(DISPLAY_FORMATTER)
                : "Initial Login";

        UserProfileDto profile = new UserProfileDto(user.getBankId(), user.getRole(), lastLoginStr);
        return ResponseEntity.ok(new AuthResponse(true, "User profile retrieved.", null, profile));
    }

    @PostMapping("/forgot-password/verify")
    public ResponseEntity<?> verifyForgotPassword(@Valid @RequestBody VerifyResetRequest request,
                                                  HttpServletRequest httpRequest) {
        String bankId = request.getBankId().trim();
        String pin = request.getSecurityPin().trim();
        String ip = getClientIp(httpRequest);

        Optional<User> userOpt = userRepository.findByBankId(bankId);

        if (userOpt.isEmpty() || !passwordEncoder.matches(pin, userOpt.get().getSecurityAnswerHash())) {
            securityActivityRepository.insertActivity(bankId, "VERIFY_FAILED", "Failed identity verification attempt", ip);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Identity verification failed. Please check your Bank ID and verification code."));
        }

        securityActivityRepository.insertActivity(bankId, "PASSWORD_RESET_REQUESTED", "Password reset token generated for local demonstration", ip);
        // Phase 8: TokenService generates raw token and stores SHA-256 hash in password_reset_tokens table
        String resetToken = tokenService.createResetToken(userOpt.get().getBankId());
        return ResponseEntity.ok(new VerifyResetResponse(true, "Password reset token generated for local demonstration.", resetToken));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request,
                                                     HttpServletRequest httpRequest) {
        String bankId = request.getBankId().trim();
        String resetToken = request.getResetToken().trim();
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();
        String ip = getClientIp(httpRequest);

        // Phase 8: Validates raw token by SHA-256 hashing and checking password_reset_tokens table
        if (!tokenService.validateResetToken(resetToken, bankId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Invalid or expired verification session. Please verify identity again."));
        }

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "New password and confirm password do not match."));
        }

        // College-demo password policy enforcement
        if (newPassword.length() < 8) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Password must be at least 8 characters long."));
        }
        if (!HAS_UPPER.matcher(newPassword).find()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Password must contain at least one uppercase letter (A-Z)."));
        }
        if (!HAS_LOWER.matcher(newPassword).find()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Password must contain at least one lowercase letter (a-z)."));
        }
        if (!HAS_DIGIT.matcher(newPassword).find()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Password must contain at least one number (0-9)."));
        }

        // Secure password update with BCrypt
        String newPasswordHash = passwordEncoder.encode(newPassword);
        userRepository.updatePassword(bankId, newPasswordHash);

        // One-time use: consume reset token (marked used_at) and revoke active sessions
        tokenService.consumeResetToken(resetToken);
        tokenService.invalidateAllUserSessions(bankId);

        securityActivityRepository.insertActivity(bankId, "PASSWORD_RESET_SUCCESS", "Password reset successfully completed", ip);

        return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully. Please log in with your new password."));
    }

    /**
     * Phase 8: Authenticated Password Change Endpoint.
     * Accessible by authenticated CUSTOMER or REVIEWER from /security.
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                            Authentication authentication,
                                            HttpServletRequest httpRequest) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required to change password."));
        }

        String bankId = authentication.getName();
        String currentPassword = request.getCurrentPassword();
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();
        String ip = getClientIp(httpRequest);

        Optional<User> userOpt = userRepository.findByBankId(bankId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User account not found."));
        }

        User user = userOpt.get();

        // 1. Current password must be correct
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Current password is incorrect."));
        }

        // 2. New password and confirmation must match
        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "New password and confirmation do not match."));
        }

        // 3. New password must satisfy password policy
        if (newPassword.length() < 8) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "New password must be at least 8 characters long."));
        }
        if (!HAS_UPPER.matcher(newPassword).find()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "New password must contain at least one uppercase letter (A-Z)."));
        }
        if (!HAS_LOWER.matcher(newPassword).find()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "New password must contain at least one lowercase letter (a-z)."));
        }
        if (!HAS_DIGIT.matcher(newPassword).find()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "New password must contain at least one number (0-9)."));
        }

        // 4. New password should be different from current password
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "New password must be different from current password."));
        }

        // 5. Update password with BCrypt(12)
        String newPasswordHash = passwordEncoder.encode(newPassword);
        userRepository.updatePassword(bankId, newPasswordHash);

        // 6. Invalidate previous sessions and establish a fresh session token
        tokenService.invalidateAllUserSessions(bankId);
        String freshToken = tokenService.createSession(bankId, user.getRole());

        // 7. Record security audit log
        securityActivityRepository.insertActivity(bankId, "PASSWORD_CHANGED", "Password changed successfully via Security Center", ip);

        String lastLoginStr = user.getLastLogin() != null
                ? user.getLastLogin().format(DISPLAY_FORMATTER)
                : LocalDateTime.now().format(DISPLAY_FORMATTER);

        UserProfileDto profile = new UserProfileDto(user.getBankId(), user.getRole(), lastLoginStr);
        return ResponseEntity.ok(new AuthResponse(true, "Password changed successfully.", freshToken, profile));
    }
}
