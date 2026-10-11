package com.securebank.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(

        @NotNull(message = "Sender account ID is required")
        UUID fromAccountId,

        @NotNull(message = "Receiver account ID is required")
        UUID toAccountId,

        @NotNull(message = "Transfer amount is required")
        @DecimalMin(
                value = "0.01",
                message = "Transfer amount must be greater than zero"
        )
        @Digits(
                integer = 17,
                fraction = 2,
                message = "Amount must have up to 17 integer digits and 2 decimal places"
        )
        BigDecimal amount,

        @NotNull(message = "Currency is required")
        @Pattern(
                regexp = "INR",
                message = "Only INR transfers are supported"
        )
        String currency,

        @NotNull(message = "Idempotency key is required")
        @Size(
                min = 1,
                max = 100,
                message = "Idempotency key must be between 1 and 100 characters"
        )
        String idempotencyKey
) {
}
