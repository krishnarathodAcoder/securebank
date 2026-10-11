
package com.securebank.service;

import com.securebank.entity.Account;
import com.securebank.entity.User;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public AccountService(
            AccountRepository accountRepository,
            UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Account createAccount(UUID authenticatedUserId) {
        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        String accountNumber;

        do {
            accountNumber = generateAccountNumber();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber(accountNumber);
        account.setCurrency("INR");
        account.setStatus("ACTIVE");

        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public List<Account> getAccountsByUserId(UUID authenticatedUserId) {
        if (!userRepository.existsById(authenticatedUserId)) {
            throw new IllegalArgumentException("User not found");
        }

        return accountRepository.findByUser_Id(authenticatedUserId);
    }

    @Transactional(readOnly = true)
    public Account getAccountById(
            UUID accountId, UUID authenticatedUserId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Account not found"));

        verifyOwnership(account, authenticatedUserId);

        return account;
    }

    @Transactional(readOnly = true)
    public BigDecimal getAccountBalance(
            UUID accountId, UUID authenticatedUserId) {

        Account account = getAccountById(accountId, authenticatedUserId);
        return account.getBalance();
    }

    private void verifyOwnership(Account account, UUID authenticatedUserId) {
        if (!account.getUser().getId().equals(authenticatedUserId)) {
            throw new AccessDeniedException(
                    "You are not authorized to access this account");
        }
    }

    private String generateAccountNumber() {
        StringBuilder number = new StringBuilder();

        for (int i = 0; i < 12; i++) {
            number.append(secureRandom.nextInt(10));
        }

        return number.toString();
    }


    @Transactional(readOnly = true)
    public UUID getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"))
                .getId();
    }

}
