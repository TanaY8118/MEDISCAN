package com.mediscan.reminder.repository;

import com.mediscan.reminder.document.Reminder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends MongoRepository<Reminder, String> {
    List<Reminder> findByUserId(String userId);

    List<Reminder> findByUserIdAndIsTakenFalse(String userId);

    /**
     * SYSTEM-ONLY: Returns aggregate count of due, untaken reminders across all
     * users.
     * Used exclusively by NotificationScheduler for operational logging — no user
     * data is surfaced.
     */
    long countByReminderTimeBetweenAndIsTakenFalse(LocalDateTime start, LocalDateTime end);

    /**
     * SYSTEM-ONLY: Fetches all due, untaken reminders across all users.
     * Must be guarded by ROLE_SYSTEM in the service layer.
     */
    List<Reminder> findByReminderTimeBetweenAndIsTakenFalse(LocalDateTime start, LocalDateTime end);
}
