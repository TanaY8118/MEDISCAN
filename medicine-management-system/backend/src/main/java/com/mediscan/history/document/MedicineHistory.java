package com.mediscan.history.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "medicine_history")
public class MedicineHistory {

    @Id
    private String id;

    private String medicineId;
    private String medicineName;
    private String action; // e.g., "CREATED", "UPDATED", "STOCK_ADDED", "REMINDER_SET", "TAKEN"
    private String details;
    private String performedBy; // Username

    @CreatedDate
    private LocalDateTime timestamp;
}
