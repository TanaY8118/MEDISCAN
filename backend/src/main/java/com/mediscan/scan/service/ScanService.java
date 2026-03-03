package com.mediscan.scan.service;

import com.mediscan.dto.v1.scan.ScanRequestDTO;
import com.mediscan.dto.v1.scan.ScannedMedicineDTO;
import com.mediscan.scan.model.MockMedicine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ScanService {

    public ScanService() {
        // Image-based scanning only - no barcode lookup needed
    }

    public ScannedMedicineDTO processScan(ScanRequestDTO request) {
        log.info("Mobile scan request: type={}", request.getInputType());

        if ("IMAGE".equalsIgnoreCase(request.getInputType())) {
            return processImageMock(request.getImageBase64());
        } else {
            throw new IllegalArgumentException("Invalid input type: " + request.getInputType());
        }
    }

    private ScannedMedicineDTO processImageMock(String imageData) {
        // Simulate OCR processing delay for mobile testing
        simulateProcessingDelay(250);

        return ScannedMedicineDTO.builder()
                .medicineId("mock-ocr-" + UUID.randomUUID())
                .name("Aspirin")
                .description("100mg Tablets")
                .dosage("100 mg")
                .manufacturer("Generic Pharma")
                .confidence(0.85)
                .build();
    }

    private ScannedMedicineDTO createUnknownMedicine() {
        return ScannedMedicineDTO.builder()
                .medicineId("unknown-" + UUID.randomUUID())
                .name("Unknown Medicine")
                .dosage("N/A")
                .manufacturer("N/A")
                .confidence(0.0)
                .build();
    }

    private void simulateProcessingDelay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Processing delay interrupted");
        }
    }

    public Map<String, String> getTestModes() {
        return Map.of(
                "SUCCESS", "Returns mock Aspirin data",
                "UNKNOWN", "Returns unknown medicine template",
                "IMAGE_OCR", "Simulates image processing");
    }
}
