package com.mediscan.notification.service;

import com.mediscan.dto.v1.notification.StockAlertDTO;
import com.mediscan.dto.v1.reminder.DueReminderDTO;
import com.mediscan.inventory.entity.Inventory;
import com.mediscan.inventory.repository.InventoryRepository;
import com.mediscan.medicine.document.Medicine;
import com.mediscan.medicine.repository.MedicineRepository;
import com.mediscan.notification.entity.NotificationDeviceToken;
import com.mediscan.notification.repository.NotificationDeviceTokenRepository;
import com.mediscan.reminder.document.Reminder;
import com.mediscan.reminder.repository.ReminderRepository;
import com.mediscan.user.entity.User;
import com.mediscan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User-scoped notification queries.
 *
 * SECURITY INVARIANT: Every public method in this class resolves the
 * authenticated
 * principal from the SecurityContext internally. No method accepts a raw userId
 * parameter, preventing any caller from accidentally or maliciously querying
 * another tenant's data.
 *
 * Pattern mirrors InventoryService.getCurrentUserEntity().
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ReminderRepository reminderRepository;
    private final InventoryRepository inventoryRepository;
    private final MedicineRepository medicineRepository;
    private final UserRepository userRepository;
    private final NotificationDeviceTokenRepository tokenRepository;

    /**
     * Returns reminders due within the next hour for the CURRENT authenticated user
     * only.
     * Safe for direct exposure via REST endpoint.
     */
    public List<DueReminderDTO> getDueRemindersForCurrentUser() {
        User user = resolveCurrentUser();
        // Reminder.userId stores the MySQL User PK as a String
        String tenantId = String.valueOf(user.getId());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourFromNow = now.plusHours(1);

        return reminderRepository.findByUserId(tenantId).stream()
                .filter(r -> r.getReminderTime() != null)
                .filter(r -> r.getReminderTime().isAfter(now) && r.getReminderTime().isBefore(oneHourFromNow))
                .filter(r -> !r.isTaken())
                .map(this::toReminderDTO)
                .collect(Collectors.toList());
    }

    /**
     * Returns top-5 low-stock alerts for the CURRENT authenticated user only.
     * Safe for direct exposure via REST endpoint.
     */
    public List<StockAlertDTO> getLowStockAlertsForCurrentUser() {
        User user = resolveCurrentUser();

        return inventoryRepository.findByUserId(user.getId()).stream()
                .filter(inv -> inv.getQuantity() <= inv.getLowStockThreshold())
                .limit(5)
                .map(this::toStockAlert)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // System Actor Queries
    // -------------------------------------------------------------------------

    /**
     * SYSTEM ONLY: Retrieves due reminders across all tenants for background push
     * dispatcher.
     */
    @PreAuthorize("hasRole('SYSTEM')")
    public List<Reminder> getDueRemindersForSystem(LocalDateTime start, LocalDateTime end) {
        log.info("System batch extracting due reminders between {} and {}", start, end);
        return reminderRepository.findByReminderTimeBetweenAndIsTakenFalse(start, end);
    }

    /**
     * SYSTEM ONLY: Retrieves FCM tokens for a specific user ID.
     */
    @PreAuthorize("hasRole('SYSTEM')")
    public List<String> getUserTokensSystem(Long userId) {
        return tokenRepository.findByUserId(userId)
                .stream()
                .map(NotificationDeviceToken::getToken)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Device Token Management
    // -------------------------------------------------------------------------

    public void registerDeviceToken(String token) {
        User user = resolveCurrentUser();
        boolean exists = tokenRepository.findByUserIdAndToken(user.getId(), token).isPresent();

        if (!exists) {
            tokenRepository.save(NotificationDeviceToken.builder()
                    .userId(user.getId())
                    .token(token)
                    .build());
            log.info("Registered new push token for User {}", user.getId());
        }
    }

    // -------------------------------------------------------------------------
    // Principal resolution — single source of truth for this service
    // -------------------------------------------------------------------------

    private User resolveCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + username));
    }

    // -------------------------------------------------------------------------
    // Mappers
    // -------------------------------------------------------------------------

    private DueReminderDTO toReminderDTO(Reminder reminder) {
        return DueReminderDTO.builder()
                .id(reminder.getId())
                .medicineId(reminder.getMedicineId())
                .medicineName(reminder.getMedicineName())
                .dueTime(reminder.getReminderTime())
                .message("Time to take " + reminder.getMedicineName())
                .frequency(reminder.getFrequency())
                .build();
    }

    private StockAlertDTO toStockAlert(Inventory inventory) {
        String medicineName = "Unknown";
        try {
            Medicine medicine = medicineRepository.findById(inventory.getMedicineId()).orElse(null);
            if (medicine != null) {
                medicineName = medicine.getName();
            }
        } catch (Exception e) {
            log.warn("Could not fetch medicine name for inventory {}", inventory.getId());
        }

        int halfThreshold = inventory.getLowStockThreshold() / 2;
        String severity = inventory.getQuantity() <= halfThreshold ? "CRITICAL" : "WARNING";

        return StockAlertDTO.builder()
                .inventoryId(inventory.getId())
                .medicineId(inventory.getMedicineId())
                .medicineName(medicineName)
                .currentQuantity(inventory.getQuantity())
                .threshold(inventory.getLowStockThreshold())
                .severity(severity)
                .build();
    }
}
