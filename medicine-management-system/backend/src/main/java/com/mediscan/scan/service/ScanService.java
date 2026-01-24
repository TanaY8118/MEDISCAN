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

    private final Map<String, MockMedicine> barcodeLookup;

    public ScanService() {
        // Initialize with 10 common medicines for MVP
        this.barcodeLookup = Map.of(
                "123456789012", new MockMedicine("Aspirin", "100 mg", "TABLET"),
                "234567890123", new MockMedicine("Lisinopril", "10 mg", "TABLET"),
                "345678901234", new MockMedicine("Metformin", "500 mg", "TABLET"),
                "456789012345", new MockMedicine("Amlodipine", "5 mg", "TABLET"),
                "567890123456", new MockMedicine("Omeprazole", "20 mg", "CAPSULE"),
                "678901234567", new MockMedicine("Simvastatin", "40 mg", "TABLET"),
                "789012345678", new MockMedicine("Losartan", "50 mg", "TABLET"),
                "890123456789", new MockMedicine("Gabapentin", "300 mg", "CAPSULE"),
                "901234567890", new MockMedicine("Levothyroxine", "75 mcg", "TABLET"),
                "012345678901", new MockMedicine("Albuterol", "90 mcg", "INHALER"));
    }

    public ScannedMedicineDTO processScan(ScanRequestDTO request) {
        log.info("Mobile scan request: type={}", request.getInputType());

        return switch (request.getInputType().toUpperCase()) {
            case "BARCODE" -> processBarcode(request.getBarcodeValue());
            case "IMAGE" -> processImageMock(request.getImageBase64());
            default -> throw new IllegalArgumentException("Invalid input type: " + request.getInputType());
        };
    }

    private ScannedMedicineDTO processBarcode(String barcode) {
        if (barcode == null || barcode.isBlank()) {
            return createUnknownMedicine();
        }

        MockMedicine medicine = barcodeLookup.getOrDefault(barcode, MockMedicine.UNKNOWN);

        return ScannedMedicineDTO.builder()
                .medicineId("mock-" + UUID.randomUUID())
                .name(medicine.getName())
                .dosage(medicine.getDosage())
                .manufacturer("Mock Manufacturer")
                .confidence(medicine.equals(MockMedicine.UNKNOWN) ? 0.0 : 0.95)
                .build();
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
                "BARCODE_SCAN", "Simulates barcode lookup",
                "IMAGE_OCR", "Simulates image processing");
    }
}
