package com.mediscan.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediscan.dto.v1.inventory.InventoryRequestDTO;
import com.mediscan.dto.v1.medicine.MedicineRequestDTO;
import com.mediscan.dto.v1.reminder.ReminderRequestDTO;
import com.mediscan.dto.v1.group.GroupRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Seed helper that creates domain objects in the running application on behalf
 * of a specific user (identified by their Bearer token).
 *
 * <p>
 * All methods go through the full HTTP stack (MockMvc) so ownership is set
 * exactly as it would be in production — via the JWT principal resolved by the
 * real {@code JwtFilter}.
 */
@Component
public class TestDataFactory {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // Medicine
    // -------------------------------------------------------------------------

    /**
     * Creates a medicine for the user identified by {@code bearerToken}.
     * 
     * @return the MongoDB {@code id} of the created medicine document.
     */
    public String createMedicine(String bearerToken, String name) {
        try {
            MedicineRequestDTO req = MedicineRequestDTO.builder()
                    .name(name)
                    .description("Test medicine: " + name)
                    .build();

            MvcResult result = mockMvc.perform(post("/api/medicines")
                    .header("Authorization", bearerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
            return node.path("id").asText();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create medicine '" + name + "'", e);
        }
    }

    // -------------------------------------------------------------------------
    // Inventory
    // -------------------------------------------------------------------------

    /**
     * Creates or updates an inventory record for the given medicine on behalf of
     * the user identified by {@code bearerToken}.
     * 
     * @return the JPA {@code id} of the created inventory record (as String).
     */
    public String createInventory(String bearerToken, String medicineId, int quantity) {
        try {
            InventoryRequestDTO req = InventoryRequestDTO.builder()
                    .medicineId(medicineId)
                    .quantity(quantity)
                    .lowStockThreshold(5)
                    .build();

            MvcResult result = mockMvc.perform(post("/api/inventories")
                    .header("Authorization", bearerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
            return node.path("id").asText();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create inventory for medicine " + medicineId, e);
        }
    }

    // -------------------------------------------------------------------------
    // Reminder
    // -------------------------------------------------------------------------

    /**
     * Creates a reminder for the given medicine on behalf of the user
     * identified by {@code bearerToken}.
     * 
     * @return the MongoDB {@code id} of the created reminder document.
     */
    public String createReminder(String bearerToken, String medicineId) {
        try {
            ReminderRequestDTO req = ReminderRequestDTO.builder()
                    .medicineId(medicineId)
                    .reminderTime(LocalDateTime.now().plusDays(1))
                    .frequency("DAILY")
                    .note("Test reminder")
                    .build();

            MvcResult result = mockMvc.perform(post("/api/reminders")
                    .header("Authorization", bearerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
            return node.path("id").asText();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create reminder for medicine " + medicineId, e);
        }
    }

    // -------------------------------------------------------------------------
    // Group
    // -------------------------------------------------------------------------

    /**
     * Creates a group on behalf of the user identified by {@code bearerToken}.
     * 
     * @return the JPA {@code id} of the created group (as String).
     */
    public String createGroup(String bearerToken, String groupName) {
        try {
            GroupRequestDTO req = GroupRequestDTO.builder()
                    .name(groupName)
                    .build();

            MvcResult result = mockMvc.perform(post("/api/groups")
                    .header("Authorization", bearerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
            return node.path("id").asText();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create group '" + groupName + "'", e);
        }
    }
}
