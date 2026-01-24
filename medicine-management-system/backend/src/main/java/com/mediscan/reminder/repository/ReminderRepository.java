package com.mediscan.reminder.repository;

import com.mediscan.reminder.document.Reminder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReminderRepository extends MongoRepository<Reminder, String> {
    List<Reminder> findByUserId(String userId);
    List<Reminder> findByUserIdAndIsTakenFalse(String userId);
}
