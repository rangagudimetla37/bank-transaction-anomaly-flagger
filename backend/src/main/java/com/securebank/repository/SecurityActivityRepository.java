package com.securebank.repository;

import com.securebank.model.SecurityActivity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
public class SecurityActivityRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SecurityActivityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SecurityActivity> rowMapper = new RowMapper<SecurityActivity>() {
        @Override
        public SecurityActivity mapRow(ResultSet rs, int rowNum) throws SQLException {
            SecurityActivity activity = new SecurityActivity();
            activity.setId(rs.getLong("id"));
            activity.setBankId(rs.getString("bank_id"));
            activity.setActivityType(rs.getString("activity_type"));
            activity.setDescription(rs.getString("description"));
            activity.setIpAddress(rs.getString("ip_address"));
            activity.setTimestamp(rs.getString("timestamp"));
            return activity;
        }
    };

    public List<SecurityActivity> findByBankId(String bankId) {
        String sql = "SELECT id, bank_id, activity_type, description, ip_address, timestamp FROM security_activities WHERE UPPER(bank_id) = UPPER(?) ORDER BY id DESC LIMIT 10";
        return jdbcTemplate.query(sql, rowMapper, bankId);
    }

    public void insertActivity(String bankId, String activityType, String description, String ipAddress) {
        insertActivity(bankId, activityType, description, ipAddress, "SecureBank Web Client");
    }

    public void insertActivity(String bankId, String activityType, String description, String ipAddress, String userAgent) {
        String nowStr = LocalDateTime.now().format(FORMATTER);
        // Insert into legacy security_activities table
        try {
            String sql1 = "INSERT INTO security_activities (bank_id, activity_type, description, ip_address, timestamp) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql1, bankId, activityType, description, ipAddress, nowStr);
        } catch (Exception ignored) {}

        // Insert into Phase 8 security_audit_logs table
        try {
            String sql2 = "INSERT INTO security_audit_logs (user_id, event_type, description, ip_address, user_agent, created_at) VALUES (?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql2, bankId, activityType, description, ipAddress, userAgent, nowStr);
        } catch (Exception ignored) {}
    }
}
