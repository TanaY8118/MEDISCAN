package com.mediscan.history.controller;

import com.mediscan.dto.v1.history.HistoryResponseDTO;
import com.mediscan.dto.v1.history.TimelineEventDTO;
import com.mediscan.dto.v1.history.TimelineResponse;
import com.mediscan.history.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * History API Controller - v1
 * 
 * Timeline endpoint uses FROZEN v1 DTOs for mobile stability.
 */
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping("/{medicineId}")
    public ResponseEntity<List<HistoryResponseDTO>> getHistory(@PathVariable String medicineId) {
        return ResponseEntity.ok(historyService.getHistory(medicineId));
    }

    @GetMapping("/timeline")
    public ResponseEntity<TimelineResponse> getTimeline(
            @RequestParam(defaultValue = "7") int days) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(historyService.getTimeline(userId, days));
    }

}
