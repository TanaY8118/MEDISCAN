package com.mediscan.dto.v1.reminder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

/**
 * ⚠️ FROZEN API CONTRACT - v1
 */
@Value
@Builder
@Jacksonized
@JsonPropertyOrder({ "id", "medicineId", "medicineName", "reminderTime", "frequency", "isTaken", "takenAt", "note" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReminderResponseDTO {
    String id;
    String medicineId;
    String medicineName;
    LocalDateTime reminderTime;
    String frequency;
    boolean isTaken;
    LocalDateTime takenAt;
    String note;
}
