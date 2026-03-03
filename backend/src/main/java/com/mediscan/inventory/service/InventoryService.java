package com.mediscan.inventory.service;

import com.mediscan.dto.v1.inventory.InventoryRequestDTO;
import com.mediscan.dto.v1.inventory.InventoryResponseDTO;
import com.mediscan.inventory.entity.Inventory;
import com.mediscan.inventory.entity.InventoryLog;
import com.mediscan.inventory.entity.LogReason;
import com.mediscan.inventory.repository.InventoryLogRepository;
import com.mediscan.inventory.repository.InventoryRepository;
import com.mediscan.user.entity.User;
import com.mediscan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

        private final InventoryRepository inventoryRepository;
        private final InventoryLogRepository inventoryLogRepository;
        private final UserRepository userRepository;

        public List<InventoryResponseDTO> getAllInventories() {
                User currentUser = getCurrentUserEntity();
                return inventoryRepository.findByUserId(currentUser.getId()).stream()
                                .map(this::toResponse)
                                .collect(Collectors.toList());
        }

        /**
         * Creates or updates an inventory record.
         *
         * <p>
         * <b>Quantity invariant:</b> All quantity changes are routed through
         * {@link #updateQuantity} to guarantee an audit log entry for every change.
         * The {@code lowStockThreshold} is metadata and may be updated directly.
         * </p>
         *
         * <ul>
         * <li><b>New record:</b> created with {@code quantity=0}; initial stock is
         * applied via {@code updateQuantity(ADDITION)}.</li>
         * <li><b>Existing record:</b> delta is computed and applied via
         * {@code updateQuantity(ADJUSTMENT)} only when quantity changes.</li>
         * </ul>
         */
        @Transactional
        public InventoryResponseDTO addOrUpdateInventory(InventoryRequestDTO request) {
                User currentUser = getCurrentUserEntity();

                Optional<Inventory> existing = inventoryRepository
                                .findByMedicineIdAndUserId(request.getMedicineId(), currentUser.getId());

                if (existing.isEmpty()) {
                        // Create the record with quantity=0 first to obtain its persisted ID
                        Inventory newInventory = inventoryRepository.save(Inventory.builder()
                                        .user(currentUser)
                                        .medicineId(request.getMedicineId())
                                        .quantity(0)
                                        .lowStockThreshold(request.getLowStockThreshold())
                                        .unit("tablet") // default unit — DTO v1 does not expose this field
                                        .build());

                        // Log the initial stock addition if quantity > 0
                        if (request.getQuantity() > 0) {
                                Inventory withStock = updateQuantity(
                                                newInventory.getId(),
                                                request.getQuantity(),
                                                LogReason.ADDITION,
                                                "Initial stock set");
                                return toResponse(withStock);
                        }
                        return toResponse(newInventory);

                } else {
                        Inventory inventory = existing.get();

                        // Update threshold metadata — not a quantity change, no log needed
                        inventory.setLowStockThreshold(request.getLowStockThreshold());
                        inventoryRepository.save(inventory);

                        // Route any quantity delta through the audited update path
                        int delta = request.getQuantity() - inventory.getQuantity();
                        if (delta != 0) {
                                Inventory updated = updateQuantity(
                                                inventory.getId(),
                                                delta,
                                                LogReason.ADJUSTMENT,
                                                "Inventory quantity updated via API");
                                return toResponse(updated);
                        }

                        return toResponse(inventoryRepository
                                        .findByIdAndUserId(inventory.getId(), currentUser.getId())
                                        .orElseThrow(() -> new IllegalStateException("Inventory not found after save: "
                                                        + inventory.getId())));
                }
        }

        public InventoryResponseDTO getInventoryByMedicineId(String medicineId) {
                User currentUser = getCurrentUserEntity();

                Inventory inventory = inventoryRepository
                                .findByMedicineIdAndUserId(medicineId, currentUser.getId())
                                .orElseThrow(() -> new com.mediscan.common.exception.ResourceNotFoundException(
                                                "Inventory not found"));
                return toResponse(inventory);
        }

        /**
         * Adjusts stock by a signed amount for the given medicine, with mandatory
         * logging.
         *
         * <p>
         * This is the ONLY unlogged-free path for external stock adjustment.
         * All mutations are routed through {@link #updateQuantity}.
         * </p>
         *
         * @param medicineId MongoDB medicine ID
         * @param adjustment Signed amount (positive = addition, negative = consumption)
         */
        @Transactional
        public InventoryResponseDTO adjustStock(String medicineId, int adjustment) {
                User currentUser = getCurrentUserEntity();

                Inventory inventory = inventoryRepository
                                .findByMedicineIdAndUserId(medicineId, currentUser.getId())
                                .orElseThrow(() -> new com.mediscan.common.exception.ResourceNotFoundException(
                                                "Inventory not found for medicine: " + medicineId));

                Inventory updated = updateQuantity(
                                inventory.getId(),
                                adjustment,
                                LogReason.ADJUSTMENT,
                                "Stock adjusted via API");

                return toResponse(updated);
        }

        /**
         * SYSTEM INVARIANT: Atomic Quantity Update with Mandatory Logging
         *
         * <p>
         * This is the <b>sole authorised path</b> for modifying inventory quantity.
         * Every call produces an immutable {@code InventoryLog} entry in the same
         * transaction. No quantity change may bypass this method.
         * </p>
         *
         * <p>
         * Guarantees:
         * </p>
         * <ol>
         * <li>Quantity is validated non-negative before any write</li>
         * <li>Quantity update and log creation are atomic (same
         * {@code @Transactional})</li>
         * <li>If log creation fails, the entire transaction rolls back</li>
         * <li>Ownership is re-validated inside this method — callers cannot bypass
         * it</li>
         * </ol>
         *
         * @param inventoryId  The inventory record to update
         * @param changeAmount Signed delta (negative for consumption)
         * @param reason       Audit reason
         * @param note         Human-readable context for the log entry
         * @return The saved, updated Inventory entity
         * @throws IllegalArgumentException if the change would result in negative
         *                                  quantity
         * @throws IllegalStateException    if log creation fails (triggers full
         *                                  rollback)
         */
        @Transactional
        public Inventory updateQuantity(Long inventoryId, Integer changeAmount, LogReason reason, String note) {
                User currentUser = getCurrentUserEntity();
                String username = currentUser.getUsername();

                // 1. Retrieve and ownership-check the inventory record
                Inventory inventory = inventoryRepository.findByIdAndUserId(inventoryId, currentUser.getId())
                                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + inventoryId));

                // 2. Compute new quantity
                int quantityBefore = inventory.getQuantity();
                int quantityAfter = quantityBefore + changeAmount;

                // 3. INVARIANT: Quantity must be non-negative
                if (quantityAfter < 0) {
                        throw new IllegalArgumentException(
                                        String.format("INVARIANT VIOLATION: Quantity cannot be negative. " +
                                                        "Current: %d, Change: %d, Would result in: %d",
                                                        quantityBefore, changeAmount, quantityAfter));
                }

                // 4. Persist quantity update
                inventory.setQuantity(quantityAfter);
                Inventory savedInventory = inventoryRepository.save(inventory);

                // 5. CRITICAL: Create audit log — MUST succeed or transaction rolls back
                InventoryLog inventoryLog = InventoryLog.builder()
                                .inventoryId(inventoryId)
                                .user(currentUser)
                                .reason(reason)
                                .changeAmount(changeAmount)
                                .quantityBefore(quantityBefore)
                                .quantityAfter(quantityAfter)
                                .note(note)
                                .performedBy(username)
                                .timestamp(LocalDateTime.now())
                                .build();

                try {
                        inventoryLogRepository.save(inventoryLog);
                        log.info("Inventory updated: ID={}, Change={}, Reason={}, New Quantity={}",
                                        inventoryId, changeAmount, reason, quantityAfter);
                } catch (Exception e) {
                        log.error("CRITICAL: Failed to create inventory log. Transaction will roll back.", e);
                        throw new IllegalStateException(
                                        "INVARIANT VIOLATION: Inventory log creation failed. Quantity change aborted.",
                                        e);
                }

                return savedInventory;
        }

        // -------------------------------------------------------------------------
        // Mappers
        // -------------------------------------------------------------------------

        private InventoryResponseDTO toResponse(Inventory inventory) {
                return InventoryResponseDTO.builder()
                                .id(inventory.getId())
                                .medicineId(inventory.getMedicineId())
                                .quantity(inventory.getQuantity())
                                .lowStockThreshold(inventory.getLowStockThreshold())
                                .updatedAt(inventory.getUpdatedAt())
                                .build();
        }

        // -------------------------------------------------------------------------
        // Principal resolution
        // -------------------------------------------------------------------------

        private String getCurrentUser() {
                // INVARIANT: An authenticated principal is required for all inventory
                // operations.
                // Unauthenticated access is a security violation — there is no SYSTEM fallback.
                return SecurityContextHolder.getContext().getAuthentication().getName();
        }

        /**
         * Resolve the current authenticated principal to a persistent User entity.
         * This is the single source of truth for ownership lookups.
         */
        private User getCurrentUserEntity() {
                String username = getCurrentUser();
                return userRepository.findByUsername(username)
                                .orElseThrow(() -> new IllegalStateException(
                                                "Authenticated user not found: " + username));
        }
}
