package com.securebank.repository;

import com.securebank.model.ReviewCase;
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
public class ReviewCaseRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReviewCaseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ReviewCase> rowMapper = new RowMapper<ReviewCase>() {
        @Override
        public ReviewCase mapRow(ResultSet rs, int rowNum) throws SQLException {
            ReviewCase rc = new ReviewCase();
            rc.setId(rs.getLong("id"));
            rc.setCaseId(rs.getString("case_id"));
            rc.setAlertId(rs.getString("alert_id"));
            rc.setTransactionId(rs.getString("transaction_id"));
            rc.setAccountId(rs.getLong("account_id"));
            rc.setRiskScore(rs.getInt("risk_score"));
            rc.setRiskLevel(rs.getString("risk_level"));
            rc.setStatus(rs.getString("status"));
            rc.setAssignedReviewerId(rs.getString("assigned_reviewer_id"));
            rc.setReviewerNotes(rs.getString("reviewer_notes"));
            rc.setDecision(rs.getString("decision"));
            rc.setCreatedAt(rs.getString("created_at"));
            rc.setUpdatedAt(rs.getString("updated_at"));
            rc.setResolvedAt(rs.getString("resolved_at"));
            return rc;
        }
    };

    public List<ReviewCase> findAllCases() {
        String sql = "SELECT id, case_id, alert_id, transaction_id, account_id, risk_score, risk_level, status, " +
                     "assigned_reviewer_id, reviewer_notes, decision, created_at, updated_at, resolved_at " +
                     "FROM review_cases ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<ReviewCase> findByCaseId(String caseId) {
        String sql = "SELECT id, case_id, alert_id, transaction_id, account_id, risk_score, risk_level, status, " +
                     "assigned_reviewer_id, reviewer_notes, decision, created_at, updated_at, resolved_at " +
                     "FROM review_cases WHERE UPPER(case_id) = UPPER(?)";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, caseId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<ReviewCase> findByTransactionId(String transactionId) {
        String sql = "SELECT id, case_id, alert_id, transaction_id, account_id, risk_score, risk_level, status, " +
                     "assigned_reviewer_id, reviewer_notes, decision, created_at, updated_at, resolved_at " +
                     "FROM review_cases WHERE UPPER(transaction_id) = UPPER(?)";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, transactionId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<ReviewCase> findByAlertId(String alertId) {
        String sql = "SELECT id, case_id, alert_id, transaction_id, account_id, risk_score, risk_level, status, " +
                     "assigned_reviewer_id, reviewer_notes, decision, created_at, updated_at, resolved_at " +
                     "FROM review_cases WHERE UPPER(alert_id) = UPPER(?)";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, alertId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void save(ReviewCase rc) {
        Optional<ReviewCase> existing = findByTransactionId(rc.getTransactionId());
        String now = LocalDateTime.now().format(FORMATTER);

        if (existing.isPresent()) {
            String sql = "UPDATE review_cases SET risk_score = ?, risk_level = ?, status = ?, " +
                         "assigned_reviewer_id = ?, reviewer_notes = ?, decision = ?, updated_at = ?, resolved_at = ? " +
                         "WHERE transaction_id = ?";
            jdbcTemplate.update(sql,
                    rc.getRiskScore(),
                    rc.getRiskLevel(),
                    rc.getStatus(),
                    rc.getAssignedReviewerId(),
                    rc.getReviewerNotes(),
                    rc.getDecision(),
                    now,
                    rc.getResolvedAt(),
                    rc.getTransactionId()
            );
        } else {
            String sql = "INSERT INTO review_cases (case_id, alert_id, transaction_id, account_id, risk_score, " +
                         "risk_level, status, assigned_reviewer_id, reviewer_notes, decision, created_at, updated_at, resolved_at) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                    rc.getCaseId(),
                    rc.getAlertId(),
                    rc.getTransactionId(),
                    rc.getAccountId(),
                    rc.getRiskScore(),
                    rc.getRiskLevel(),
                    rc.getStatus() != null ? rc.getStatus() : "NEW",
                    rc.getAssignedReviewerId(),
                    rc.getReviewerNotes(),
                    rc.getDecision(),
                    rc.getCreatedAt() != null ? rc.getCreatedAt() : now,
                    now,
                    rc.getResolvedAt()
            );
        }
    }

    public int updateStatusAndAssignee(String caseId, String status, String reviewerId) {
        String now = LocalDateTime.now().format(FORMATTER);
        String sql = "UPDATE review_cases SET status = ?, assigned_reviewer_id = ?, updated_at = ? WHERE UPPER(case_id) = UPPER(?)";
        return jdbcTemplate.update(sql, status, reviewerId, now, caseId);
    }

    public int updateNotes(String caseId, String notes) {
        String now = LocalDateTime.now().format(FORMATTER);
        String sql = "UPDATE review_cases SET reviewer_notes = ?, updated_at = ? WHERE UPPER(case_id) = UPPER(?)";
        return jdbcTemplate.update(sql, notes, now, caseId);
    }

    public int updateDecision(String caseId, String decision, String status, String resolvedAt) {
        String now = LocalDateTime.now().format(FORMATTER);
        String sql = "UPDATE review_cases SET decision = ?, status = ?, resolved_at = ?, updated_at = ? WHERE UPPER(case_id) = UPPER(?)";
        return jdbcTemplate.update(sql, decision, status, resolvedAt != null ? resolvedAt : now, now, caseId);
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM review_cases WHERE UPPER(status) = UPPER(?)";
        Integer c = jdbcTemplate.queryForObject(sql, Integer.class, status);
        return c != null ? c : 0;
    }

    public int countByRiskLevel(String riskLevel) {
        String sql = "SELECT COUNT(*) FROM review_cases WHERE UPPER(risk_level) = UPPER(?)";
        Integer c = jdbcTemplate.queryForObject(sql, Integer.class, riskLevel);
        return c != null ? c : 0;
    }
}
