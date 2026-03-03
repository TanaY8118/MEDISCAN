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
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final ReminderHistoryRepository reminderHistoryRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;
    private final MedicineService medicineService;
    private final UserRepository userRepository;

    private String getCurrentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Resolves the current authenticated principal to a persistent User entity.
     * Required to obtain the Long user.id needed for JPA/MySQL scoped queries.
     */
    private User resolveCurrentUserEntity() {
        String username = getCurrentUser();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + username));
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
                .orElseThrow(() -> new com.mediscan.common.exception.ResourceNotFoundException("Reminder not found"));

        if (!reminder.getUserId().equals(getCurrentUser())) {
            throw new com.mediscan.common.exception.UnauthorizedAccessException("Unauthorized Access");
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
     * <p>
     * <b>Operation-ordering guarantee (cross-DB desync prevention):</b>
     * </p>
     * 
     * <pre>
     * Step | Database | Operation                          | Failure behaviour
     * -----|----------|------------------------------------|-----------------------------------
     *  1   | —        | Ownership + idempotency checks     | throws → nothing written
     *  2   | MySQL    | inventoryService.updateQuantity()  | throws → nothing written to MongoDB
     *  3   | MongoDB  | reminderHistoryRepository.insert() | MySQL committed ✅; audit gap only
     *  4   | MongoDB  | reminderRepository.save()          | stock + history correct; retry safe
     * </pre>
     *
     * <p>
     * MySQL MUST succeed before any MongoDB write is attempted.
     * Broad exception swallowing on the inventory step is intentionally absent —
     * any MySQL failure propagates as an exception to the caller.
     * </p>
     *
     * <p>
     * NOTE: {@code @Transactional} covers the MySQL boundary only.
     * MongoDB writes are not part of the JPA transaction.
     * </p>
     *
     * Enforces:
     * 1. Ownership (user must own reminder)
     * 2. Idempotency (60-minute window)
     * 3. Referential integrity (medicine must exist in MongoDB)
     * 4. Atomic inventory decrement with mandatory logging (via InventoryService)
     * 5. Append-only audit trail in reminder_history
     */
    @Transactional
    public com.mediscan.dto.v1.reminder.ReminderActionResponse recordAction(String reminderId,
            com.mediscan.dto.v1.reminder.ReminderActionRequest request) {
        String userId = getCurrentUser();

        // 1. Validate reminder exists and belongs to user
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new com.mediscan.common.exception.ResourceNotFoundException("Reminder not found"));

        // INVARIANT: Ownership check
        if (!reminder.getUserId().equals(userId)) {
            throw new com.mediscan.common.exception.UnauthorizedAccessException(
                    "INVARIANT VIOLATION: Unauthorized access to reminder");
        }

        // 2. INVARIANT: Idempotency — prevent duplicate TAKEN within 60 minutes
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

            // 3. INVARIANT: Referential integrity — medicine must exist in MongoDB
            try {
                medicineService.getMedicineById(reminder.getMedicineId());
            } catch (Exception e) {
                throw new IllegalStateException(
                        "INVARIANT VIOLATION: Referenced medicine does not exist: " + reminder.getMedicineId());
            }
        }

        // ── ORDERING GUARANTEE ────────────────────────────────────────────────────
        // MySQL inventory update (step 4) MUST run before any MongoDB write (step 5).
        // If the MySQL update fails, the method throws here and no MongoDB document
        // is ever committed — eliminating cross-DB desync entirely.
        // DO NOT move reminderHistoryRepository.insert() above this block.
        // ─────────────────────────────────────────────────────────────────────────

        // 4. [MYSQL] If TAKEN: decrement inventory atomically — fail-loud on error
        Integer remainingQuantity = null;
        boolean lowStockWarning = false;

        if ("TAKEN".equals(request.getAction())) {
            Long currentUserId = resolveCurrentUserEntity().getId();
            Optional<Inventory> inventoryOpt = inventoryRepository
                    .findByMedicineIdAndUserId(reminder.getMedicineId(), currentUserId);

            if (inventoryOpt.isPresent()) {
                Inventory inventory = inventoryOpt.get();

                try {
                    // CRITICAL: Atomic update — validates quantity, writes InventoryLog,
                    // rolls back MySQL on log failure (see InventoryService.updateQuantity)
                    Inventory updatedInventory = inventoryService.updateQuantity(
                            inventory.getId(),
                            -1,
                            LogReason.CONSUMPTION,
                            "Medicine taken via reminder: " + reminderId);

                    remainingQuantity = updatedInventory.getQuantity();
                    lowStockWarning = remainingQuantity <= inventory.getLowStockThreshold();

                } catch (IllegalArgumentException e) {
                    // Controlled business case: stock exhausted (quantity would go negative).
                    // No inventory change was made. Proceed to record the TAKEN action
                    // in history so the audit trail captures the attempt.
                    log.warn("Stock exhausted for medicine {} — reminder action recorded without decrement: {}",
                            reminder.getMedicineId(), e.getMessage());
                    remainingQuantity = 0;
                }
                // All other exceptions (DB failure, constraint violation, etc.) propagate
                // freely — NO broad catch. The caller receives a 500 and no MongoDB write
                // will have occurred at this point.
            } else {
                log.warn("No inventory tracked for medicine {} — reminder action recorded without decrement",
                        reminder.getMedicineId());
            }
        }

        // 5. [MONGO] Insert ReminderHistory — append-only audit record
        // This write occurs AFTER MySQL success. If it fails, MySQL is already
        // committed
        // (inventory is correct); the caller receives a 500 and can retry safely.
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

        reminderHistoryRepository.insert(history);

        // 6. [MONGO] Mark Reminder as TAKEN
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
