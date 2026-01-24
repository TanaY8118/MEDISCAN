package com.mediscan.dto.v1.scan;

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
 * Forward compatible: Accepts unknown fields from future versions.
 */
@Value
@Builder
@Jacksonized
@JsonPropertyOrder({ "inputType", "barcodeValue", "imageBase64" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScanRequestDTO {

    @NotBlank(message = "Input type is required")
    @Pattern(regexp = "BARCODE|IMAGE", message = "Input type must be BARCODE or IMAGE")
    String inputType;

    String barcodeValue;

    String imageBase64;
}
