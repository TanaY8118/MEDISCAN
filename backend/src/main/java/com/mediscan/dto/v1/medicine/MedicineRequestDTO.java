package com.mediscan.dto.v1.medicine;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * ⚠️ FROZEN API CONTRACT - v1
 */
@Value
@Builder
@Jacksonized
@JsonPropertyOrder({ "name", "description" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicineRequestDTO {

    @NotBlank(message = "Name is required")
    String name;

    String description;
}
