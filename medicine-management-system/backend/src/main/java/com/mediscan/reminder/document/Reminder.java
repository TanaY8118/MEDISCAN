package com.mediscan.reminder.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reminders")
public class Reminder {

    @Id
    private String id;

    private String userId; // Link to MySQL User ID
    private String medicineId; // Link to MongoDB Medicine ID
    private String medicineName; // Denormalized for quick access

    private LocalDateTime reminderTime;
    private String frequency; // e.g., "DAILY", "WEEKLY", "ONCE"
    private boolean isTaken;
    private LocalDateTime takenAt;
    private String note;
}
