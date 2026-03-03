package com.mediscan.dto.v1.history;

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
@JsonPropertyOrder({ "id", "medicineId", "medicineName", "action", "details", "performedBy", "timestamp" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoryResponseDTO {
    String id;
    String medicineId;
    String medicineName;
    String action;
    String details;
    String performedBy;
    LocalDateTime timestamp;
}
