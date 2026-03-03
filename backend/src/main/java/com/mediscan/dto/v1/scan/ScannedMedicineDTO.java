package com.mediscan.dto.v1.scan;

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
@JsonPropertyOrder({ "medicineId", "name", "description", "dosage", "manufacturer", "confidence" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScannedMedicineDTO {

    String medicineId;
    String name;
    String description;
    String dosage;
    String manufacturer;
    Double confidence;
}
