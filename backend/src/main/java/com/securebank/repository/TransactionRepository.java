package com.securebank.repository;

import com.securebank.model.Transaction;
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
public class TransactionRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Transaction> rowMapper = new RowMapper<Transaction>() {
        @Override
        public Transaction mapRow(ResultSet rs, int rowNum) throws SQLException {
            Transaction txn = new Transaction();
            txn.setId(rs.getLong("id"));
            txn.setTransactionId(rs.getString("transaction_id"));
            txn.setAccountId(rs.getLong("account_id"));
            txn.setTransactionType(rs.getString("transaction_type"));
            txn.setDescription(rs.getString("description"));
            txn.setAmount(rs.getDouble("amount"));
            txn.setTransactionDirection(rs.getString("transaction_direction"));
            txn.setRecipient(rs.getString("recipient"));
            txn.setLocation(rs.getString("location"));
            txn.setTimestamp(rs.getString("timestamp"));
            txn.setStatus(rs.getString("status"));

            String createdAtStr = rs.getString("created_at");
            if (createdAtStr != null) {
                try {
                    txn.setCreatedAt(LocalDateTime.parse(createdAtStr.replace("T", " ").substring(0, Math.min(19, createdAtStr.length())), FORMATTER));
                } catch (Exception ignored) {
                    txn.setCreatedAt(LocalDateTime.now());
                }
            }
            return txn;
        }
    };

    public List<Transaction> findByAccountId(Long accountId) {
        String sql = "SELECT id, transaction_id, account_id, transaction_type, description, amount, transaction_direction, recipient, location, timestamp, status, created_at FROM transactions WHERE account_id = ? ORDER BY timestamp DESC";
        return jdbcTemplate.query(sql, rowMapper, accountId);
    }

    public Optional<Transaction> findByTransactionIdAndAccountId(String transactionId, Long accountId) {
        String sql = "SELECT id, transaction_id, account_id, transaction_type, description, amount, transaction_direction, recipient, location, timestamp, status, created_at FROM transactions WHERE UPPER(transaction_id) = UPPER(?) AND account_id = ?";
        try {
            Transaction txn = jdbcTemplate.queryForObject(sql, rowMapper, transactionId, accountId);
            return Optional.ofNullable(txn);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Transaction> findByTransactionId(String transactionId) {
        String sql = "SELECT id, transaction_id, account_id, transaction_type, description, amount, transaction_direction, recipient, location, timestamp, status, created_at FROM transactions WHERE UPPER(transaction_id) = UPPER(?)";
        try {
            Transaction txn = jdbcTemplate.queryForObject(sql, rowMapper, transactionId);
            return Optional.ofNullable(txn);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Double getMoneyIn(Long accountId) {
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE account_id = ? AND transaction_direction = 'CREDIT' AND status = 'COMPLETED'";
        Double val = jdbcTemplate.queryForObject(sql, Double.class, accountId);
        return val != null ? val : 0.0;
    }

    public Double getMoneyOut(Long accountId) {
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE account_id = ? AND transaction_direction = 'DEBIT' AND status = 'COMPLETED'";
        Double val = jdbcTemplate.queryForObject(sql, Double.class, accountId);
        return val != null ? val : 0.0;
    }

    public int countByAccountId(Long accountId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE account_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, accountId);
        return count != null ? count : 0;
    }

    public void insertTransaction(Transaction txn) {
        String nowStr = LocalDateTime.now().format(FORMATTER);
        String sql = "INSERT INTO transactions (transaction_id, account_id, transaction_type, description, amount, transaction_direction, recipient, location, timestamp, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                txn.getTransactionId(),
                txn.getAccountId(),
                txn.getTransactionType(),
                txn.getDescription(),
                txn.getAmount(),
                txn.getTransactionDirection(),
                txn.getRecipient(),
                txn.getLocation(),
                txn.getTimestamp(),
                txn.getStatus(),
                nowStr);
    }

    public boolean isRecipientSharedWithOtherAccounts(String recipient, Long currentAccountId) {
        if (recipient == null || recipient.isBlank()) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM transactions WHERE UPPER(recipient) = UPPER(?) AND account_id != ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, recipient.trim(), currentAccountId);
        return count != null && count > 0;
    }
}
