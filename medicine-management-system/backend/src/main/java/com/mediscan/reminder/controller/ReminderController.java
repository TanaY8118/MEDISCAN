package com.mediscan.reminder.controller;

import com.mediscan.dto.v1.reminder.DueReminderDTO;
import com.mediscan.dto.v1.reminder.DueRemindersResponse;
import com.mediscan.dto.v1.reminder.ReminderActionRequest;
import com.mediscan.dto.v1.reminder.ReminderActionResponse;
import com.mediscan.dto.v1.reminder.ReminderRequestDTO;
import com.mediscan.dto.v1.reminder.ReminderResponseDTO;
import com.mediscan.notification.service.NotificationScheduler;
import com.mediscan.reminder.service.ReminderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Reminder API Controller - v1
 *
 * Action endpoints use FROZEN v1 DTOs for mobile stability.
 */
@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;
    private final NotificationScheduler notificationScheduler;

    @PostMapping
    public ResponseEntity<ReminderResponseDTO> createReminder(
            @RequestBody @Valid ReminderRequestDTO request) {
        return ResponseEntity.ok(reminderService.createReminder(request));
    }

    @GetMapping
    public ResponseEntity<List<ReminderResponseDTO>> getMyReminders() {
        return ResponseEntity.ok(reminderService.getMyReminders());
    }

    @GetMapping("/due")
    public ResponseEntity<DueRemindersResponse> getDueReminders() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        List<DueReminderDTO> reminders = notificationScheduler.getDueReminders(userId);
        List<com.mediscan.dto.v1.notification.StockAlertDTO> alerts = notificationScheduler.checkLowStockAlerts(userId);

        DueRemindersResponse response = DueRemindersResponse
                .builder()
                .reminders(reminders)
                .stockAlerts(alerts)
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/taken")
    public ResponseEntity<ReminderResponseDTO> markAsTaken(@PathVariable String id) {
        return ResponseEntity.ok(reminderService.markAsTaken(id));
    }

    @PostMapping("/{id}/actions")
    public ResponseEntity<ReminderActionResponse> recordAction(
            @PathVariable String id,
            @RequestBody @Valid ReminderActionRequest request) {
        return ResponseEntity.ok(reminderService.recordAction(id, request));
    }
}
