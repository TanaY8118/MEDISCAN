package com.mediscan.inventory.repository;

import com.mediscan.inventory.entity.InventoryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {

    List<InventoryLog> findByPerformedByAndTimestampAfter(String performedBy, LocalDateTime timestamp);

    List<InventoryLog> findByInventoryIdOrderByTimestampDesc(Long inventoryId);
}
