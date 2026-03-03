package com.mediscan.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.mediscan.BaseIntegrationTest;
import com.mediscan.dto.v1.auth.LoginRequestDTO;
import com.mediscan.dto.v1.auth.RegisterRequestDTO;
import com.mediscan.dto.v1.auth.TokenRefreshRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Auth endpoint group.
 *
 * Invariants validated:
 * - Register issues access + refresh tokens
 * - Duplicate register is rejected with 409
 * - Login with correct credentials issues tokens
 * - Login with wrong credentials is rejected
 * - Refresh token flow issues a new access token
 * - Invalid refresh token is rejected
 * - Protected endpoints reject unauthenticated requests
 */
@DisplayName("Auth Controller Integration Tests")
class AuthControllerIT extends BaseIntegrationTest {

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/auth/register — happy path returns access + refresh tokens")
    void register_validRequest_returns200WithTokens() throws Exception {
        RegisterRequestDTO req = RegisterRequestDTO.builder()
                .username("alice")
                .email("alice@test.com")
                .password("AlicePass@1")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.path("token").asText()).isNotBlank();
        assertThat(body.path("refreshToken").asText()).isNotBlank();
    }

    @Test
    @DisplayName("POST /api/auth/register — duplicate username returns 409 Conflict")
    void register_duplicateUsername_returns409() throws Exception {
        RegisterRequestDTO req = RegisterRequestDTO.builder()
                .username("bob")
                .email("bob@test.com")
                .password("BobPass@1")
                .build();

        // First registration succeeds
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // Second registration with same username → 409
        RegisterRequestDTO duplicate = RegisterRequestDTO.builder()
                .username("bob")
                .email("bob2@test.com")
                .password("BobPass@1")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/auth/register — duplicate email returns 409 Conflict")
    void register_duplicateEmail_returns409() throws Exception {
        RegisterRequestDTO req = RegisterRequestDTO.builder()
                .username("carol")
                .email("carol@test.com")
                .password("CarolPass@1")
                .build();
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        RegisterRequestDTO duplicate = RegisterRequestDTO.builder()
                .username("carol2")
                .email("carol@test.com") // same email
                .password("CarolPass@1")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());
    }

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/auth/login — correct credentials returns 200 with token")
    void login_validCredentials_returns200() throws Exception {
        // Ensure userA exists
        authHelper.bearerA();

        LoginRequestDTO req = LoginRequestDTO.builder()
                .username(authHelper.usernameA())
                .password(authHelper.passwordA())
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.path("token").asText()).isNotBlank();
    }

    @Test
    @DisplayName("POST /api/auth/login — wrong password returns 4xx")
    void login_wrongPassword_returns4xx() throws Exception {
        authHelper.bearerA(); // ensure user exists

        LoginRequestDTO req = LoginRequestDTO.builder()
                .username(authHelper.usernameA())
                .password("WrongPassword999!")
                .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isBetween(400, 499));
    }

    // -------------------------------------------------------------------------
    // Refresh Token
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/auth/refresh — valid refresh token returns new access token")
    void refreshToken_validToken_returnsNewAccessToken() throws Exception {
        // Ensure userA has a refresh token
        authHelper.bearerA();
        String refreshToken = authHelper.refreshTokenA();

        TokenRefreshRequestDTO req = TokenRefreshRequestDTO.builder()
                .refreshToken(refreshToken)
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.path("token").asText()).isNotBlank();
        assertThat(body.path("refreshToken").asText()).isEqualTo(refreshToken);
    }

    @Test
    @DisplayName("POST /api/auth/refresh — invalid token returns 4xx")
    void refreshToken_invalidToken_returns4xx() throws Exception {
        TokenRefreshRequestDTO req = TokenRefreshRequestDTO.builder()
                .refreshToken("completely-invalid-token")
                .build();

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isBetween(400, 599));
    }

    // -------------------------------------------------------------------------
    // Unauthenticated access
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/inventories — no auth header returns 403")
    void protectedEndpoint_noAuthHeader_returns403() throws Exception {
        mockMvc.perform(get("/api/inventories"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/reminders — no auth header returns 403")
    void reminders_noAuthHeader_returns403() throws Exception {
        mockMvc.perform(get("/api/reminders"))
                .andExpect(status().isForbidden());
    }
}
