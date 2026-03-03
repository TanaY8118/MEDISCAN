package com.mediscan.inventory.controller;

import com.mediscan.dto.v1.inventory.InventoryRequestDTO;
import com.mediscan.dto.v1.inventory.InventoryResponseDTO;
import com.mediscan.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<InventoryResponseDTO>> getAllInventories() {
        return ResponseEntity.ok(inventoryService.getAllInventories());
    }

    @PostMapping
    public ResponseEntity<InventoryResponseDTO> updateInventory(
            @RequestBody @Valid InventoryRequestDTO request) {
        return ResponseEntity.ok(inventoryService.addOrUpdateInventory(request));
    }

    @GetMapping("/{medicineId}")
    public ResponseEntity<InventoryResponseDTO> getInventory(@PathVariable String medicineId) {
        return ResponseEntity.ok(inventoryService.getInventoryByMedicineId(medicineId));
    }

    @PostMapping("/{medicineId}/adjust")
    public ResponseEntity<InventoryResponseDTO> adjustStock(@PathVariable String medicineId,
            @RequestParam int amount) {
        return ResponseEntity.ok(inventoryService.adjustStock(medicineId, amount));
    }
}
