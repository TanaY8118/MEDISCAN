package com.mediscan.dto.v1.group;

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
@JsonPropertyOrder({ "id", "name", "description", "ownerEmail", "memberCount", "createdAt" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupResponseDTO {
    Long id;
    String name;
    String description;
    String ownerEmail;
    Integer memberCount;
    LocalDateTime createdAt;
}
