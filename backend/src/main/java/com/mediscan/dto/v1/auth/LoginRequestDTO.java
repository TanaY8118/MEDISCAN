package com.mediscan.dto.v1.auth;

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
@JsonPropertyOrder({ "username", "password" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequestDTO {

    @NotBlank(message = "Username or Email is required")
    String username;

    @NotBlank(message = "Password is required")
    String password;
}
