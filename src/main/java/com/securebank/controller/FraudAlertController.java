
package com.securebank.controller;

import com.securebank.entity.FraudAlert;
import com.securebank.service.FraudAlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/fraud-alerts")
public class FraudAlertController {

    private final FraudAlertService fraudAlertService;

    public FraudAlertController(
            FraudAlertService fraudAlertService) {
        this.fraudAlertService = fraudAlertService;
    }

    // Get all open fraud alerts
    @GetMapping
    public ResponseEntity<List<FraudAlert>> getOpenAlerts() {
        return ResponseEntity.ok(
                fraudAlertService.getOpenAlerts());
    }

    // Mark a fraud alert as reviewed
    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, String>> reviewAlert(
            @PathVariable UUID id) {

        FraudAlert alert = fraudAlertService.reviewAlert(id);

        return ResponseEntity.ok(Map.of(
                "message", "Fraud alert reviewed successfully",
                "alertId", alert.getId().toString(),
                "status", alert.getStatus()
        ));
    }
}
