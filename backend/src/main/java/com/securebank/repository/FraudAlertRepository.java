package com.securebank.repository;

import com.securebank.model.FraudAlert;
import org.springframework.dao.EmptyResultDataAccessException;
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
public class FraudAlertRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FraudAlertRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<FraudAlert> rowMapper = new RowMapper<FraudAlert>() {
        @Override
        public FraudAlert mapRow(ResultSet rs, int rowNum) throws SQLException {
            FraudAlert alert = new FraudAlert();
            alert.setId(rs.getLong("id"));
            alert.setAlertId(rs.getString("alert_id"));
            alert.setTransactionId(rs.getString("transaction_id"));
            alert.setAccountId(rs.getLong("account_id"));
            alert.setRiskScore(rs.getInt("risk_score"));
            alert.setRiskLevel(rs.getString("risk_level"));
            alert.setTitle(rs.getString("title"));
            alert.setDescription(rs.getString("description"));
            alert.setStatus(rs.getString("status"));
            alert.setCreatedAt(rs.getString("created_at"));
            alert.setUpdatedAt(rs.getString("updated_at"));
            return alert;
        }
    };

    public List<FraudAlert> findByAccountId(Long accountId) {
        String sql = "SELECT id, alert_id, transaction_id, account_id, risk_score, risk_level, title, description, status, created_at, updated_at " +
                     "FROM fraud_alerts WHERE account_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, accountId);
    }

    public Optional<FraudAlert> findByAlertIdAndAccountId(String alertId, Long accountId) {
        String sql = "SELECT id, alert_id, transaction_id, account_id, risk_score, risk_level, title, description, status, created_at, updated_at " +
                     "FROM fraud_alerts WHERE UPPER(alert_id) = UPPER(?) AND account_id = ?";
        try {
            FraudAlert alert = jdbcTemplate.queryForObject(sql, rowMapper, alertId, accountId);
            return Optional.ofNullable(alert);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<FraudAlert> findAll() {
        String sql = "SELECT id, alert_id, transaction_id, account_id, risk_score, risk_level, title, description, status, created_at, updated_at " +
                     "FROM fraud_alerts ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<FraudAlert> findByAlertId(String alertId) {
        String sql = "SELECT id, alert_id, transaction_id, account_id, risk_score, risk_level, title, description, status, created_at, updated_at " +
                     "FROM fraud_alerts WHERE UPPER(alert_id) = UPPER(?)";
        try {
            FraudAlert alert = jdbcTemplate.queryForObject(sql, rowMapper, alertId);
            return Optional.ofNullable(alert);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<FraudAlert> findByTransactionId(String transactionId) {
        String sql = "SELECT id, alert_id, transaction_id, account_id, risk_score, risk_level, title, description, status, created_at, updated_at " +
                     "FROM fraud_alerts WHERE UPPER(transaction_id) = UPPER(?)";
        try {
            FraudAlert alert = jdbcTemplate.queryForObject(sql, rowMapper, transactionId);
            return Optional.ofNullable(alert);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Inserts new alert or updates existing alert to prevent duplicate alert spamming.
     */
    public void save(FraudAlert alert) {
        Optional<FraudAlert> existing = findByTransactionId(alert.getTransactionId());
        String now = LocalDateTime.now().format(FORMATTER);

        if (existing.isPresent()) {
            // Update existing alert
            String sql = "UPDATE fraud_alerts SET risk_score = ?, risk_level = ?, title = ?, description = ?, updated_at = ? WHERE transaction_id = ?";
            jdbcTemplate.update(sql,
                    alert.getRiskScore(),
                    alert.getRiskLevel(),
                    alert.getTitle(),
                    alert.getDescription(),
                    now,
                    alert.getTransactionId()
            );
        } else {
            // Insert fresh alert
            String sql = "INSERT INTO fraud_alerts (alert_id, transaction_id, account_id, risk_score, risk_level, title, description, status, created_at, updated_at) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                    alert.getAlertId(),
                    alert.getTransactionId(),
                    alert.getAccountId(),
                    alert.getRiskScore(),
                    alert.getRiskLevel(),
                    alert.getTitle(),
                    alert.getDescription(),
                    alert.getStatus() != null ? alert.getStatus() : "NEW",
                    alert.getCreatedAt() != null ? alert.getCreatedAt() : now,
                    now
            );
        }
    }

    public int updateStatus(String alertId, Long accountId, String status) {
        String now = LocalDateTime.now().format(FORMATTER);
        String sql = "UPDATE fraud_alerts SET status = ?, updated_at = ? WHERE UPPER(alert_id) = UPPER(?) AND account_id = ?";
        return jdbcTemplate.update(sql, status, now, alertId, accountId);
    }

    public int countNewAlertsByAccountId(Long accountId) {
        String sql = "SELECT COUNT(*) FROM fraud_alerts WHERE account_id = ? AND status = 'NEW'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, accountId);
        return count != null ? count : 0;
    }

    public int countByAccountIdAndRiskLevel(Long accountId, String riskLevel) {
        String sql = "SELECT COUNT(*) FROM fraud_alerts WHERE account_id = ? AND UPPER(risk_level) = UPPER(?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, accountId, riskLevel);
        return count != null ? count : 0;
    }
}
