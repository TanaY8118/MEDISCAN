package com.mediscan.notification.controller;

import com.mediscan.dto.v1.notification.DeviceTokenRequestDTO;
import com.mediscan.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationDeviceTokenController {

    private final NotificationService notificationService;

    @PostMapping("/tokens")
    public ResponseEntity<Void> registerToken(@Valid @RequestBody DeviceTokenRequestDTO request) {
        notificationService.registerDeviceToken(request.getToken());
        return ResponseEntity.ok().build();
    }
}
