package com.mediscan.reminder.repository;

import com.mediscan.reminder.document.ReminderHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SYSTEM INVARIANT: reminder_history is APPEND-ONLY
 * Updates and deletes are forbidden to maintain audit trail integrity
 */
@Repository
public interface ReminderHistoryRepository extends MongoRepository<ReminderHistory, String> {

    List<ReminderHistory> findByUserIdAndTimestampAfter(String userId, LocalDateTime timestamp);

    List<ReminderHistory> findByReminderIdOrderByTimestampDesc(String reminderId);

    // For idempotency check: find if this reminder was already marked as TAKEN
    // recently
    Optional<ReminderHistory> findFirstByReminderIdAndActionOrderByTimestampDesc(String reminderId, String action);

    // INVARIANT ENFORCEMENT: Override save to prevent updates
    @Override
    default <S extends ReminderHistory> S save(S entity) {
        if (entity.getId() != null) {
            throw new UnsupportedOperationException(
                    "INVARIANT VIOLATION: reminder_history is append-only. Updates are forbidden. " +
                            "Existing record ID: " + entity.getId());
        }
        // Delegate to insert for new records
        return insert(entity);
    }

    // INVARIANT ENFORCEMENT: Block all deletes
    @Override
    default void delete(ReminderHistory entity) {
        throw new UnsupportedOperationException(
                "INVARIANT VIOLATION: reminder_history is append-only. Deletion is forbidden.");
    }

    @Override
    default void deleteById(String id) {
        throw new UnsupportedOperationException(
                "INVARIANT VIOLATION: reminder_history is append-only. Deletion is forbidden.");
    }

    @Override
    default void deleteAll() {
        throw new UnsupportedOperationException(
                "INVARIANT VIOLATION: reminder_history is append-only. Deletion is forbidden.");
    }
}
