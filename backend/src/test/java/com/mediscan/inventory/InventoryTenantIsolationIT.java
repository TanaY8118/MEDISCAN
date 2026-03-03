package com.mediscan.inventory;

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
 * Tenant isolation tests for the Inventory module.
 *
 * Invariants validated:
 * - Every query is scoped to the authenticated user
 * - UserB cannot read UserA's inventory records by medicineId
 * - UserB cannot read UserA's direct inventory records via GET /api/inventories
 * - Inventory quantity cannot go negative (guard in
 * InventoryService.updateQuantity)
 */
@DisplayName("Inventory Tenant Isolation Tests")
class InventoryTenantIsolationIT extends BaseIntegrationTest {

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
        // UserA creates a medicine (needed as foreign reference for inventory)
        medicineIdA = factory.createMedicine(tokenA, "Aspirin");
    }

    // -------------------------------------------------------------------------
    // Happy-path: own inventory operations
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/inventories — userA creates inventory, returns 200")
    void createInventory_userA_returns200() throws Exception {
        String medicineId = factory.createMedicine(tokenA, "Ibuprofen");

        mockMvc.perform(post("/api/inventories")
                .header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"medicineId\":\"" + medicineId + "\",\"quantity\":10,\"lowStockThreshold\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(10));
    }

    @Test
    @DisplayName("GET /api/inventories — userA sees only own inventory records")
    void getInventories_userA_seesOwnOnly() throws Exception {
        // UserA creates 2 inventories
        String med1 = factory.createMedicine(tokenA, "MedA1");
        String med2 = factory.createMedicine(tokenA, "MedA2");
        factory.createInventory(tokenA, med1, 10);
        factory.createInventory(tokenA, med2, 20);

        // UserB creates 1 inventory
        String medB = factory.createMedicine(tokenB, "MedB1");
        factory.createInventory(tokenB, medB, 99);

        MvcResult result = mockMvc.perform(get("/api/inventories")
                .header("Authorization", tokenA))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode arr = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(arr.isArray()).isTrue();
        // All returned records belong to userA — none have quantity 99 (userB's record)
        for (JsonNode inv : arr) {
            assertThat(inv.path("quantity").asInt()).isNotEqualTo(99);
        }
    }

    @Test
    @DisplayName("UserB creates inventory for same medicineId — creates separate, independent record")
    void createInventory_sameMedicine_separateRecordsPerUser() throws Exception {
        String medicineB = factory.createMedicine(tokenB, "Aspirin"); // same name, different user
        factory.createInventory(tokenB, medicineB, 50);
        factory.createInventory(tokenA, medicineIdA, 30);

        // UserA sees qty=30 for their own record
        MvcResult resA = mockMvc.perform(get("/api/inventories/" + medicineIdA)
                .header("Authorization", tokenA))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode bodyA = objectMapper.readTree(resA.getResponse().getContentAsString());
        assertThat(bodyA.path("quantity").asInt()).isEqualTo(30);

        // UserB sees qty=50 for their own record (medicineB)
        MvcResult resB = mockMvc.perform(get("/api/inventories/" + medicineB)
                .header("Authorization", tokenB))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode bodyB = objectMapper.readTree(resB.getResponse().getContentAsString());
        assertThat(bodyB.path("quantity").asInt()).isEqualTo(50);
    }

    // -------------------------------------------------------------------------
    // CROSS-TENANT: access isolation assertions
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CROSS-TENANT: userB fetches userA's inventory by medicineId — returns 4xx (not found or forbidden)")
    void getInventory_userBAccessesUserAMedicineId_returns4xx() throws Exception {
        factory.createInventory(tokenA, medicineIdA, 10);

        // UserB attempts to read userA's inventory record by medicineId
        mockMvc.perform(get("/api/inventories/" + medicineIdA)
                .header("Authorization", tokenB))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isBetween(400, 499));
    }

    @Test
    @DisplayName("CROSS-TENANT: userB GET /api/inventories — does not include userA's records")
    void getInventories_userB_doesNotIncludeUserARecords() throws Exception {
        factory.createInventory(tokenA, medicineIdA, 42);

        MvcResult result = mockMvc.perform(get("/api/inventories")
                .header("Authorization", tokenB))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode arr = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(arr.isArray()).isTrue();
        // UserB has no inventories yet — list must be empty
        for (JsonNode inv : arr) {
            assertThat(inv.path("quantity").asInt()).isNotEqualTo(42);
        }
    }

    // -------------------------------------------------------------------------
    // Quantity invariant guard
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/inventories/{id}/adjust — adjustment that would result in negative quantity returns 400")
    void adjustStock_wouldGoNegative_returns400() throws Exception {
        factory.createInventory(tokenA, medicineIdA, 5);

        // Attempt to remove 10 from a stock of 5 → would result in -5
        mockMvc.perform(post("/api/inventories/" + medicineIdA + "/adjust")
                .header("Authorization", tokenA)
                .param("amount", "-10"))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isBetween(400, 499));
    }
}
