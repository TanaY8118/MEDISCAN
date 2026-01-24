package com.mediscan.medicine.service;

import com.mediscan.dto.v1.medicine.MedicineRequestDTO;
import com.mediscan.dto.v1.medicine.MedicineResponseDTO;
import com.mediscan.medicine.document.Medicine;
import com.mediscan.medicine.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;

    public List<MedicineResponseDTO> getAllMedicines() {
        return medicineRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public MedicineResponseDTO createMedicine(MedicineRequestDTO request) {
        if (medicineRepository.existsByBarcode(request.getBarcode())) {
            throw new RuntimeException("Medicine with this barcode already exists");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Medicine medicine = Medicine.builder()
                .name(request.getName())
                .description(request.getDescription())
                .barcode(request.getBarcode())
                .createdBy(username)
                .build();

        return toResponse(medicineRepository.save(medicine));
    }

    public MedicineResponseDTO getMedicineById(String id) {
        return toResponse(medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found")));
    }

    public MedicineResponseDTO getMedicineByBarcode(String barcode) {
        return toResponse(medicineRepository.findByBarcode(barcode)
                .orElseThrow(() -> new RuntimeException("Medicine not found")));
    }

    private MedicineResponseDTO toResponse(Medicine medicine) {
        return MedicineResponseDTO.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .description(medicine.getDescription())
                .barcode(medicine.getBarcode())
                .createdAt(medicine.getCreatedAt())
                .build();
    }

    /**
     * Get medicine name by ID (returns primitive for cross-module safety)
     * 
     * @param id Medicine unique identifier
     * @return Medicine name as String
     * @throws RuntimeException if medicine not found
     */
    public String getMedicineNameById(String id) {
        return medicineRepository.findById(id)
                .map(Medicine::getName)
                .orElseThrow(() -> new RuntimeException("Medicine not found: " + id));
    }
}
