package com.mediscan.dto.v1.medicine;

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
@JsonPropertyOrder({ "id", "name", "description", "createdAt" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicineResponseDTO {
    String id;
    String name;
    String description;
    LocalDateTime createdAt;
}
