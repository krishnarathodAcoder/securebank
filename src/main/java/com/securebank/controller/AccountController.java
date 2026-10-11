
package com.securebank.controller;

import com.securebank.entity.Account;
import com.securebank.service.AccountService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // Create an account for the logged-in customer
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createAccount(
            Authentication authentication) {

        UUID userId = getAuthenticatedUserId(authentication);
        Account account = accountService.createAccount(userId);

        return Map.of(
                "message", "Account created successfully",
                "accountId", account.getId(),
                "accountNumber", account.getAccountNumber(),
                "currency", account.getCurrency(),
                "balance", account.getBalance(),
                "status", account.getStatus()
        );
    }

    // List only the logged-in customer's accounts
    @GetMapping
    public List<Map<String, Object>> getAccounts(
            Authentication authentication) {

        UUID userId = getAuthenticatedUserId(authentication);

        return accountService.getAccountsByUserId(userId)
                .stream()
                .map(account -> Map.<String, Object>of(
                        "accountId", account.getId(),
                        "accountNumber", account.getAccountNumber(),
                        "currency", account.getCurrency(),
                        "balance", account.getBalance(),
                        "status", account.getStatus()
                ))
                .toList();
    }

    // Get an account only if the customer owns it
    @GetMapping("/{id}")
    public Map<String, Object> getAccountById(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID userId = getAuthenticatedUserId(authentication);
        Account account = accountService.getAccountById(id, userId);

        return Map.of(
                "accountId", account.getId(),
                "accountNumber", account.getAccountNumber(),
                "currency", account.getCurrency(),
                "balance", account.getBalance(),
                "status", account.getStatus()
        );
    }

    // Get the balance only if the customer owns the account
    @GetMapping("/{id}/balance")
    public Map<String, Object> getAccountBalance(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID userId = getAuthenticatedUserId(authentication);
        BigDecimal balance =
                accountService.getAccountBalance(id, userId);

        return Map.of(
                "accountId", id,
                "balance", balance
        );
    }

    private UUID getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Authentication is required");
        }

        String email = authentication.getName();

        // Our current session stores the customer's email as the principal.
        // Resolve the corresponding database user ID.
        return accountService.getUserIdByEmail(email);
    }
}
