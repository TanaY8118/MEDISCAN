package com.mediscan.dto.v1.inventory;

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
@JsonPropertyOrder({ "id", "medicineId", "quantity", "lowStockThreshold", "updatedAt" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryResponseDTO {
    Long id;
    String medicineId;
    Integer quantity;
    Integer lowStockThreshold;
    LocalDateTime updatedAt;
}
