package com.mediscan.dto.v1.inventory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * ⚠️ FROZEN API CONTRACT - v1
 */
@Value
@Builder
@Jacksonized
@JsonPropertyOrder({ "medicineId", "quantity", "lowStockThreshold" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryRequestDTO {

    @NotBlank(message = "Medicine ID is required")
    String medicineId;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    Integer quantity;

    @NotNull(message = "Low Stock Threshold is required")
    @Min(value = 0, message = "Threshold cannot be negative")
    Integer lowStockThreshold;
}
