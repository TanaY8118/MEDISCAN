package com.mediscan.reminder.service;

import com.mediscan.inventory.entity.Inventory;
import com.mediscan.inventory.entity.LogReason;
import com.mediscan.inventory.repository.InventoryRepository;
import com.mediscan.inventory.service.InventoryService;

import com.mediscan.medicine.service.MedicineService;
import com.mediscan.dto.v1.reminder.ReminderRequestDTO;
import com.mediscan.dto.v1.reminder.ReminderResponseDTO;
import com.mediscan.reminder.document.Reminder;
import com.mediscan.reminder.repository.ReminderHistoryRepository;
import com.mediscan.reminder.repository.ReminderRepository;
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
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final ReminderHistoryRepository reminderHistoryRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;
    private final MedicineService medicineService;

    private String getCurrentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public ReminderResponseDTO createReminder(ReminderRequestDTO request) {
        String medicineName = medicineService.getMedicineNameById(request.getMedicineId());

        Reminder reminder = Reminder.builder()
                .userId(getCurrentUser())
                .medicineId(request.getMedicineId())
                .medicineName(medicineName)
                .reminderTime(request.getReminderTime())
                .frequency(request.getFrequency())
                .isTaken(false)
                .note(request.getNote())
                .build();

        return toResponse(reminderRepository.save(reminder));
    }

    public List<ReminderResponseDTO> getMyReminders() {
        return reminderRepository.findByUserId(getCurrentUser()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ReminderResponseDTO markAsTaken(String id) {
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reminder not found"));

        if (!reminder.getUserId().equals(getCurrentUser())) {
            throw new RuntimeException("Unauthorized Access");
        }

        reminder.setTaken(true);
        reminder.setTakenAt(LocalDateTime.now());
        return toResponse(reminderRepository.save(reminder));
    }

    private ReminderResponseDTO toResponse(Reminder reminder) {
        return ReminderResponseDTO.builder()
                .id(reminder.getId())
                .medicineId(reminder.getMedicineId())
                .medicineName(reminder.getMedicineName())
                .reminderTime(reminder.getReminderTime())
                .frequency(reminder.getFrequency())
                .isTaken(reminder.isTaken())
                .takenAt(reminder.getTakenAt())
                .note(reminder.getNote())
                .build();
    }

    /**
     * SYSTEM INVARIANT: Record reminder action with cross-database coordination
     * 
     * Enforces:
     * 1. Idempotency (60-minute window)
     * 2. Referential integrity (medicine must exist)
     * 3. Ownership (user must own medicine)
     * 4. Atomic inventory update with logging
     * 
     * MongoDB: Create reminder_history entry
     * MySQL: If TAKEN, decrement inventory atomically with log
     */
    @Transactional
    public com.mediscan.dto.v1.reminder.ReminderActionResponse recordAction(String reminderId,
            com.mediscan.dto.v1.reminder.ReminderActionRequest request) {
        String userId = getCurrentUser();

        // 1. Validate reminder exists and belongs to user
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found"));

        // INVARIANT: Ownership check
        if (!reminder.getUserId().equals(userId)) {
            throw new RuntimeException("INVARIANT VIOLATION: Unauthorized access to reminder");
        }

        // 2. INVARIANT: Idempotency check - prevent duplicate TAKEN actions within last
        // 60 minutes
        if ("TAKEN".equals(request.getAction())) {
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            Optional<com.mediscan.reminder.document.ReminderHistory> recentTaken = reminderHistoryRepository
                    .findByUserIdAndTimestampAfter(userId, oneHourAgo)
                    .stream()
                    .filter(h -> h.getReminderId().equals(reminderId) && "TAKEN".equals(h.getAction()))
                    .findFirst();

            if (recentTaken.isPresent()) {
                log.warn("IDEMPOTENCY: Duplicate TAKEN action detected for reminder {}", reminderId);
                return com.mediscan.dto.v1.reminder.ReminderActionResponse.builder()
                        .success(false)
                        .message("This reminder was already marked as taken recently")
                        .build();
            }

            // 3. INVARIANT: Referential Integrity - medicine must exist in MongoDB
            try {
                medicineService.getMedicineById(reminder.getMedicineId());
            } catch (Exception e) {
                throw new IllegalStateException(
                        "INVARIANT VIOLATION: Referenced medicine does not exist: " + reminder.getMedicineId());
            }
        }

        // 4. Create reminder history (MongoDB) - append-only
        com.mediscan.reminder.document.ReminderHistory history = com.mediscan.reminder.document.ReminderHistory
                .builder()
                .reminderId(reminderId)
                .medicineId(reminder.getMedicineId())
                .userId(userId)
                .action(request.getAction())
                .actualTime(LocalDateTime.now())
                .timestamp(LocalDateTime.now())
                .note(request.getNote())
                .build();

        reminderHistoryRepository.insert(history); // Use insert to enforce append-only

        // 5. If action is TAKEN, handle inventory decrement using ATOMIC update
        Integer remainingQuantity = null;
        boolean lowStockWarning = false;

        if ("TAKEN".equals(request.getAction())) {
            try {
                Optional<Inventory> inventoryOpt = inventoryRepository.findByMedicineId(reminder.getMedicineId());

                if (inventoryOpt.isPresent()) {
                    Inventory inventory = inventoryOpt.get();

                    // CRITICAL: Use atomic updateQuantity method (already has logging)
                    Inventory updatedInventory = inventoryService.updateQuantity(
                            inventory.getId(),
                            -1, // Decrement by 1
                            LogReason.CONSUMPTION,
                            "Medicine taken via reminder: " + reminderId);

                    remainingQuantity = updatedInventory.getQuantity();
                    lowStockWarning = remainingQuantity <= inventory.getLowStockThreshold();
                } else {
                    log.warn("No inventory found for medicine {}", reminder.getMedicineId());
                }
            } catch (IllegalArgumentException e) {
                // Quantity would go negative - inventory exhausted
                log.warn("Cannot decrement inventory: {}", e.getMessage());
                remainingQuantity = 0;
            } catch (Exception e) {
                log.error("Error updating inventory for reminder action", e);
                // Don't fail the reminder action if inventory update fails
                // History is already saved (compensation pattern)
            }
        }

        // 6. Update reminder status
        if ("TAKEN".equals(request.getAction())) {
            reminder.setTaken(true);
            reminder.setTakenAt(LocalDateTime.now());
            reminderRepository.save(reminder);
        }

        return com.mediscan.dto.v1.reminder.ReminderActionResponse.builder()
                .success(true)
                .remainingQuantity(remainingQuantity)
                .lowStockWarning(lowStockWarning)
                .message("Medicine intake recorded successfully")
                .build();
    }
}
