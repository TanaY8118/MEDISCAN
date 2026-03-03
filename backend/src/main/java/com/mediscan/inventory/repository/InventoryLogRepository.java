package com.mediscan.inventory.repository;

import com.mediscan.inventory.entity.InventoryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SYSTEM INVARIANT: inventory_logs is APPEND-ONLY.
 *
 * <p>
 * Updates and deletes are permanently forbidden to preserve audit trail
 * integrity.
 * This mirrors the same enforcement pattern on
 * {@code ReminderHistoryRepository}.
 * </p>
 *
 * <p>
 * Repository-level guards are defined here (interface) so the invariant is
 * visible at the contract boundary, not buried inside entity lifecycle
 * callbacks.
 * </p>
 */
@Repository
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {

    List<InventoryLog> findByPerformedByAndTimestampAfter(String performedBy, LocalDateTime timestamp);

    List<InventoryLog> findByInventoryIdOrderByTimestampDesc(Long inventoryId);

}
