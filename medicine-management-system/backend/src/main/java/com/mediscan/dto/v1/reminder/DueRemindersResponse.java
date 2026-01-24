package com.mediscan.dto.v1.reminder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.mediscan.dto.v1.notification.StockAlertDTO;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * ⚠️ FROZEN API CONTRACT - v1
 * 
 * Do NOT modify field names, types, or ordering.
 * Any breaking changes require a new version (v2).
 */
@Value
@Builder
@Jacksonized
@JsonPropertyOrder({ "reminders", "stockAlerts" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class DueRemindersResponse {

    List<DueReminderDTO> reminders;
    List<StockAlertDTO> stockAlerts;
}
