package com.securebank.security;

import com.securebank.model.PasswordResetToken;
import com.securebank.repository.PasswordResetTokenRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    public record SessionData(String bankId, String role, Instant expiresAt) {}
    public record ResetTokenData(String bankId, Instant expiresAt) {}

    private final Map<String, SessionData> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, ResetTokenData> activeResetTokens = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private static final long SESSION_EXPIRATION_HOURS = 2;
    private static final long RESET_EXPIRATION_MINUTES = 15;

    public TokenService(PasswordResetTokenRepository passwordResetTokenRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return HexFormat.of().formatHex(randomBytes);
    }

    public static String hashToken(String rawToken) {
        if (rawToken == null) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(rawToken.trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm unavailable", e);
        }
    }

    public String createSession(String bankId, String role) {
        String token = generateSecureToken();
        Instant expiresAt = Instant.now().plus(SESSION_EXPIRATION_HOURS, ChronoUnit.HOURS);
        activeSessions.put(token, new SessionData(bankId, role, expiresAt));
        return token;
    }

    public Optional<SessionData> validateSession(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        SessionData session = activeSessions.get(token);
        if (session == null) {
            return Optional.empty();
        }
        if (Instant.now().isAfter(session.expiresAt())) {
            activeSessions.remove(token);
            return Optional.empty();
        }
        return Optional.of(session);
    }

    public void invalidateSession(String token) {
        if (token != null) {
            activeSessions.remove(token);
        }
    }

    public void invalidateAllUserSessions(String bankId) {
        if (bankId == null) return;
        activeSessions.entrySet().removeIf(entry -> entry.getValue().bankId().equalsIgnoreCase(bankId));
    }

    /**
     * Phase 8: Secure Reset Token Generation.
     * Generates a raw token for the user, stores ONLY the SHA-256 hash in SQLite,
     * invalidates any prior tokens, and sets a 15-minute expiration.
     */
    public String createResetToken(String bankId) {
        if (bankId == null) return null;
        String normalizedId = bankId.trim().toUpperCase();

        // Invalidate prior tokens in DB and memory
        passwordResetTokenRepository.invalidateUserTokens(normalizedId);
        activeResetTokens.entrySet().removeIf(entry -> entry.getValue().bankId().equalsIgnoreCase(normalizedId));

        String rawToken = generateSecureToken();
        String tokenHash = hashToken(rawToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(RESET_EXPIRATION_MINUTES);

        passwordResetTokenRepository.insertToken(normalizedId, tokenHash, expiresAt);
        activeResetTokens.put(tokenHash, new ResetTokenData(normalizedId, Instant.now().plus(RESET_EXPIRATION_MINUTES, ChronoUnit.MINUTES)));

        return rawToken;
    }

    /**
     * Phase 8: Validates reset token by hashing input and checking against DB hash,
     * expiry time, and ensuring used_at IS NULL.
     */
    public boolean validateResetToken(String resetToken, String bankId) {
        if (resetToken == null || bankId == null) {
            return false;
        }
        String tokenHash = hashToken(resetToken);
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findValidToken(tokenHash);
        if (tokenOpt.isPresent()) {
            return tokenOpt.get().getUserId().equalsIgnoreCase(bankId);
        }

        // Secondary fallback to in-memory store
        ResetTokenData data = activeResetTokens.get(tokenHash);
        if (data != null && !Instant.now().isAfter(data.expiresAt())) {
            return data.bankId().equalsIgnoreCase(bankId);
        }

        return false;
    }

    /**
     * Phase 8: Consumes reset token (marks as used in DB and removes from active map).
     * Single-use guarantee: a used token can NEVER be reused.
     */
    public void consumeResetToken(String resetToken) {
        if (resetToken != null) {
            String tokenHash = hashToken(resetToken);
            passwordResetTokenRepository.markUsed(tokenHash);
            activeResetTokens.remove(tokenHash);
        }
    }
}
