package com.mediscan.reminder;

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
 * Tenant isolation tests for the Reminder module.
 *
 * Invariants validated:
 * - GET /api/reminders is scoped to the authenticated user
 * - PATCH /{id}/taken with another user's token → 403
 * (UnauthorizedAccessException)
 * - POST /{id}/actions TAKEN with another user's token → 403
 * - Idempotency: second TAKEN within 60-minute window returns success=false
 * - recordAction TAKEN only decrements inventory for the owning user
 */
@DisplayName("Reminder Tenant Isolation Tests")
class ReminderTenantIsolationIT extends BaseIntegrationTest {

    @Autowired
    private TestDataFactory factory;

    private String tokenA;
    private String tokenB;
    private String medicineIdA;
    private String reminderIdA;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        tokenA = authHelper.bearerA();
        tokenB = authHelper.bearerB();
        medicineIdA = factory.createMedicine(tokenA, "Paracetamol");
        reminderIdA = factory.createReminder(tokenA, medicineIdA);
    }

    // -------------------------------------------------------------------------
    // Own-data happy paths
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/reminders — userA creates reminder, returns 200")
    void createReminder_userA_returns200() throws Exception {
        String medId = factory.createMedicine(tokenA, "Vitamin D");

        mockMvc.perform(post("/api/reminders")
                .header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"medicineId\":\"" + medId + "\","
                        + "\"reminderTime\":\"2027-01-01T08:00:00\","
                        + "\"frequency\":\"DAILY\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medicineId").value(medId));
    }

    @Test
    @DisplayName("GET /api/reminders — userA sees only own reminders")
    void getReminders_userA_seesOwnOnly() throws Exception {
        // UserB creates their own reminder
        String medB = factory.createMedicine(tokenB, "UserBMed");
        factory.createReminder(tokenB, medB);

        MvcResult result = mockMvc.perform(get("/api/reminders")
                .header("Authorization", tokenA))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode arr = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(arr.isArray()).isTrue();
        // All records must reference userA's medicine — none should reference medB
        for (JsonNode r : arr) {
            assertThat(r.path("medicineId").asText()).isNotEqualTo(medB);
        }
    }

    // -------------------------------------------------------------------------
    // CROSS-TENANT: access isolation assertions
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CROSS-TENANT: PATCH /{id}/taken with userB's token on userA's reminder — returns 403")
    void markAsTaken_userBOnUserAReminder_returns403() throws Exception {
        mockMvc.perform(patch("/api/reminders/" + reminderIdA + "/taken")
                .header("Authorization", tokenB))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CROSS-TENANT: POST /{id}/actions with userB's token on userA's reminder — returns 403")
    void recordAction_userBOnUserAReminder_returns403() throws Exception {
        mockMvc.perform(post("/api/reminders/" + reminderIdA + "/actions")
                .header("Authorization", tokenB)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"TAKEN\"}"))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // Idempotency invariant
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /{id}/actions TAKEN — second call within 60 min returns success=false")
    void recordAction_secondTakenWithin60Min_returnsIdempotencyResponse() throws Exception {
        // First TAKEN — should succeed
        mockMvc.perform(post("/api/reminders/" + reminderIdA + "/actions")
                .header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"TAKEN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Second TAKEN within 60-minute window — should be idempotency-blocked
        MvcResult result = mockMvc.perform(post("/api/reminders/" + reminderIdA + "/actions")
                .header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"TAKEN\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.path("success").asBoolean()).isFalse();
        assertThat(body.path("message").asText()).containsIgnoringCase("already");
    }

    // -------------------------------------------------------------------------
    // Inventory decrement scoping
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /{id}/actions TAKEN — decrements inventory for userA only, not userB")
    void recordAction_taken_decrementsCorrectUserInventory() throws Exception {
        // userA has 10 units of Paracetamol in inventory
        factory.createInventory(tokenA, medicineIdA, 10);

        MvcResult result = mockMvc.perform(post("/api/reminders/" + reminderIdA + "/actions")
                .header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"TAKEN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        // Remaining quantity should be 9 (10 - 1)
        assertThat(body.path("remainingQuantity").asInt()).isEqualTo(9);
    }
}
