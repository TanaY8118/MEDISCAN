package com.mediscan.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediscan.dto.v1.auth.LoginRequestDTO;
import com.mediscan.dto.v1.auth.RegisterRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Shared authentication fixture helper for integration tests.
 *
 * <p>
 * Registers two canonical test users (userA and userB) and caches their
 * JWT Bearer tokens. Call {@link #reset()} before each test to clear cached
 * state — token caches are re-populated lazily on next access.
 *
 * <p>
 * Design rule: this helper never mocks the security layer. Tokens are
 * obtained by making real HTTP calls through the full filter chain, so the
 * tokens that tests use are indistinguishable from production tokens.
 */
@Component
public class AuthTestHelper {

    private static final String USERNAME_A = "testUserA";
    private static final String USERNAME_B = "testUserB";
    private static final String EMAIL_A = "userA@mediscan.test";
    private static final String EMAIL_B = "userB@mediscan.test";
    private static final String PASSWORD = "TestPass@123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String tokenA;
    private String tokenB;
    private String refreshTokenA;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Returns "Bearer <jwt>" for userA, registering/logging in if needed. */
    public String bearerA() {
        if (tokenA == null)
            ensureUserA();
        return "Bearer " + tokenA;
    }

    /** Returns "Bearer <jwt>" for userB, registering/logging in if needed. */
    public String bearerB() {
        if (tokenB == null)
            ensureUserB();
        return "Bearer " + tokenB;
    }

    public String usernameA() {
        return USERNAME_A;
    }

    public String usernameB() {
        return USERNAME_B;
    }

    public String passwordA() {
        return PASSWORD;
    }

    /** Raw refresh token for userA (used by refresh-token tests). */
    public String refreshTokenA() {
        if (refreshTokenA == null)
            ensureUserA();
        return refreshTokenA;
    }

    /**
     * Clears all cached tokens. Mongo collections and JPA data are handled by
     * the test transaction rollback / Flapdoodle restart — this only clears the
     * in-memory state of this helper so tokens are re-acquired in the next test.
     */
    public void reset() {
        tokenA = null;
        tokenB = null;
        refreshTokenA = null;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void ensureUserA() {
        String[] tokens = registerOrLogin(USERNAME_A, EMAIL_A, PASSWORD);
        tokenA = tokens[0];
        refreshTokenA = tokens[1];
    }

    private void ensureUserB() {
        String[] tokens = registerOrLogin(USERNAME_B, EMAIL_B, PASSWORD);
        tokenB = tokens[0];
    }

    /**
     * Attempts registration; falls back to login if the user already exists
     * (e.g., because the JPA transaction from a previous test was not rolled
     * back for some embedded-DB reason).
     *
     * @return String[]{accessToken, refreshToken}
     */
    private String[] registerOrLogin(String username, String email, String password) {
        try {
            RegisterRequestDTO req = RegisterRequestDTO.builder()
                    .username(username)
                    .email(email)
                    .password(password)
                    .build();

            MvcResult result = mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andReturn();

            int status = result.getResponse().getStatus();
            if (status == 200) {
                return extractTokens(result);
            }
            // 409 = already exists — fall through to login
        } catch (Exception e) {
            // unexpected — try login
        }
        return login(username, password);
    }

    private String[] login(String username, String password) {
        try {
            LoginRequestDTO req = LoginRequestDTO.builder()
                    .username(username)
                    .password(password)
                    .build();

            MvcResult result = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andReturn();

            return extractTokens(result);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to authenticate test user: " + username, e);
        }
    }

    private String[] extractTokens(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(body);
        String access = node.path("token").asText();
        String refresh = node.path("refreshToken").asText("");
        return new String[] { access, refresh };
    }
}
