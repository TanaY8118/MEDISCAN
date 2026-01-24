package com.mediscan.medicine.controller;

import com.mediscan.dto.v1.medicine.MedicineRequestDTO;
import com.mediscan.dto.v1.medicine.MedicineResponseDTO;
import com.mediscan.medicine.service.MedicineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping
    public ResponseEntity<List<MedicineResponseDTO>> getAllMedicines() {
        return ResponseEntity.ok(medicineService.getAllMedicines());
    }

    @PostMapping
    public ResponseEntity<MedicineResponseDTO> createMedicine(
            @RequestBody @Valid MedicineRequestDTO request) {
        return ResponseEntity.ok(medicineService.createMedicine(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicineResponseDTO> getMedicineById(@PathVariable String id) {
        return ResponseEntity.ok(medicineService.getMedicineById(id));
    }

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<MedicineResponseDTO> getMedicineByBarcode(
            @PathVariable String barcode) {
        return ResponseEntity.ok(medicineService.getMedicineByBarcode(barcode));
    }
}
