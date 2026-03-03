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
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return medicineRepository.findByCreatedBy(username).stream()
                .map(this::toResponse)
                .toList();
    }

    public MedicineResponseDTO createMedicine(MedicineRequestDTO request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Medicine medicine = Medicine.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(username)
                .build();

        return toResponse(medicineRepository.save(medicine));
    }

    public MedicineResponseDTO getMedicineById(String id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new com.mediscan.common.exception.ResourceNotFoundException("Medicine not found"));

        if (!medicine.getCreatedBy().equals(username)) {
            throw new com.mediscan.common.exception.UnauthorizedAccessException("Unauthorized access to medicine");
        }

        return toResponse(medicine);
    }

    private MedicineResponseDTO toResponse(Medicine medicine) {
        return MedicineResponseDTO.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .description(medicine.getDescription())
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
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(
                        () -> new com.mediscan.common.exception.ResourceNotFoundException("Medicine not found: " + id));

        if (!medicine.getCreatedBy().equals(username)) {
            throw new com.mediscan.common.exception.UnauthorizedAccessException("Unauthorized access to medicine");
        }

        return medicine.getName();
    }
}
