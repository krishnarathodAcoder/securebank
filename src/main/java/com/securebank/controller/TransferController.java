package com.securebank.controller;

import com.securebank.dto.TransferRequest;
import com.securebank.entity.Transfer;
import com.securebank.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    // Create a money transfer
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createTransfer(
            @Valid @RequestBody TransferRequest request) {

        Transfer transfer = transferService.createTransfer(request);

        return Map.of(
                "message", "Transfer completed successfully",
                "transferId", transfer.getId(),
                "fromAccountId", transfer.getFromAccount().getId(),
                "toAccountId", transfer.getToAccount().getId(),
                "amount", transfer.getAmount(),
                "currency", transfer.getCurrency(),
                "status", transfer.getStatus(),
                "createdAt", transfer.getCreatedAt()
        );
    }

    // Get transfer details
    @GetMapping("/{id}")
    public Map<String, Object> getTransferById(
            @PathVariable UUID id) {

        Transfer transfer = transferService.getTransferById(id);

        return Map.of(
                "transferId", transfer.getId(),
                "fromAccountId", transfer.getFromAccount().getId(),
                "toAccountId", transfer.getToAccount().getId(),
                "amount", transfer.getAmount(),
                "currency", transfer.getCurrency(),
                "status", transfer.getStatus(),
                "createdAt", transfer.getCreatedAt()
        );
    }
}
