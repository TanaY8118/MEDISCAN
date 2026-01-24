package com.mediscan.history.service;

import com.mediscan.dto.v1.history.HistoryResponseDTO;
import com.mediscan.dto.v1.history.TimelineEventDTO;
import com.mediscan.dto.v1.history.TimelineResponse;
import com.mediscan.history.document.MedicineHistory;
import com.mediscan.history.repository.HistoryRepository;
import com.mediscan.inventory.entity.InventoryLog;
import com.mediscan.inventory.repository.InventoryLogRepository;
import com.mediscan.medicine.document.Medicine;
import com.mediscan.medicine.repository.MedicineRepository;
import com.mediscan.reminder.document.ReminderHistory;
import com.mediscan.reminder.repository.ReminderHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.mediscan.common.exception.ResourceNotFoundException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final ReminderHistoryRepository reminderHistoryRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final com.mediscan.inventory.repository.InventoryRepository inventoryRepository;
    private final MedicineRepository medicineRepository;
    private final com.mediscan.medicine.service.MedicineService medicineService;

    @Async
    public void logAction(String medicineId, String action, String details, String performedBy) {
        MedicineHistory history = MedicineHistory.builder()
                .medicineId(medicineId)
                .action(action)
                .details(details)
                .performedBy(performedBy)
                .timestamp(LocalDateTime.now())
                .build();

        historyRepository.save(history);
    }

    public List<HistoryResponseDTO> getHistory(String medicineId) {
        return historyRepository.findByMedicineId(medicineId).stream()
                .map(this::toResponse)
                .toList();
    }

    private HistoryResponseDTO toResponse(MedicineHistory history) {
        return HistoryResponseDTO.builder()
                .id(history.getId())
                .medicineId(history.getMedicineId())
                .action(history.getAction())
                .details(history.getDetails())
                .performedBy(history.getPerformedBy())
                .timestamp(history.getTimestamp())
                .build();
    }

    /**
     * Aggregate timeline events from multiple sources
     * Uses v1 TimelineEventDTO for API consistency
     */
    public TimelineResponse getTimeline(String userId, int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);

        List<TimelineEventDTO> events = new ArrayList<>();

        // 1. Fetch reminder history
        List<ReminderHistory> reminderEvents = reminderHistoryRepository.findByUserIdAndTimestampAfter(userId, cutoff);
        events.addAll(reminderEvents.stream()
                .map(this::mapReminderToTimeline)
                .toList());

        // 2. Fetch inventory logs
        List<InventoryLog> inventoryLogs = inventoryLogRepository
                .findByPerformedByAndTimestampAfter(userId, cutoff);

        // Convert inventory logs to timeline events
        List<TimelineEventDTO> inventoryEvents = inventoryLogs.stream()
                .map(this::mapInventoryLogToTimeline)
                .toList();

        // Add to combined events list
        events.addAll(inventoryEvents);

        // 3. Sort all events by timestamp (descending - newest first)
        events.sort(Comparator.comparing(TimelineEventDTO::getTimestamp).reversed());

        // 4. Paginate (limit to 20 events)
        List<TimelineEventDTO> paginatedEvents = events.stream()
                .limit(20)
                .toList();

        return TimelineResponse.builder()
                .events(paginatedEvents)
                .totalEvents(events.size())
                .page(1)
                .pageSize(20)
                .build();
    }

    /**
     * Safely retrieve medicine name with fallback for deleted medicines
     * Prevents timeline from breaking if medicine is deleted
     * 
     * @param medicineId Medicine UUID from MongoDB
     * @return Medicine name or placeholder if not found
     */
    private String getMedicineNameSafe(String medicineId) {
        try {
            return medicineService.getMedicineNameById(medicineId);
        } catch (ResourceNotFoundException e) {
            // Medicine was deleted - show placeholder
            return "Unknown Medicine (Deleted)";
        } catch (Exception e) {
            // Other errors (e.g., MongoDB down) - log and show placeholder
            log.warn("Failed to fetch medicine name for ID: {}", medicineId, e);
            return "Unknown Medicine";
        }
    }

    private TimelineEventDTO mapReminderToTimeline(ReminderHistory history) {
        String medicineName = getMedicineNameSafe(history.getMedicineId());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("reminderId", history.getReminderId());
        if (history.getNote() != null) {
            metadata.put("note", history.getNote());
        }

        return TimelineEventDTO.builder()
                .timestamp(history.getTimestamp())
                .eventType("REMINDER_" + history.getAction())
                .medicineName(medicineName)
                .description(getActionDescription(history.getAction(), medicineName))
                .metadata(metadata)
                .build();
    }

    private String getActionDescription(String action, String medicineName) {
        return switch (action) {
            case "TAKEN" -> "Took " + medicineName;
            case "SKIPPED" -> "Skipped " + medicineName;
            case "MISSED" -> "Missed " + medicineName;
            case "DELAYED" -> "Delayed " + medicineName;
            default -> action + " - " + medicineName;
        };
    }

    /**
     * Convert InventoryLog to TimelineEventDTO
     * Maps inventory changes (consumption, addition, etc.) to timeline events
     * 
     * @param log Inventory log from MySQL
     * @return Timeline event DTO for mobile consumption
     */
    private TimelineEventDTO mapInventoryLogToTimeline(InventoryLog log) {
        // Safely get medicine name (handles missing medicine gracefully)
        String medicineName = getMedicineNameSafe(log.getInventory().getMedicineId());

        // Build metadata map with change details
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("reason", log.getReason().toString());
        metadata.put("changeAmount", log.getChangeAmount().intValue());
        metadata.put("quantityBefore", log.getQuantityBefore().intValue());
        metadata.put("quantityAfter", log.getQuantityAfter().intValue());
        metadata.put("unit", log.getInventory().getUnit());

        // Include note if present
        if (log.getNote() != null && !log.getNote().isEmpty()) {
            metadata.put("note", log.getNote());
        }

        return TimelineEventDTO.builder()
                .timestamp(log.getTimestamp())
                .eventType("STOCK_" + log.getReason().toString()) // e.g., "STOCK_CONSUMPTION"
                .medicineName(medicineName)
                .description(getStockDescription(log))
                .metadata(metadata)
                .build();
    }

    /**
     * Generate human-readable description for inventory change
     * Provides mobile-friendly text for timeline display
     * 
     * @param log Inventory log entry
     * @return Human-readable description (e.g., "Stock consumed: 1 tablet (99
     *         remaining)")
     */
    private String getStockDescription(InventoryLog log) {
        // Map reason enum to past-tense action verb
        String action = switch (log.getReason()) {
            case CONSUMPTION -> "consumed";
            case ADDITION -> "added";
            case EXPIRY -> "expired";
            case WASTAGE -> "wasted";
            case ADJUSTMENT -> "adjusted";
            case TRANSFER -> "transferred";
            default -> "changed";
        };

        int changeAmount = Math.abs(log.getChangeAmount().intValue());
        int remainingQuantity = log.getQuantityAfter().intValue();
        String unit = log.getInventory().getUnit();

        return String.format("Stock %s: %d %s (%d remaining)",
                action,
                changeAmount,
                unit,
                remainingQuantity);
    }
}
