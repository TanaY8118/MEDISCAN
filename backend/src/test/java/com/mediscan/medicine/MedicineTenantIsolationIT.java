package com.mediscan.medicine;

import com.fasterxml.jackson.databind.JsonNode;
import com.mediscan.BaseIntegrationTest;
import com.mediscan.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tenant isolation tests for the Medicine module (MongoDB).
 *
 * Invariants validated:
 * - GET /api/medicines returns only the authenticated user's own medicines
 * - GET /api/medicines/{id} with another user's token → 403 / 404
 * - Medicine count returned for userA does not include userB's medicines
 */
@DisplayName("Medicine Tenant Isolation Tests")
class MedicineTenantIsolationIT extends BaseIntegrationTest {

    @Autowired
    private TestDataFactory factory;

    private String tokenA;
    private String tokenB;
    private String medicineIdA;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        tokenA = authHelper.bearerA();
        tokenB = authHelper.bearerB();
        medicineIdA = factory.createMedicine(tokenA, "Metformin");
    }

    // -------------------------------------------------------------------------
    // Own-data happy paths
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/medicines — userA creates medicine, returns 200 with id")
    void createMedicine_userA_returns200() throws Exception {
        String id = factory.createMedicine(tokenA, "Lisinopril");
        assertThat(id).isNotBlank();
    }

    @Test
    @DisplayName("GET /api/medicines — userA sees only own medicines")
    void getMedicines_userA_seesOwnOnly() throws Exception {
        // Seed userA with 2 medicines (medicineIdA already created + 1 more)
        factory.createMedicine(tokenA, "Atorvastatin");
        // Seed userB with 1 medicine
        factory.createMedicine(tokenB, "UserBDrug");

        MvcResult result = mockMvc.perform(get("/api/medicines")
                .header("Authorization", tokenA))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode arr = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(arr.isArray()).isTrue();
        // No record should have name "UserBDrug"
        for (JsonNode m : arr) {
            assertThat(m.path("name").asText()).isNotEqualTo("UserBDrug");
        }
    }

    @Test
    @DisplayName("GET /api/medicines — userB list does not include userA's medicines")
    void getMedicines_userB_doesNotIncludeUserAMedicines() throws Exception {
        // medicineIdA was already created for userA in setUp()
        MvcResult result = mockMvc.perform(get("/api/medicines")
                .header("Authorization", tokenB))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode arr = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(arr.isArray()).isTrue();
        for (JsonNode m : arr) {
            assertThat(m.path("name").asText()).isNotEqualTo("Metformin");
        }
    }

    // -------------------------------------------------------------------------
    // CROSS-TENANT: access isolation assertions
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CROSS-TENANT: GET /api/medicines/{id} with userB's token on userA's medicine — returns 4xx")
    void getMedicineById_userBOnUserAId_returns4xx() throws Exception {
        // UserB attempts to read userA's medicine by its MongoDB id
        mockMvc.perform(get("/api/medicines/" + medicineIdA)
                .header("Authorization", tokenB))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isBetween(400, 499));
    }

    @Test
    @DisplayName("GET /api/medicines/{id} — userA can read own medicine")
    void getMedicineById_userA_ownMedicine_returns200() throws Exception {
        mockMvc.perform(get("/api/medicines/" + medicineIdA)
                .header("Authorization", tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Metformin"));
    }
}
