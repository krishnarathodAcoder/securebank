
package com.securebank.service;

import com.securebank.entity.FraudAlert;
import com.securebank.repository.FraudAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FraudAlertService {

    private final FraudAlertRepository fraudAlertRepository;

    public FraudAlertService(
            FraudAlertRepository fraudAlertRepository) {
        this.fraudAlertRepository = fraudAlertRepository;
    }

    public List<FraudAlert> getOpenAlerts() {
        return fraudAlertRepository
                .findByStatusOrderByCreatedAtDesc("OPEN");
    }

    @Transactional
    public FraudAlert reviewAlert(UUID alertId) {
        FraudAlert alert = fraudAlertRepository.findById(alertId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Fraud alert not found: " + alertId));

        alert.setStatus("REVIEWED");

        return fraudAlertRepository.save(alert);
    }
}
