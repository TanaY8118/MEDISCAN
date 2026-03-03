package com.mediscan.dto.v1.history;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ⚠️ FROZEN API CONTRACT - v1
 * 
 * Do NOT modify field names, types, or ordering.
 * Any breaking changes require a new version (v2).
 */
@Value
@Builder
@Jacksonized
@JsonPropertyOrder({ "timestamp", "eventType", "medicineName", "description", "metadata" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimelineEventDTO {

    LocalDateTime timestamp;
    String eventType; // REMINDER_TAKEN, STOCK_CHANGED, MEDICINE_ADDED
    String medicineName;
    String description;
    Map<String, Object> metadata;
}
