package com.mediscan.dto.v1.reminder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * ⚠️ FROZEN API CONTRACT - v1
 * 
 * Do NOT modify field names, types, or ordering.
 * Any breaking changes require a new version (v2).
 * 
 * Idempotency Key: (reminderId, action, timestamp)
 * Server enforces 60-minute idempotency window for TAKEN actions.
 * 
 * Forward compatible: Accepts unknown fields from future versions.
 */
@Value
@Builder
@Jacksonized
@JsonPropertyOrder({ "action", "note" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReminderActionRequest {

    @NotBlank(message = "Action is required")
    @Pattern(regexp = "TAKEN|SKIPPED|MISSED|DELAYED", message = "Action must be TAKEN, SKIPPED, MISSED, or DELAYED")
    String action;

    String note;
}
