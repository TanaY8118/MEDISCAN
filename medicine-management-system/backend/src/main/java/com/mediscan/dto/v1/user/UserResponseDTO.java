package com.mediscan.dto.v1.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * ⚠️ FROZEN API CONTRACT - v1
 */
@Value
@Builder
@Jacksonized
@JsonPropertyOrder({ "id", "username", "email", "role" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponseDTO {
    Long id;
    String username;
    String email;
    String role;
}
