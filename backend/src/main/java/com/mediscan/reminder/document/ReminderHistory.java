package com.mediscan.reminder.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reminder_history")
public class ReminderHistory {

    @Id
    private String id;

    @Indexed
    private String reminderId;

    private String medicineId;

    @Indexed
    private String userId;

    private String action; // TAKEN, SKIPPED, MISSED, DELAYED

    private LocalDateTime actualTime;

    @Indexed
    private LocalDateTime timestamp;

    private String note;
}
