package com.mediscan.dto.v1.reminder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@JsonPropertyOrder({ "medicineId", "reminderTime", "frequency", "note" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReminderRequestDTO {

    @NotBlank(message = "Medicine ID is required")
    String medicineId;

    @NotNull(message = "Reminder Time is required")
    @Future(message = "Reminder time must be in the future")
    LocalDateTime reminderTime;

    String frequency;
    String note;
}
