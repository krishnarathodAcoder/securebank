package com.securebank.controller;

import com.securebank.dto.TransactionHistoryResponse;
import com.securebank.entity.LedgerEntry;
import com.securebank.service.TransactionHistoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class TransactionHistoryController {

    private final TransactionHistoryService transactionHistoryService;

    public TransactionHistoryController(
            TransactionHistoryService transactionHistoryService) {
        this.transactionHistoryService = transactionHistoryService;
    }

    @GetMapping("/{accountId}/transactions")
    public List<TransactionHistoryResponse> getAccountTransactions(
            @PathVariable UUID accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return transactionHistoryService.getAccountTransactions(
                accountId, page, size);
    }
}