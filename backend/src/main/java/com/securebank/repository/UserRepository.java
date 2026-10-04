package com.securebank.repository;

import com.securebank.model.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> rowMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setBankId(rs.getString("bank_id"));
            user.setPasswordHash(rs.getString("password_hash"));
            user.setRole(rs.getString("role"));
            user.setSecurityQuestion(rs.getString("security_question"));
            user.setSecurityAnswerHash(rs.getString("security_answer_hash"));

            String createdAtStr = rs.getString("created_at");
            if (createdAtStr != null) {
                try {
                    user.setCreatedAt(LocalDateTime.parse(createdAtStr.replace("T", " ").substring(0, Math.min(19, createdAtStr.length())), FORMATTER));
                } catch (Exception ignored) {
                    user.setCreatedAt(LocalDateTime.now());
                }
            }

            String updatedAtStr = rs.getString("updated_at");
            if (updatedAtStr != null) {
                try {
                    user.setUpdatedAt(LocalDateTime.parse(updatedAtStr.replace("T", " ").substring(0, Math.min(19, updatedAtStr.length())), FORMATTER));
                } catch (Exception ignored) {
                    user.setUpdatedAt(LocalDateTime.now());
                }
            }

            String lastLoginStr = rs.getString("last_login");
            if (lastLoginStr != null) {
                try {
                    user.setLastLogin(LocalDateTime.parse(lastLoginStr.replace("T", " ").substring(0, Math.min(19, lastLoginStr.length())), FORMATTER));
                } catch (Exception ignored) {
                    user.setLastLogin(null);
                }
            }

            return user;
        }
    };

    public Optional<User> findByBankId(String bankId) {
        String sql = "SELECT id, bank_id, password_hash, role, security_question, security_answer_hash, created_at, updated_at, last_login FROM users WHERE UPPER(bank_id) = UPPER(?)";
        try {
            User user = jdbcTemplate.queryForObject(sql, rowMapper, bankId);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void updatePassword(String bankId, String newPasswordHash) {
        String nowStr = LocalDateTime.now().format(FORMATTER);
        String sql = "UPDATE users SET password_hash = ?, updated_at = ? WHERE UPPER(bank_id) = UPPER(?)";
        jdbcTemplate.update(sql, newPasswordHash, nowStr, bankId);
    }

    public void updateLastLogin(String bankId, LocalDateTime lastLogin) {
        String nowStr = lastLogin.format(FORMATTER);
        String sql = "UPDATE users SET last_login = ? WHERE UPPER(bank_id) = UPPER(?)";
        jdbcTemplate.update(sql, nowStr, bankId);
    }

    public void insertUser(User user) {
        String nowStr = LocalDateTime.now().format(FORMATTER);
        String sql = "INSERT INTO users (bank_id, password_hash, role, security_question, security_answer_hash, created_at, updated_at, last_login) VALUES (?, ?, ?, ?, ?, ?, ?, NULL)";
        jdbcTemplate.update(sql,
                user.getBankId(),
                user.getPasswordHash(),
                user.getRole(),
                user.getSecurityQuestion(),
                user.getSecurityAnswerHash(),
                nowStr,
                nowStr);
    }

    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT count(*) FROM users", Long.class);
        return count != null ? count : 0L;
    }
}
