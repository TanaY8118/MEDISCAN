package com.mediscan.dto.v1.reminder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * ⚠️ FROZEN API CONTRACT - v1
 * 
 * Do NOT modify field names, types, or ordering.
 * Any breaking changes require a new version (v2).
 */
@Value
@Builder
@Jacksonized
@JsonPropertyOrder({ "success", "remainingQuantity", "lowStockWarning", "message" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReminderActionResponse {

    boolean success;
    Integer remainingQuantity;
    boolean lowStockWarning;
    String message;
}
