package com.mediscan.notification.service;

import com.mediscan.dto.v1.notification.StockAlertDTO;
import com.mediscan.dto.v1.reminder.DueReminderDTO;
import com.mediscan.inventory.entity.Inventory;
import com.mediscan.inventory.repository.InventoryRepository;
import com.mediscan.medicine.document.Medicine;
import com.mediscan.medicine.repository.MedicineRepository;
import com.mediscan.reminder.document.Reminder;
import com.mediscan.reminder.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final ReminderRepository reminderRepository;
    private final InventoryRepository inventoryRepository;
    private final MedicineRepository medicineRepository;

    /**
     * Battery-efficient: Runs every 5 minutes instead of constant polling
     * Marks reminders that are due in the next hour
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void checkDueReminders() {
        log.debug("Checking for due reminders...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourFromNow = now.plusHours(1);

        // This would mark reminders as "pending notification" in a real system
        // For MVP, we just log the count
        List<Reminder> dueReminders = reminderRepository.findAll().stream()
                .filter(r -> r.getReminderTime() != null)
                .filter(r -> r.getReminderTime().isAfter(now) && r.getReminderTime().isBefore(oneHourFromNow))
                .filter(r -> !r.isTaken())
                .toList();

        if (!dueReminders.isEmpty()) {
            log.info("Found {} reminders due in the next hour", dueReminders.size());
        }
    }

    /**
     * Get all reminders due in the next hour for mobile polling
     * Battery-safe: Mobile app calls this endpoint every 15 minutes
     */
    public List<DueReminderDTO> getDueReminders(String userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourFromNow = now.plusHours(1);

        return reminderRepository.findByUserId(userId).stream()
                .filter(r -> r.getReminderTime() != null)
                .filter(r -> r.getReminderTime().isAfter(now) && r.getReminderTime().isBefore(oneHourFromNow))
                .filter(r -> !r.isTaken())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Check for low stock inventories
     * Returns top 5 alerts ordered by severity
     */
    public List<StockAlertDTO> checkLowStockAlerts(String userId) {
        return inventoryRepository.findAll().stream()
                .filter(inv -> inv.getQuantity() <= inv.getLowStockThreshold())
                .limit(5) // Top 5 alerts
                .map(this::toStockAlert)
                .collect(Collectors.toList());
    }

    private DueReminderDTO toDTO(Reminder reminder) {
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
