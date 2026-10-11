
package com.securebank.service;

import com.securebank.entity.FraudAlert;
import com.securebank.entity.Transfer;
import com.securebank.repository.FraudAlertRepository;
import com.securebank.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class FraudDetectionService {

    private static final BigDecimal LARGE_TRANSFER_THRESHOLD =
            new BigDecimal("50000.00");

    private static final int TRANSFER_LIMIT = 5;
    private static final int TIME_WINDOW_MINUTES = 10;

    private final FraudAlertRepository fraudAlertRepository;
    private final TransferRepository transferRepository;

    public FraudDetectionService(
            FraudAlertRepository fraudAlertRepository,
            TransferRepository transferRepository) {
        this.fraudAlertRepository = fraudAlertRepository;
        this.transferRepository = transferRepository;
    }

    @Transactional
    public void checkTransfer(Transfer transfer) {

        // Rule 1: Large transfer
        if (transfer.getAmount().compareTo(LARGE_TRANSFER_THRESHOLD) > 0) {
            createAlert(transfer, "LARGE_TRANSFER", 70);
        }

        // Rule 2: Too many transfers in 10 minutes
        Instant startTime = Instant.now()
                .minus(TIME_WINDOW_MINUTES, ChronoUnit.MINUTES);

        long transferCount =
                transferRepository.countByFromAccount_IdAndCreatedAtAfter(
                        transfer.getFromAccount().getId(),
                        startTime
                );

        if (transferCount >= TRANSFER_LIMIT) {
            createAlert(transfer, "HIGH_TRANSFER_FREQUENCY", 80);
        }
    }

    private void createAlert(
            Transfer transfer,
            String ruleCode,
            int riskScore) {

        FraudAlert alert = new FraudAlert();
        alert.setTransfer(transfer);
        alert.setRuleCode(ruleCode);
        alert.setRiskScore(riskScore);
        alert.setStatus("OPEN");

        fraudAlertRepository.save(alert);
    }
}
