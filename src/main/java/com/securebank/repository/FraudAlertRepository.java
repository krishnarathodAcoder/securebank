
package com.securebank.repository;

import com.securebank.entity.FraudAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FraudAlertRepository
        extends JpaRepository<FraudAlert, UUID> {

    // Get alerts by status, newest first
    List<FraudAlert> findByStatusOrderByCreatedAtDesc(String status);

    // Get alerts related to a particular transfer
    List<FraudAlert> findByTransfer_Id(UUID transferId);

    // Find one fraud alert by its ID
    Optional<FraudAlert> findById(UUID id);
}
