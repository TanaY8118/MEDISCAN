package com.mediscan.dto.v1.group;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class GroupRequestDTO {

    @NotBlank(message = "Group name is required")
    @Size(max = 100, message = "Group name must be less than 100 characters")
    String name;

    @Size(max = 500, message = "Description must be less than 500 characters")
    String description;
}
