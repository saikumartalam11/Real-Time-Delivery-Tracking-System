package com.example.orderservice.service;

import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.model.OrderEntity;
import com.example.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository repo;
    private final RestTemplate restTemplate;

    public OrderService(OrderRepository repo, RestTemplate restTemplate) {
        this.repo = repo;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest req) {
        var e = new OrderEntity();
        e.setCustomerName(req.getCustomerName());
        e.setPickupLat(req.getPickupLat());
        e.setPickupLng(req.getPickupLng());
        e.setDropoffLat(req.getDropoffLat());
        e.setDropoffLng(req.getDropoffLng());
        e.setStatus("CREATED");
        e.setCreatedAt(Instant.now());
        var saved = repo.save(e);
        return toDto(saved);
    }

    public Optional<OrderResponse> getById(Long id) {
        return repo.findById(id).map(this::toDto);
    }

    @Transactional
    public Optional<OrderResponse> assignPartner(Long orderId, Long partnerId) {
        var opt = repo.findById(orderId);
        if (opt.isEmpty()) return Optional.empty();
        var order = opt.get();
        order.setAssignedPartnerId(partnerId);
        order.setStatus("ASSIGNED");
        repo.save(order);

        // notify notification-service (best effort)
        try {
            restTemplate.postForObject("http://notification-service:9004/notifications", java.util.Map.of(
                    "orderId", order.getId(),
                    "type", "ASSIGNED",
                    "partnerId", partnerId
            ), Object.class);
        } catch (Exception ignored) {}

        return Optional.of(toDto(order));
    }

    private OrderResponse toDto(OrderEntity e) {
        var r = new OrderResponse();
        r.setId(e.getId());
        r.setCustomerName(e.getCustomerName());
        r.setPickupLat(e.getPickupLat());
        r.setPickupLng(e.getPickupLng());
        r.setDropoffLat(e.getDropoffLat());
        r.setDropoffLng(e.getDropoffLng());
        r.setStatus(e.getStatus());
        r.setAssignedPartnerId(e.getAssignedPartnerId());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }
}
