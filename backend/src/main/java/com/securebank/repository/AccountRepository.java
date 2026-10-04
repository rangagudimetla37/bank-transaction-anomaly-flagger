package com.securebank.repository;

import com.securebank.model.Account;
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
public class AccountRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Account> rowMapper = new RowMapper<Account>() {
        @Override
        public Account mapRow(ResultSet rs, int rowNum) throws SQLException {
            Account account = new Account();
            account.setId(rs.getLong("id"));
            account.setAccountNumber(rs.getString("account_number"));
            account.setBankId(rs.getString("bank_id"));
            account.setCustomerName(rs.getString("customer_name"));
            account.setAccountType(rs.getString("account_type"));
            account.setBalance(rs.getDouble("balance"));

            String createdAtStr = rs.getString("created_at");
            if (createdAtStr != null) {
                try {
                    account.setCreatedAt(LocalDateTime.parse(createdAtStr.replace("T", " ").substring(0, Math.min(19, createdAtStr.length())), FORMATTER));
                } catch (Exception ignored) {
                    account.setCreatedAt(LocalDateTime.now());
                }
            }
            return account;
        }
    };

    public Optional<Account> findByBankId(String bankId) {
        String sql = "SELECT id, account_number, bank_id, customer_name, account_type, balance, created_at FROM accounts WHERE UPPER(bank_id) = UPPER(?)";
        try {
            Account account = jdbcTemplate.queryForObject(sql, rowMapper, bankId);
            return Optional.ofNullable(account);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Account> findById(Long id) {
        String sql = "SELECT id, account_number, bank_id, customer_name, account_type, balance, created_at FROM accounts WHERE id = ?";
        try {
            Account account = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.ofNullable(account);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void insertAccount(Account account) {
        String nowStr = LocalDateTime.now().format(FORMATTER);
        String sql = "INSERT INTO accounts (account_number, bank_id, customer_name, account_type, balance, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                account.getAccountNumber(),
                account.getBankId(),
                account.getCustomerName(),
                account.getAccountType(),
                account.getBalance(),
                nowStr);
    }
}
