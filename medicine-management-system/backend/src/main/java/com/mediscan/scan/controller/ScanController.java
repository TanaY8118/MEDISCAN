package com.mediscan.scan.controller;

import com.mediscan.dto.v1.scan.ScanRequestDTO;
import com.mediscan.dto.v1.scan.ScannedMedicineDTO;
import com.mediscan.scan.service.ScanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Scan API Controller - v1
 *
 * All endpoints use FROZEN v1 DTOs for mobile stability.
 */
@RestController
@RequestMapping("/api/v1/scan")
@RequiredArgsConstructor
@Slf4j
public class ScanController {

    private final ScanService scanService;

    @PostMapping
    public ResponseEntity<ScannedMedicineDTO> scanMedicine(@RequestBody @Valid ScanRequestDTO request) {
        log.info("Received scan request from mobile: type={}", request.getInputType());

        try {
            ScannedMedicineDTO result = scanService.processScan(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error processing scan request", e);

            // Return fallback response for mobile graceful degradation
            ScannedMedicineDTO fallback = ScannedMedicineDTO.builder()
                    .medicineId("error-fallback")
                    .name("Error - Try Manual Entry")
                    .dosage("N/A")
                    .manufacturer("N/A")
                    .confidence(0.0)
                    .build();

            return ResponseEntity.status(500).body(fallback);
        }
    }

    @GetMapping("/test-modes")
    public ResponseEntity<Map<String, String>> getTestModes() {
        log.info("Mobile development test modes requested");
        return ResponseEntity.ok(scanService.getTestModes());
    }

    @PostMapping("/simulate/{scenario}")
    public ResponseEntity<ScannedMedicineDTO> simulateScenario(@PathVariable String scenario) {
        log.info("Simulating mobile test scenario: {}", scenario);

        return switch (scenario.toUpperCase()) {
            case "SUCCESS" -> ResponseEntity.ok(
                    ScannedMedicineDTO.builder()
                            .medicineId("test-success")
                            .name("Test Medicine")
                            .dosage("100 mg")
                            .manufacturer("Test Pharma")
                            .confidence(0.95)
                            .build());
            case "UNKNOWN" -> ResponseEntity.ok(
                    ScannedMedicineDTO.builder()
                            .medicineId("test-unknown")
                            .name("Unknown Medicine")
                            .dosage("N/A")
                            .manufacturer("N/A")
                            .confidence(0.0)
                            .build());
            case "TIMEOUT" -> {
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                yield ResponseEntity.status(408).build();
            }
            default -> ResponseEntity.badRequest().build();
        };
    }
}
