package com.mediscan.dto.v1.auth;

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
@JsonPropertyOrder({ "token", "refreshToken" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponseDTO {
    String token;
    String refreshToken;
}
