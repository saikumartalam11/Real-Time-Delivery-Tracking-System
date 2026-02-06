package com.example.notificationservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final Logger log = LoggerFactory.getLogger(NotificationController.class);

    @PostMapping
    public ResponseEntity<?> send(@RequestBody Map<String, Object> body) {
        log.info("Notification: {}", body);
        return ResponseEntity.ok(Map.of("status", "sent"));
    }
}
