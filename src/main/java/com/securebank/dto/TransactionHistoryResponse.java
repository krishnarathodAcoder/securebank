package com.securebank.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionHistoryResponse(
        UUID transactionId,
        UUID transferId,
        String entryType,
        BigDecimal amount,
        Instant createdAt
) {
}