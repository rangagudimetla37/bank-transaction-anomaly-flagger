package com.securebank.repository;

import com.securebank.model.ReviewAuditLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
public class ReviewAuditLogRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReviewAuditLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ReviewAuditLog> rowMapper = new RowMapper<ReviewAuditLog>() {
        @Override
        public ReviewAuditLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            ReviewAuditLog log = new ReviewAuditLog();
            log.setId(rs.getLong("id"));
            log.setCaseId(rs.getString("case_id"));
            log.setReviewerId(rs.getString("reviewer_id"));
            log.setAction(rs.getString("action"));
            log.setDetails(rs.getString("details"));
            log.setCreatedAt(rs.getString("created_at"));
            return log;
        }
    };

    public void save(ReviewAuditLog log) {
        String now = LocalDateTime.now().format(FORMATTER);
        String sql = "INSERT INTO review_audit_logs (case_id, reviewer_id, action, details, created_at) " +
                     "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                log.getCaseId(),
                log.getReviewerId(),
                log.getAction(),
                log.getDetails(),
                log.getCreatedAt() != null ? log.getCreatedAt() : now
        );
    }

    public List<ReviewAuditLog> findByCaseId(String caseId) {
        String sql = "SELECT id, case_id, reviewer_id, action, details, created_at " +
                     "FROM review_audit_logs WHERE UPPER(case_id) = UPPER(?) ORDER BY created_at ASC";
        return jdbcTemplate.query(sql, rowMapper, caseId);
    }

    public List<ReviewAuditLog> findAll() {
        String sql = "SELECT id, case_id, reviewer_id, action, details, created_at " +
                     "FROM review_audit_logs ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }
}
