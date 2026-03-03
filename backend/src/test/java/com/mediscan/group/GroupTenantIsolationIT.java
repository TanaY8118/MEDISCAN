package com.mediscan.group;

import com.fasterxml.jackson.databind.JsonNode;
import com.mediscan.BaseIntegrationTest;
import com.mediscan.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tenant isolation tests for the Group module.
 *
 * Invariants validated:
 * - GET /api/groups returns only the authenticated user's own groups
 * - Duplicate group name for same user → 409 Conflict
 * - UserB's group list is empty when userA has groups (no cross-leakage)
 */
@DisplayName("Group Tenant Isolation Tests")
class GroupTenantIsolationIT extends BaseIntegrationTest {

    @Autowired
    private TestDataFactory factory;

    private String tokenA;
    private String tokenB;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        tokenA = authHelper.bearerA();
        tokenB = authHelper.bearerB();
    }

    // -------------------------------------------------------------------------
    // Own-data happy paths
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/groups — userA creates group, returns 200")
    void createGroup_userA_returns200() throws Exception {
        mockMvc.perform(post("/api/groups")
                .header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"FamilyGroup\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("FamilyGroup"));
    }

    @Test
    @DisplayName("POST /api/groups — duplicate name for same user returns 409")
    void createGroup_duplicateName_returns409() throws Exception {
        factory.createGroup(tokenA, "MyMeds");

        mockMvc.perform(post("/api/groups")
                .header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"MyMeds\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/groups — userA sees only own groups")
    void getGroups_userA_seesOwnOnly() throws Exception {
        factory.createGroup(tokenA, "GroupA1");
        factory.createGroup(tokenA, "GroupA2");
        factory.createGroup(tokenB, "GroupB1");

        MvcResult result = mockMvc.perform(get("/api/groups")
                .header("Authorization", tokenA))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode arr = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(arr.isArray()).isTrue();
        for (JsonNode g : arr) {
            assertThat(g.path("name").asText()).isNotEqualTo("GroupB1");
        }
    }

    // -------------------------------------------------------------------------
    // CROSS-TENANT: isolation assertions
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CROSS-TENANT: GET /api/groups with userB token — returns empty list when only userA has groups")
    void getGroups_userBToken_doesNotIncludeUserAGroups() throws Exception {
        factory.createGroup(tokenA, "OnlyUserAGroup");

        MvcResult result = mockMvc.perform(get("/api/groups")
                .header("Authorization", tokenB))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode arr = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(arr.isArray()).isTrue();
        // UserB should see ZERO groups — no cross-tenant leakage
        assertThat(arr.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("Duplicate group name is allowed across different users")
    void createGroup_sameNameDifferentUsers_both200() throws Exception {
        mockMvc.perform(post("/api/groups")
                .header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"SharedName\"}"))
                .andExpect(status().isOk());

        // UserB can create a group with the same name — no conflict
        mockMvc.perform(post("/api/groups")
                .header("Authorization", tokenB)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"SharedName\"}"))
                .andExpect(status().isOk());
    }
}
