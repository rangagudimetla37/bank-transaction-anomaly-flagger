package com.securebank.controller;

import com.securebank.dto.ApiResponse;
import com.securebank.dto.TransactionDto;
import com.securebank.dto.TransactionSummaryDto;
import com.securebank.model.Account;
import com.securebank.model.Transaction;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionController(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    private TransactionDto toDto(Transaction t) {
        return new TransactionDto(
                t.getTransactionId(),
                t.getTransactionType(),
                t.getDescription(),
                t.getAmount(),
                t.getTransactionDirection(),
                t.getRecipient(),
                t.getLocation(),
                t.getTimestamp(),
                t.getStatus()
        );
    }

    private String maskAccountNumber(String rawNumber) {
        if (rawNumber == null || rawNumber.length() < 4) {
            return "XXXX XXXX 4582";
        }
        return "XXXX XXXX " + rawNumber.substring(rawNumber.length() - 4);
    }

    @GetMapping
    public ResponseEntity<?> getTransactions(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required."));
        }

        String bankId = authentication.getName();
        Optional<Account> accountOpt = accountRepository.findByBankId(bankId);

        if (accountOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Customer account not found."));
        }

        Account account = accountOpt.get();
        List<Transaction> transactions = transactionRepository.findByAccountId(account.getId());
        List<TransactionDto> dtos = transactions.stream().map(this::toDto).collect(Collectors.toList());

        Double moneyIn = transactionRepository.getMoneyIn(account.getId());
        Double moneyOut = transactionRepository.getMoneyOut(account.getId());
        int totalCount = transactionRepository.countByAccountId(account.getId());

        TransactionSummaryDto summary = new TransactionSummaryDto(
                account.getBalance(),
                totalCount,
                moneyIn,
                moneyOut,
                account.getCustomerName(),
                maskAccountNumber(account.getAccountNumber()),
                dtos
        );

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<?> getTransactionDetail(Authentication authentication, @PathVariable String transactionId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required."));
        }

        String bankId = authentication.getName();
        Optional<Account> accountOpt = accountRepository.findByBankId(bankId);

        if (accountOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Customer account not found."));
        }

        Account account = accountOpt.get();
        // Crucial security requirement: Must match both transactionId AND customer's accountId!
        Optional<Transaction> txnOpt = transactionRepository.findByTransactionIdAndAccountId(transactionId, account.getId());

        if (txnOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Transaction not found."));
        }

        return ResponseEntity.ok(toDto(txnOpt.get()));
    }
}
