package com.securebank.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    public static final int MAX_ATTEMPTS = 5;
    public static final int LOCKOUT_MINUTES = 5;

    public static class AttemptTracker {
        private int attempts;
        private Instant lastAttempt;
        private Instant blockedUntil;

        public AttemptTracker(int attempts, Instant lastAttempt) {
            this.attempts = attempts;
            this.lastAttempt = lastAttempt;
        }

        public int getAttempts() {
            return attempts;
        }

        public void increment() {
            this.attempts++;
            this.lastAttempt = Instant.now();
            if (this.attempts >= MAX_ATTEMPTS) {
                this.blockedUntil = Instant.now().plus(LOCKOUT_MINUTES, ChronoUnit.MINUTES);
            }
        }

        public boolean isBlocked() {
            if (blockedUntil == null) {
                return false;
            }
            if (Instant.now().isAfter(blockedUntil)) {
                // Lockout has elapsed, reset attempts
                this.attempts = 0;
                this.blockedUntil = null;
                return false;
            }
            return true;
        }

        public long getRemainingLockoutSeconds() {
            if (blockedUntil == null) return 0;
            long remaining = ChronoUnit.SECONDS.between(Instant.now(), blockedUntil);
            return Math.max(0, remaining);
        }
    }

    private final Map<String, AttemptTracker> attemptsMap = new ConcurrentHashMap<>();

    private String normalize(String bankId) {
        return bankId == null ? "" : bankId.trim().toUpperCase();
    }

    public boolean isBlocked(String bankId) {
        String key = normalize(bankId);
        AttemptTracker tracker = attemptsMap.get(key);
        if (tracker == null) {
            return false;
        }
        return tracker.isBlocked();
    }

    public void loginFailed(String bankId) {
        String key = normalize(bankId);
        attemptsMap.compute(key, (k, tracker) -> {
            if (tracker == null) {
                tracker = new AttemptTracker(0, Instant.now());
            }
            tracker.increment();
            return tracker;
        });
    }

    public void loginSucceeded(String bankId) {
        String key = normalize(bankId);
        attemptsMap.remove(key);
    }

    public int getFailedAttempts(String bankId) {
        String key = normalize(bankId);
        AttemptTracker tracker = attemptsMap.get(key);
        return tracker != null ? tracker.getAttempts() : 0;
    }

    public void resetAttempts(String bankId) {
        attemptsMap.remove(normalize(bankId));
    }
}
