package com.securebank.service;

import com.securebank.dto.TransactionHistoryResponse;
import com.securebank.entity.LedgerEntry;
import com.securebank.repository.LedgerEntryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TransactionHistoryService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public TransactionHistoryService(
            LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional(readOnly = true)
    public List<TransactionHistoryResponse> getAccountTransactions(
            UUID accountId, int page, int size) {

        // Validate pagination values
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100");
        }

        // Fetch one page, newest transactions first
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        List<LedgerEntry> entries = ledgerEntryRepository
                .findByAccount_Id(accountId, pageable)
                .getContent();

        // Convert database entities into safe response DTOs
        return entries.stream()
                .map(entry -> new TransactionHistoryResponse(
                        entry.getId(),
                        entry.getTransfer().getId(),
                        entry.getEntryType(),
                        entry.getAmount(),
                        entry.getCreatedAt()
                ))
                .toList();
    }
}
