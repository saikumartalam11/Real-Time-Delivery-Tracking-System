package com.example.trackingservice.ws;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class TrackingWebSocketHandler {
    private final SimpMessagingTemplate template;

    public TrackingWebSocketHandler(SimpMessagingTemplate template) {
        this.template = template;
    }

    public void sendUpdate(String orderId, Object payload) {
        // broadcast to topic for the order
        String dest = "/topic/orders/" + orderId;
        template.convertAndSend(dest, payload);
    }
}
