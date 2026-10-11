
package com.securebank.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.securebank.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LedgerEntryRepository
        extends JpaRepository<LedgerEntry, UUID> {

    List<LedgerEntry> findByTransfer_Id(UUID transferId);

    List<LedgerEntry> findByAccount_IdOrderByCreatedAtDesc(
            UUID accountId);
    Page<LedgerEntry> findByAccount_Id(
            UUID accountId,
            Pageable pageable
    );
}
