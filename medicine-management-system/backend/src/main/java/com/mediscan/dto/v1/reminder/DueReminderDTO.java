package com.mediscan.dto.v1.reminder;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;

/**
 * ⚠️ FROZEN API CONTRACT - v1
 * 
 * Do NOT modify field names, types, or ordering.
 * Any breaking changes require a new version (v2).
 */
@Value
@Builder
@Jacksonized
@JsonPropertyOrder({ "id", "medicineId", "medicineName", "dueTime", "message", "frequency" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class DueReminderDTO {

    String id;
    String medicineId;
    String medicineName;
    LocalDateTime dueTime;
    String message;
    String frequency;
}
