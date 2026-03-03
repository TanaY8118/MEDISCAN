package com.mediscan.dto.v1.notification;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * ⚠️ FROZEN API CONTRACT - v1
 * 
 * Do NOT modify field names, types, or ordering.
 * Any breaking changes require a new version (v2).
 */
@Value
@Builder
@Jacksonized
@JsonPropertyOrder({ "inventoryId", "medicineId", "medicineName", "currentQuantity", "threshold", "severity" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockAlertDTO {

    Long inventoryId;
    String medicineId;
    String medicineName;
    Integer currentQuantity;
    Integer threshold;
    String severity; // WARNING or CRITICAL
}
