package com.mediscan.inventory.controller;

import com.mediscan.inventory.dto.InventoryRequest;
import com.mediscan.inventory.dto.InventoryResponse;
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
    public ResponseEntity<List<InventoryResponse>> getAllInventories() {
        return ResponseEntity.ok(inventoryService.getAllInventories());
    }

    @PostMapping
    public ResponseEntity<InventoryResponse> updateInventory(
            @RequestBody @Valid InventoryRequest request) {
        return ResponseEntity.ok(inventoryService.addOrUpdateInventory(request));
    }

    @GetMapping("/{medicineId}")
    public ResponseEntity<InventoryResponse> getInventory(@PathVariable String medicineId) {
        return ResponseEntity.ok(inventoryService.getInventoryByMedicineId(medicineId));
    }

    @PostMapping("/{medicineId}/adjust")
    public ResponseEntity<InventoryResponse> adjustStock(@PathVariable String medicineId,
            @RequestParam int amount) {
        return ResponseEntity.ok(inventoryService.adjustStock(medicineId, amount));
    }
}
