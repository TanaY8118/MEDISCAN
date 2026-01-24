package com.mediscan.inventory.service;

import com.mediscan.inventory.dto.InventoryRequest;
import com.mediscan.inventory.dto.InventoryResponse;
import com.mediscan.inventory.entity.Inventory;
import com.mediscan.inventory.entity.InventoryLog;
import com.mediscan.inventory.entity.LogReason;
import com.mediscan.inventory.repository.InventoryLogRepository;
import com.mediscan.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryLogRepository inventoryLogRepository;

    public List<InventoryResponse> getAllInventories() {
        return inventoryRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public InventoryResponse addOrUpdateInventory(InventoryRequest request) {
        Inventory inventory = inventoryRepository.findByMedicineId(request.getMedicineId())
                .orElse(Inventory.builder()
                        .medicineId(request.getMedicineId())
                        .quantity(0)
                        .lowStockThreshold(request.getLowStockThreshold())
                        .build());

        inventory.setQuantity(request.getQuantity());
        inventory.setLowStockThreshold(request.getLowStockThreshold());

        return toResponse(inventoryRepository.save(inventory));
    }

    @Transactional
    public InventoryResponse adjustStock(String medicineId, int adjustment) {
        Inventory inventory = inventoryRepository.findByMedicineId(medicineId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for medicine: " + medicineId));

        int newQuantity = inventory.getQuantity() + adjustment;
        if (newQuantity < 0) {
            throw new RuntimeException("Insufficient stock");
        }

        inventory.setQuantity(newQuantity);
        return toResponse(inventoryRepository.save(inventory));
    }

    public InventoryResponse getInventoryByMedicineId(String medicineId) {
        Inventory inventory = inventoryRepository.findByMedicineId(medicineId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));
        return toResponse(inventory);
    }

    /**
     * SYSTEM INVARIANT: Atomic Quantity Update with Mandatory Logging
     *
     * This method ensures that ANY quantity change is:
     * 1. Validated (non-negative, within bounds)
     * 2. Logged atomically (same transaction)
     * 3. Rolled back entirely if logging fails
     *
     * @param inventoryId  The inventory to update
     * @param changeAmount The amount to change (negative for consumption)
     * @param reason       The reason for the change
     * @param note         Additional context
     * @return Updated inventory
     * @throws IllegalArgumentException if change would result in negative quantity
     * @throws IllegalStateException    if logging fails (triggers rollback)
     */
    @Transactional
    public Inventory updateQuantity(Long inventoryId, Integer changeAmount, LogReason reason, String note) {
        String userId = getCurrentUser();

        // 1. Retrieve inventory
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + inventoryId));

        // 2. Calculate new quantity
        int quantityBefore = inventory.getQuantity();
        int quantityAfter = quantityBefore + changeAmount;

        // 3. INVARIANT: Quantity must be non-negative
        if (quantityAfter < 0) {
            throw new IllegalArgumentException(
                    String.format("INVARIANT VIOLATION: Quantity cannot be negative. " +
                            "Current: %d, Change: %d, Would result in: %d",
                            quantityBefore, changeAmount, quantityAfter));
        }

        // 4. Update inventory
        inventory.setQuantity(quantityAfter);
        Inventory savedInventory = inventoryRepository.save(inventory);

        // 5. CRITICAL: Create inventory log (MUST succeed or transaction rolls back)
        InventoryLog inventoryLog = InventoryLog.builder()
                .inventoryId(inventoryId)
                .reason(reason)
                .changeAmount(changeAmount)
                .quantityBefore(quantityBefore)
                .quantityAfter(quantityAfter)
                .note(note)
                .performedBy(userId)
                .timestamp(LocalDateTime.now())
                .build();

        try {
            inventoryLogRepository.save(inventoryLog);
            log.info("Inventory updated: ID={}, Change={}, Reason={}, New Quantity={}",
                    inventoryId, changeAmount, reason, quantityAfter);
        } catch (Exception e) {
            log.error("CRITICAL: Failed to create inventory log. Transaction will roll back.", e);
            throw new IllegalStateException(
                    "INVARIANT VIOLATION: Inventory log creation failed. Quantity change aborted.", e);
        }

        return savedInventory;
    }

    private InventoryResponse toResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .medicineId(inventory.getMedicineId())
                .quantity(inventory.getQuantity())
                .lowStockThreshold(inventory.getLowStockThreshold())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }

    private String getCurrentUser() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "SYSTEM";
        }
    }
}
