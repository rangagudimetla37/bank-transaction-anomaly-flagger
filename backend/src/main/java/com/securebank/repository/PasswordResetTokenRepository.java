package com.securebank.repository;

import com.securebank.model.PasswordResetToken;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Repository
public class PasswordResetTokenRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public PasswordResetTokenRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<PasswordResetToken> rowMapper = new RowMapper<PasswordResetToken>() {
        @Override
        public PasswordResetToken mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new PasswordResetToken(
                    rs.getLong("id"),
                    rs.getString("user_id"),
                    rs.getString("token_hash"),
                    rs.getString("expires_at"),
                    rs.getString("used_at"),
                    rs.getString("created_at")
            );
        }
    };

    public void insertToken(String userId, String tokenHash, LocalDateTime expiresAt) {
        String expiresAtStr = expiresAt.format(FORMATTER);
        String sql = "INSERT INTO password_reset_tokens (user_id, token_hash, expires_at) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, tokenHash, expiresAtStr);
    }

    public Optional<PasswordResetToken> findValidToken(String tokenHash) {
        String nowStr = LocalDateTime.now().format(FORMATTER);
        String sql = "SELECT id, user_id, token_hash, expires_at, used_at, created_at FROM password_reset_tokens " +
                     "WHERE token_hash = ? AND used_at IS NULL AND expires_at > ? ORDER BY id DESC LIMIT 1";
        List<PasswordResetToken> list = jdbcTemplate.query(sql, rowMapper, tokenHash, nowStr);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public void markUsed(String tokenHash) {
        String nowStr = LocalDateTime.now().format(FORMATTER);
        String sql = "UPDATE password_reset_tokens SET used_at = ? WHERE token_hash = ?";
        jdbcTemplate.update(sql, nowStr, tokenHash);
    }

    public void invalidateUserTokens(String userId) {
        String nowStr = LocalDateTime.now().format(FORMATTER);
        String sql = "UPDATE password_reset_tokens SET used_at = ? WHERE UPPER(user_id) = UPPER(?) AND used_at IS NULL";
        jdbcTemplate.update(sql, nowStr, userId);
    }
}
