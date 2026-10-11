package com.securebank.service;

import com.securebank.dto.TransferRequest;
import com.securebank.entity.Account;
import com.securebank.entity.LedgerEntry;
import com.securebank.entity.Transfer;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.LedgerEntryRepository;
import com.securebank.repository.TransferRepository;
import com.securebank.service.AuditLogService;
import com.securebank.service.FraudDetectionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransferService {

    private final FraudDetectionService fraudDetectionService;
    private final AuditLogService auditLogService;
    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public TransferService(
            FraudDetectionService fraudDetectionService,
            AuditLogService auditLogService,
            AccountRepository accountRepository,
            TransferRepository transferRepository,
            LedgerEntryRepository ledgerEntryRepository) {

        this.fraudDetectionService = fraudDetectionService;
        this.auditLogService = auditLogService;
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public Transfer createTransfer(TransferRequest request) {

        // 1. Check whether the idempotency key has already been used
        if (transferRepository.existsByIdempotencyKey(
                request.idempotencyKey())) {
            throw new IllegalArgumentException(
                    "Idempotency key has already been used");
        }

        // 2. Sender and receiver must be different
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new IllegalArgumentException(
                    "Sender and receiver accounts must be different");
        }

        // 3. Validate transfer amount
        BigDecimal amount = request.amount();

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Transfer amount must be greater than zero");
        }

        if (amount.scale() > 2) {
            throw new IllegalArgumentException(
                    "Amount cannot have more than two decimal places");
        }

        // 4. Only INR transfers are supported
        if (!"INR".equals(request.currency())) {
            throw new IllegalArgumentException(
                    "Only INR transfers are supported");
        }

        // 5. Find sender account
        Account sender = accountRepository
                .findById(request.fromAccountId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Sender account not found"));

        // 6. Find receiver account
        Account receiver = accountRepository
                .findById(request.toAccountId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Receiver account not found"));

        // 7. Both accounts must be active
        if (!"ACTIVE".equals(sender.getStatus())
                || !"ACTIVE".equals(receiver.getStatus())) {
            throw new IllegalArgumentException(
                    "Both accounts must be active");
        }

        // 8. Validate account currencies
        if (!request.currency().equals(sender.getCurrency())
                || !request.currency().equals(receiver.getCurrency())) {
            throw new IllegalArgumentException(
                    "Account currency does not match transfer currency");
        }

        // 9. Check sender's available balance
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient account balance");
        }

        // 10. Update both account balances
        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        accountRepository.save(sender);
        accountRepository.save(receiver);

        // 11. Save the transfer record
        Transfer transfer = new Transfer();
        transfer.setFromAccount(sender);
        transfer.setToAccount(receiver);
        transfer.setAmount(amount);
        transfer.setCurrency(request.currency());
        transfer.setIdempotencyKey(request.idempotencyKey());
        transfer.setStatus("COMPLETED");

        Transfer savedTransfer = transferRepository.save(transfer);

        // 12. Check the transfer for suspicious activity
        fraudDetectionService.checkTransfer(savedTransfer);

        // 13. Create the sender's debit ledger entry
        LedgerEntry debit = new LedgerEntry();
        debit.setTransfer(savedTransfer);
        debit.setAccount(sender);
        debit.setEntryType("DEBIT");
        debit.setAmount(amount);

        ledgerEntryRepository.save(debit);

        // 14. Create the receiver's credit ledger entry
        LedgerEntry credit = new LedgerEntry();
        credit.setTransfer(savedTransfer);
        credit.setAccount(receiver);
        credit.setEntryType("CREDIT");
        credit.setAmount(amount);

        ledgerEntryRepository.save(credit);

        // 15. Record the successful transfer in the audit log
        // The actor ID is temporarily null because authentication
        // is not yet connected to audit logging.
        auditLogService.recordAction(
                null,
                "TRANSFER_COMPLETED",
                "TRANSFER",
                savedTransfer.getId()
        );

        return savedTransfer;
    }

    @Transactional(readOnly = true)
    public Transfer getTransferById(UUID transferId) {
        return transferRepository.findById(transferId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Transfer not found"));
    }
}
