package com.securebank.controller;

import com.securebank.dto.AccountDto;
import com.securebank.dto.ApiResponse;
import com.securebank.model.Account;
import com.securebank.repository.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountRepository accountRepository;
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    private String maskAccountNumber(String rawNumber) {
        if (rawNumber == null || rawNumber.length() < 4) {
            return "XXXX XXXX 4582";
        }
        String lastFour = rawNumber.substring(rawNumber.length() - 4);
        return "XXXX XXXX " + lastFour;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyAccount(Authentication authentication) {
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
        String createdStr = account.getCreatedAt() != null
                ? account.getCreatedAt().format(DISPLAY_FORMATTER)
                : "15 Jan 2025";

        AccountDto dto = new AccountDto(
                maskAccountNumber(account.getAccountNumber()),
                account.getCustomerName(),
                account.getBankId(),
                account.getAccountType(),
                account.getBalance(),
                createdStr
        );

        return ResponseEntity.ok(dto);
    }
}
