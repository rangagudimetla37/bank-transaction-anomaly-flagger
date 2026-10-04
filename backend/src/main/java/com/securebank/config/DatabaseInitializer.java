package com.securebank.config;

import com.securebank.model.Account;
import com.securebank.model.Transaction;
import com.securebank.model.User;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.SecurityActivityRepository;
import com.securebank.repository.TransactionRepository;
import com.securebank.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;

@Component
public class DatabaseInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final SecurityActivityRepository securityActivityRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate, UserRepository userRepository,
                               AccountRepository accountRepository, TransactionRepository transactionRepository,
                               SecurityActivityRepository securityActivityRepository, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.securityActivityRepository = securityActivityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        logger.info("Initializing SecureBank SQLite Database schema (Phase 1 & 2)...");

        // Ensure database directory exists
        try {
            File dbDir = Paths.get("database").toFile();
            if (!dbDir.exists()) dbDir.mkdirs();
            File parentDbDir = Paths.get("../database").toFile();
            if (!parentDbDir.exists()) parentDbDir.mkdirs();
        } catch (Exception e) {
            logger.warn("Could not check/create database directory: {}", e.getMessage());
        }

        // 1. Users table (Phase 1)
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                bank_id TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                role TEXT NOT NULL DEFAULT 'CUSTOMER',
                security_question TEXT NOT NULL,
                security_answer_hash TEXT NOT NULL,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                last_login TEXT
            );
            """);

        // 2. Accounts table (Phase 2)
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS accounts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                account_number TEXT UNIQUE NOT NULL,
                bank_id TEXT NOT NULL,
                customer_name TEXT NOT NULL,
                account_type TEXT NOT NULL DEFAULT 'SAVINGS',
                balance REAL NOT NULL DEFAULT 0.0,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (bank_id) REFERENCES users(bank_id)
            );
            """);

        // 3. Transactions table (Phase 2)
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                transaction_id TEXT UNIQUE NOT NULL,
                account_id INTEGER NOT NULL,
                transaction_type TEXT NOT NULL,
                description TEXT NOT NULL,
                amount REAL NOT NULL,
                transaction_direction TEXT NOT NULL,
                recipient TEXT NOT NULL,
                location TEXT NOT NULL,
                timestamp TEXT NOT NULL,
                status TEXT NOT NULL,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (account_id) REFERENCES accounts(id)
            );
            """);

        // 4. Security activities table (Phase 2)
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS security_activities (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                bank_id TEXT NOT NULL,
                activity_type TEXT NOT NULL,
                description TEXT NOT NULL,
                ip_address TEXT NOT NULL,
                timestamp TEXT DEFAULT CURRENT_TIMESTAMP
            );
            """);

        // 5. Fraud alerts table (Phase 5)
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS fraud_alerts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                alert_id TEXT UNIQUE NOT NULL,
                transaction_id TEXT NOT NULL,
                account_id INTEGER NOT NULL,
                risk_score INTEGER NOT NULL,
                risk_level TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'NEW',
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id),
                FOREIGN KEY (account_id) REFERENCES accounts(id)
            );
            """);

        // 6. Review Cases table (Phase 6)
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS review_cases (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                case_id TEXT UNIQUE NOT NULL,
                alert_id TEXT NOT NULL,
                transaction_id TEXT UNIQUE NOT NULL,
                account_id INTEGER NOT NULL,
                risk_score INTEGER NOT NULL,
                risk_level TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'NEW',
                assigned_reviewer_id TEXT,
                reviewer_notes TEXT,
                decision TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                resolved_at TEXT,
                FOREIGN KEY (alert_id) REFERENCES fraud_alerts(alert_id),
                FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id),
                FOREIGN KEY (account_id) REFERENCES accounts(id)
            );
            """);

        // 7. Review Audit Logs table (Phase 6)
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS review_audit_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                case_id TEXT NOT NULL,
                reviewer_id TEXT NOT NULL,
                action TEXT NOT NULL,
                details TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            );
            """);

        // 8. Password Reset Tokens table (Phase 8 - Storing token hash, never raw token)
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS password_reset_tokens (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id TEXT NOT NULL,
                token_hash TEXT NOT NULL,
                expires_at TEXT NOT NULL,
                used_at TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(bank_id)
            );
            """);

        // 9. Security Audit Logs table (Phase 8 - General Security Audit Logs)
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS security_audit_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id TEXT NOT NULL,
                event_type TEXT NOT NULL,
                description TEXT NOT NULL,
                ip_address TEXT,
                user_agent TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            );
            """);

        // Seed demo user SB100001 (Customer)
        String demoBankId = "SB100001";
        if (userRepository.findByBankId(demoBankId).isEmpty()) {
            logger.info("Seeding synthetic demo account for Bank ID: {}", demoBankId);
            String demoPasswordHash = passwordEncoder.encode("Demo@12345");
            String securityPinHash = passwordEncoder.encode("778899");

            User demoUser = new User();
            demoUser.setBankId(demoBankId);
            demoUser.setPasswordHash(demoPasswordHash);
            demoUser.setRole("CUSTOMER");
            demoUser.setSecurityQuestion("Demo Security PIN");
            demoUser.setSecurityAnswerHash(securityPinHash);

            userRepository.insertUser(demoUser);
        }

        // Seed demo reviewer REV100001 (Reviewer - Phase 6)
        String reviewerBankId = "REV100001";
        if (userRepository.findByBankId(reviewerBankId).isEmpty()) {
            logger.info("Seeding synthetic reviewer account for Bank ID: {}", reviewerBankId);
            String reviewerPasswordHash = passwordEncoder.encode("Review@12345");
            String reviewerPinHash = passwordEncoder.encode("778899");

            User reviewerUser = new User();
            reviewerUser.setBankId(reviewerBankId);
            reviewerUser.setPasswordHash(reviewerPasswordHash);
            reviewerUser.setRole("REVIEWER");
            reviewerUser.setSecurityQuestion("Demo Reviewer PIN");
            reviewerUser.setSecurityAnswerHash(reviewerPinHash);

            userRepository.insertUser(reviewerUser);
        }

        // Seed demo account ACC100001
        Optional<Account> existingAccount = accountRepository.findByBankId(demoBankId);
        Account account;
        if (existingAccount.isEmpty()) {
            logger.info("Seeding demo account for Bank ID: {}", demoBankId);
            account = new Account();
            account.setAccountNumber("ACC100001");
            account.setBankId(demoBankId);
            account.setCustomerName("Rajesh Sharma");
            account.setAccountType("Savings Account");
            account.setBalance(84250.00);
            accountRepository.insertAccount(account);

            account = accountRepository.findByBankId(demoBankId).orElseThrow();
        } else {
            account = existingAccount.get();
        }

        // Seed secondary demo customer SB100002 for Phase 7 shared recipient network analysis
        String demoCustomer2 = "SB100002";
        if (userRepository.findByBankId(demoCustomer2).isEmpty()) {
            logger.info("Seeding secondary customer account for Bank ID: {}", demoCustomer2);
            String passHash2 = passwordEncoder.encode("Demo@12345");
            String pinHash2 = passwordEncoder.encode("778899");

            User user2 = new User();
            user2.setBankId(demoCustomer2);
            user2.setPasswordHash(passHash2);
            user2.setRole("CUSTOMER");
            user2.setSecurityQuestion("Demo Security PIN");
            user2.setSecurityAnswerHash(pinHash2);
            userRepository.insertUser(user2);
        }

        Optional<Account> existingAccount2 = accountRepository.findByBankId(demoCustomer2);
        if (existingAccount2.isEmpty()) {
            Account account2 = new Account();
            account2.setAccountNumber("ACC100002");
            account2.setBankId(demoCustomer2);
            account2.setCustomerName("Priya Patel");
            account2.setAccountType("Savings Account");
            account2.setBalance(42800.00);
            accountRepository.insertAccount(account2);
            account2 = accountRepository.findByBankId(demoCustomer2).orElseThrow();

            // Seed transactions for account 2 with shared recipients
            seedTransaction(account2.getId(), "TXN-20261003-0091", "GROCERY", "Weekly Groceries", 1200.00, "DEBIT", "More Megastore", "Hyderabad", "03 Oct 2026, 04:30 PM", "COMPLETED");
            seedTransaction(account2.getId(), "TXN-20261002-0092", "SHOPPING", "E-Commerce Purchase", 3500.00, "DEBIT", "Flipkart Internet Pvt Ltd", "Bengaluru", "02 Oct 2026, 02:15 PM", "COMPLETED");
            seedTransaction(account2.getId(), "TXN-20261001-0093", "RECHARGE", "Mobile Recharge", 499.00, "DEBIT", "Jio Prepaid Recharge", "Hyderabad", "01 Oct 2026, 11:00 AM", "COMPLETED");
            logger.info("Seeded secondary demo account ACC100002 with shared recipient transactions.");
        }

        // Seed transactions if none exist for this account
        if (transactionRepository.countByAccountId(account.getId()) == 0) {
            logger.info("Seeding 25 realistic transactions for account ACC100001...");

            seedTransaction(account.getId(), "TXN-20261004-0025", "GROCERY", "Grocery Supermarket", 1450.00, "DEBIT", "More Megastore", "Hyderabad", "04 Oct 2026, 06:30 PM", "COMPLETED");
            seedTransaction(account.getId(), "TXN-20261004-0024", "FOOD", "Artisanal Coffee & Snacks", 380.00, "DEBIT", "Roastery Coffee House", "Hyderabad", "04 Oct 2026, 02:15 PM", "COMPLETED");
            seedTransaction(account.getId(), "TXN-20261003-0023", "RECHARGE", "Mobile Prepaid Recharge", 699.00, "DEBIT", "Jio Prepaid Recharge", "Hyderabad", "03 Oct 2026, 09:05 PM", "COMPLETED");
            seedTransaction(account.getId(), "TXN-20261003-0022", "BILL", "Electricity Bill Payment", 2850.00, "DEBIT", "TSSPDCL Hyderabad", "Hyderabad", "03 Oct 2026, 11:45 AM", "COMPLETED");
            seedTransaction(account.getId(), "TXN-20261001-0021", "SALARY", "Monthly Salary Credit", 65000.00, "CREDIT", "Tech Corp India Ltd", "Bengaluru", "01 Oct 2026, 09:00 AM", "COMPLETED");
            seedTransaction(account.getId(), "TXN-20260930-0020", "FOOD", "Family Dinner Restaurant", 2120.00, "DEBIT", "Paradise Biryani", "Hyderabad", "30 Sep 2026, 08:10 PM", "COMPLETED");
            seedTransaction(account.getId(), "TXN-20260929-0019", "ATM", "ATM Cash Withdrawal", 5000.00, "DEBIT", "SecureBank ATM Banjara Hills", "Hyderabad", "29 Sep 2026, 04:30 PM", "COMPLETED");
            seedTransaction(account.getId(), "TXN-20260928-0018", "UPI", "UPI Payment at Gourmet Store", 450.00, "DEBIT", "Nature's Basket Bandra", "Mumbai", "28 Sep 2026, 01:20 PM", "COMPLETED");
            seedTransaction(account.getId(), "TXN-20260927-0017", "CREDIT", "Quarterly Dividend Credit", 3250.00, "CREDIT", "HDFC Mutual Fund AMC", "Mumbai", "27 Sep 2026, 10:05 AM", "COMPLETED");
            seedTransaction(account.getId(), "TXN-20260925-0016", "SHOPPING", "Electronics Online Purchase", 8999.00, "DEBIT", "Flipkart Internet Pvt Ltd", "Bengaluru", "25 Sep 2026, 07:40 PM", "COMPLETED");
            seedTransaction(account.getId(), "TXN-20260924-0015", "BILL", "Broadband Internet Bill", 1179.00, "DEBIT", "ACT Fibernet Broadband", "Vijayawada", "24 Sep 2026, 03:10 PM", "COMPLETED");
            seedTransaction(account.getId(), "TXN-20260922-0014", "BILL", "LPG Gas Cylinder Booking", 950.00, "DEBIT", "Bharat Gas Distributors", "Visakhapatnam", "22 Sep 2026, 08:30 AM", "COMPLETED");
            seedTransaction(account.getId(), "TXN-20260920-0013", "UPI", "Peer-to-Peer UPI Received", 4250.00, "CREDIT", "Suresh Kumar (Friend)", "Chennai", "20 Sep 2026, 05:55 PM", "COMPLETED");
            seedTransaction(account.getId(), "TXN-20260918-0012", "FUEL", "Fuel Station Petrol Pump", 2500.00, "DEBIT", "Indian Oil Jubilee Hills", "Hyderabad", "18 Sep 2026, 12:40 PM", "COMPLETED");
            seedTransaction(account.getId(), "TXN-20260916-0011", "SUBSCRIPTION", "Streaming Service Subscription", 499.00, "DEBIT", "Netflix India Service", "Mumbai", "16 Sep 2026, 10:15 PM", "COMPLETED");
            seedTransaction(account.getId(), "TXN-20260915-0010", "PHARMACY", "Prescription Medicines", 840.00, "DEBIT", "Apollo Pharmacy Gajuwaka", "Visakhapatnam", "15 Sep 2026, 02:00 PM", "COMPLETED");
            seedTransaction(account.getId(), "TXN-20260912-0009", "TRAVEL", "Domestic Flight Tickets", 6400.00, "DEBIT", "IndiGo Airlines Chennai", "Chennai", "12 Sep 2026, 11:20 AM", "COMPLETED");
            seedTransaction(account.getId(), "TXN-20260910-0008", "UPI", "Laundry Service UPI Payment", 1200.00, "DEBIT", "City Dry Cleaners", "Hyderabad", "10 Sep 2026, 04:45 PM", "COMPLETED");
            seedTransaction(account.getId(), "TXN-20260908-0007", "DEPOSIT", "Cash Deposit at Branch Counter", 15000.00, "CREDIT", "SecureBank Vijayawada Main Branch", "Vijayawada", "08 Sep 2026, 09:30 AM", "COMPLETED");
            seedTransaction(account.getId(), "TXN-20260905-0006", "FOOD", "Food Delivery Order", 720.00, "DEBIT", "Swiggy Online Delivery", "Bengaluru", "05 Sep 2026, 09:10 PM", "COMPLETED");
            seedTransaction(account.getId(), "TXN-20260903-0005", "SHOPPING", "Apparel Department Store", 3450.00, "DEBIT", "Westside Retail Hyderabad", "Hyderabad", "03 Sep 2026, 06:00 PM", "COMPLETED");
            seedTransaction(account.getId(), "TXN-20260901-0004", "SALARY", "Monthly Salary Credit", 65000.00, "CREDIT", "Tech Corp India Ltd", "Bengaluru", "01 Sep 2026, 09:00 AM", "COMPLETED");
            seedTransaction(account.getId(), "TXN-20260829-0003", "UPI", "Furniture Deposit Transfer", 1800.00, "DEBIT", "Urban Ladder Design", "Bengaluru", "29 Aug 2026, 02:22 PM", "PENDING");
            seedTransaction(account.getId(), "TXN-20260826-0002", "ONLINE", "International Digital Purchase", 4200.00, "DEBIT", "Cloud Software Global", "Mumbai", "26 Aug 2026, 07:15 PM", "FAILED");
            seedTransaction(account.getId(), "TXN-20260824-0001", "BILL", "Direct-to-Home TV Recharge", 350.00, "DEBIT", "Tata Play DTH Service", "Hyderabad", "24 Aug 2026, 10:10 AM", "COMPLETED");

            logger.info("Successfully seeded initial transactions.");
        }

        // Ensure Phase 3 specific demonstration transactions exist
        if (transactionRepository.findByTransactionIdAndAccountId("TXN-20261004-0026", account.getId()).isEmpty()) {
            logger.info("Seeding Phase 3 multi-rule demonstration transactions...");
            // Showcase 1: Multi-Rule High Risk Anomaly (High Amount + Late Night + Location Change + New Recipient)
            seedTransaction(account.getId(), "TXN-20261004-0026", "ONLINE", "High-Value Jewelry & Electronics", 48500.00, "DEBIT", "Global Luxury Imports", "Mumbai", "04 Oct 2026, 02:41 AM", "COMPLETED");

            // Showcase 2 & 3: Rapid Transaction Burst (within 2 minutes of each other)
            seedTransaction(account.getId(), "TXN-20261004-0027", "UPI", "Rapid Express Pay - Transfer 1", 2000.00, "DEBIT", "Speed Merchant", "Hyderabad", "04 Oct 2026, 03:10 PM", "COMPLETED");
            seedTransaction(account.getId(), "TXN-20261004-0028", "UPI", "Rapid Express Pay - Transfer 2", 2500.00, "DEBIT", "Speed Merchant", "Hyderabad", "04 Oct 2026, 03:12 PM", "COMPLETED");

            // Showcase 4: Late Night Unusual Time
            seedTransaction(account.getId(), "TXN-20261003-0030", "ATM", "Late Night ATM Withdrawal", 3000.00, "DEBIT", "ATM Cash Dispenser", "Hyderabad", "03 Oct 2026, 03:25 AM", "COMPLETED");

            // Showcase 5: Outstation Location Change
            seedTransaction(account.getId(), "TXN-20261002-0031", "FOOD", "Coastal Seafood Dining", 1850.00, "DEBIT", "Sea Breeze Bistro", "Visakhapatnam", "02 Oct 2026, 01:15 PM", "COMPLETED");
            logger.info("Phase 3 demonstration transactions seeded successfully.");
        }

        // Ensure Phase 5 demonstration transaction (Amount ₹95,000, 02:40 AM) exists
        if (transactionRepository.findByTransactionIdAndAccountId("TXN-20261004-0035", account.getId()).isEmpty()) {
            logger.info("Seeding Phase 5 demonstration transaction TXN-20261004-0035...");
            seedTransaction(account.getId(), "TXN-20261004-0035", "ONLINE", "High-Value Bullion Investment", 95000.00, "DEBIT", "Apex Luxury Bullion", "Mumbai", "04 Oct 2026, 02:40 AM", "COMPLETED");
        }

        // Seed demonstration fraud alerts if none exist
        Integer alertCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM fraud_alerts", Integer.class);
        if (alertCount == null || alertCount == 0) {
            logger.info("Seeding demonstration fraud alerts...");
            jdbcTemplate.update("""
                INSERT INTO fraud_alerts (alert_id, transaction_id, account_id, risk_score, risk_level, title, description, status, created_at, updated_at)
                VALUES ('ALT-20261004-0035', 'TXN-20261004-0035', ?, 82, 'HIGH_RISK', 'HIGH-RISK TRANSACTION ALERT',
                        'Transaction TXN-20261004-0035 received a composite risk score of 82/100 (HIGH RISK). Reason: High transaction amount and statistical outlier.',
                        'NEW', '04 Oct 2026, 02:41 AM', '04 Oct 2026, 02:41 AM')
            """, account.getId());

            jdbcTemplate.update("""
                INSERT INTO fraud_alerts (alert_id, transaction_id, account_id, risk_score, risk_level, title, description, status, created_at, updated_at)
                VALUES ('ALT-20261004-0026', 'TXN-20261004-0026', ?, 70, 'SUSPICIOUS', 'SUSPICIOUS TRANSACTION ALERT',
                        'Transaction TXN-20261004-0026 received a composite risk score of 70/100 (SUSPICIOUS). Reason: Multiple rule violations noted for review.',
                        'NEW', '04 Oct 2026, 02:45 AM', '04 Oct 2026, 02:45 AM')
            """, account.getId());

            jdbcTemplate.update("""
                INSERT INTO fraud_alerts (alert_id, transaction_id, account_id, risk_score, risk_level, title, description, status, created_at, updated_at)
                VALUES ('ALT-20261004-0028', 'TXN-20261004-0028', ?, 65, 'SUSPICIOUS', 'SUSPICIOUS TRANSACTION ALERT',
                        'Transaction TXN-20261004-0028 received a composite risk score of 65/100 (SUSPICIOUS). Reason: Rapid consecutive payment burst detected.',
                        'REVIEW_REQUIRED', '04 Oct 2026, 03:13 PM', '04 Oct 2026, 03:15 PM')
            """, account.getId());
        }

        // Seed demonstration review cases (Phase 6) if none exist
        Integer reviewCaseCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM review_cases", Integer.class);
        if (reviewCaseCount == null || reviewCaseCount == 0) {
            logger.info("Seeding demonstration review cases for Phase 6 manual review workflow...");

            // Case 1: NEW High-Risk case
            jdbcTemplate.update("""
                INSERT INTO review_cases (case_id, alert_id, transaction_id, account_id, risk_score, risk_level, status, assigned_reviewer_id, reviewer_notes, decision, created_at, updated_at, resolved_at)
                VALUES ('CASE-20261004-0001', 'ALT-20261004-0035', 'TXN-20261004-0035', ?, 82, 'HIGH_RISK',
                        'NEW', NULL, NULL, NULL, '2026-10-04 02:41:00', '2026-10-04 02:41:00', NULL)
            """, account.getId());

            // Case 2: IN_REVIEW Suspicious case
            jdbcTemplate.update("""
                INSERT INTO review_cases (case_id, alert_id, transaction_id, account_id, risk_score, risk_level, status, assigned_reviewer_id, reviewer_notes, decision, created_at, updated_at, resolved_at)
                VALUES ('CASE-20261004-0002', 'ALT-20261004-0026', 'TXN-20261004-0026', ?, 70, 'SUSPICIOUS',
                        'IN_REVIEW', 'REV100001', 'Transaction originated from unusual location at 02:41 AM. Contacted cardholder via secure notification; awaiting secondary confirmation.', NULL, '2026-10-04 02:45:00', '2026-10-04 03:00:00', NULL)
            """, account.getId());

            // Case 3: RESOLVED Suspicious case
            jdbcTemplate.update("""
                INSERT INTO review_cases (case_id, alert_id, transaction_id, account_id, risk_score, risk_level, status, assigned_reviewer_id, reviewer_notes, decision, created_at, updated_at, resolved_at)
                VALUES ('CASE-20261004-0003', 'ALT-20261004-0028', 'TXN-20261004-0028', ?, 65, 'SUSPICIOUS',
                        'RESOLVED', 'REV100001', 'Customer confirmed rapid retry due to network timeout at POS terminal. Transaction verified legitimate.', 'LEGITIMATE', '2026-10-04 03:13:00', '2026-10-04 03:30:00', '2026-10-04 03:30:00')
            """, account.getId());

            // Seed audit logs for Case 2 and 3
            jdbcTemplate.update("""
                INSERT INTO review_audit_logs (case_id, reviewer_id, action, details, created_at)
                VALUES ('CASE-20261004-0002', 'REV100001', 'CASE_OPENED', 'Case opened and assigned to reviewer REV100001', '2026-10-04 02:46:00')
            """);
            jdbcTemplate.update("""
                INSERT INTO review_audit_logs (case_id, reviewer_id, action, details, created_at)
                VALUES ('CASE-20261004-0002', 'REV100001', 'NOTE_ADDED', 'Reviewer added preliminary investigation note', '2026-10-04 03:00:00')
            """);

            jdbcTemplate.update("""
                INSERT INTO review_audit_logs (case_id, reviewer_id, action, details, created_at)
                VALUES ('CASE-20261004-0003', 'REV100001', 'CASE_OPENED', 'Case opened and assigned to reviewer REV100001', '2026-10-04 03:14:00')
            """);
            jdbcTemplate.update("""
                INSERT INTO review_audit_logs (case_id, reviewer_id, action, details, created_at)
                VALUES ('CASE-20261004-0003', 'REV100001', 'DECISION_SUBMITTED', 'Disposition submitted: LEGITIMATE | Customer confirmed rapid retry', '2026-10-04 03:30:00')
            """);
        }

        // Seed initial security activity records if empty
        if (securityActivityRepository.findByBankId(demoBankId).isEmpty()) {
            securityActivityRepository.insertActivity(demoBankId, "ACCOUNT_PROVISIONED", "SecureBank digital customer account initialized", "127.0.0.1");
            securityActivityRepository.insertActivity(demoBankId, "SECURITY_PIN_SET", "Two-step demo verification PIN registered", "127.0.0.1");
            securityActivityRepository.insertActivity(demoBankId, "DEVICE_AUTHORIZED", "Localhost development browser recognized", "127.0.0.1");
        }

        logger.info("SecureBank SQLite Database schema and demo data initialized successfully.");
    }

    private void seedTransaction(Long accountId, String txnId, String type, String desc,
                                 Double amount, String direction, String recipient,
                                 String location, String timestamp, String status) {
        Transaction t = new Transaction();
        t.setAccountId(accountId);
        t.setTransactionId(txnId);
        t.setTransactionType(type);
        t.setDescription(desc);
        t.setAmount(amount);
        t.setTransactionDirection(direction);
        t.setRecipient(recipient);
        t.setLocation(location);
        t.setTimestamp(timestamp);
        t.setStatus(status);
        transactionRepository.insertTransaction(t);
    }
}
