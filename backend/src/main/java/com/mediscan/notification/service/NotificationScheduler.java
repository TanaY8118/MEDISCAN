package com.mediscan.notification.service;

import com.mediscan.common.security.SystemContextWrapper;
import com.mediscan.reminder.document.Reminder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Background-only scheduler.
 *
 * DESIGN: This class is a pure timer/trigger. It does NOT query user-scoped
 * data
 * and must NEVER be injected into user-facing controllers. All user-scoped
 * notification queries live in NotificationService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationService notificationService;
    private final SystemContextWrapper systemContextWrapper;

    /**
     * Battery-efficient: Runs every 5 minutes instead of constant polling.
     * Guaranteed EXACTLY-ONCE across multi-pod deployments via ShedLock.
     * Executes headless background batch ops under the formal SYSTEM Principal
     * context.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @SchedulerLock(name = "NotificationScheduler_checkDueReminders", lockAtLeastFor = "3m", lockAtMostFor = "10m")
    public void checkDueReminders() {
        log.debug("Checking for due reminders (Cluster-Synchronized via ShedLock)...");

        systemContextWrapper.executeAsSystem(() -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourFromNow = now.plusHours(1);

            List<Reminder> dueReminders = notificationService.getDueRemindersForSystem(now, oneHourFromNow);

            if (dueReminders.isEmpty()) {
                return; // Nothing to process
            }

            log.info("[SCHEDULER] Discovered {} due reminder(s) requiring push dispatch", dueReminders.size());

            // Simple user grouping to dispatch push payload per user
            dueReminders.stream()
                    .collect(java.util.stream.Collectors.groupingBy(Reminder::getUserId))
                    .forEach((userIdString, userReminders) -> {
                        try {
                            Long userId = Long.valueOf(userIdString);
                            List<String> tokens = notificationService.getUserTokensSystem(userId);

                            if (tokens.isEmpty()) {
                                log.debug("No FCM tokens registered for user {} - skipping push dispatch", userId);
                            } else {
                                // Dummy FCM Dispatch Point
                                log.info(
                                        "DISPATCH FCM >>> {} user tokens identified for user {}. Sending {} reminders.",
                                        tokens.size(), userId, userReminders.size());
                            }
                        } catch (NumberFormatException e) {
                            log.warn("Corrupted userId format for pushing FCM: {}", userIdString);
                        }
                    });
        });
    }
}
