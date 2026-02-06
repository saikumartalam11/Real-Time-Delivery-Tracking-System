package com.example.trackingservice.controller;

import com.example.trackingservice.ws.TrackingWebSocketHandler;
import com.example.trackingservice.service.LocationCacheService;
import com.example.trackingservice.service.EtaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/tracking")
public class TrackingController {
    private final RedisTemplate<String, Object> redisTemplate;
    private final TrackingWebSocketHandler wsHandler;
    private final RestTemplate restTemplate;
    private final LocationCacheService locationCacheService;
    private final EtaService etaService;

    @Autowired
    public TrackingController(RedisTemplate<String, Object> redisTemplate, TrackingWebSocketHandler wsHandler, RestTemplate restTemplate, LocationCacheService locationCacheService, EtaService etaService) {
        this.redisTemplate = redisTemplate;
        this.wsHandler = wsHandler;
        this.restTemplate = restTemplate;
        this.locationCacheService = locationCacheService;
        this.etaService = etaService;
    }

    @PostMapping("/update/{orderId}")
    public ResponseEntity<?> updateLocation(@PathVariable String orderId, @RequestBody Map<String, Object> body) {
        // body contains partnerId, lat, lng
        redisTemplate.opsForHash().putAll("order:" + orderId, body);
        try {
            if (body.containsKey("partnerId") && body.containsKey("lat") && body.containsKey("lng")) {
                Long partnerId = Long.parseLong(body.get("partnerId").toString());
                double lat = Double.parseDouble(body.get("lat").toString());
                double lng = Double.parseDouble(body.get("lng").toString());
                locationCacheService.savePartnerLocation(partnerId, lat, lng);
            }
        } catch (Exception ignored) {}

        // Try to fetch order dropoff coordinates to compute ETA
        try {
            String url = String.format("http://order-service:9001/orders/%s", orderId);
            Map order = restTemplate.getForObject(url, Map.class);
            if (order != null && order.containsKey("dropoffLat") && order.containsKey("dropoffLng")) {
                double dropLat = Double.parseDouble(order.get("dropoffLat").toString());
                double dropLng = Double.parseDouble(order.get("dropoffLng").toString());
                double lat = Double.parseDouble(body.get("lat").toString());
                double lng = Double.parseDouble(body.get("lng").toString());
                long etaMinutes = etaService.estimateEtaMinutes(lat, lng, dropLat, dropLng, 30.0);
                body.put("eta_minutes", etaMinutes);

                // send a notification about ETA
                try {
                    restTemplate.postForObject("http://notification-service:9004/notifications", Map.of(
                            "orderId", orderId,
                            "type", "ETA",
                            "eta_minutes", etaMinutes
                    ), Map.class);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}

        wsHandler.sendUpdate(orderId, Map.of("type", "location", "data", body));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping("/latest/{orderId}")
    public ResponseEntity<?> latest(@PathVariable String orderId) {
        var map = redisTemplate.opsForHash().entries("order:" + orderId);
        return ResponseEntity.ok(map);
    }

    
}
