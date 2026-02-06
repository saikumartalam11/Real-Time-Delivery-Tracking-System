package com.example.trackingservice.service;

import com.example.trackingservice.dto.PartnerLocation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class LocationCacheService {
    private final RedisTemplate<String, PartnerLocation> redisTemplate;
    private final long ttlSeconds;

    public LocationCacheService(RedisTemplate<String, PartnerLocation> redisTemplate,
                                @Value("${tracking.redis.ttl-seconds:120}") long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.ttlSeconds = ttlSeconds;
    }

    public void savePartnerLocation(Long partnerId, double lat, double lng) {
        PartnerLocation loc = new PartnerLocation(partnerId, lat, lng, Instant.now());
        String key = keyFor(partnerId);
        redisTemplate.opsForValue().set(key, loc, Duration.ofSeconds(ttlSeconds));
    }

    public PartnerLocation getPartnerLocation(Long partnerId) {
        return redisTemplate.opsForValue().get(keyFor(partnerId));
    }

    public void deletePartnerLocation(Long partnerId) {
        redisTemplate.delete(keyFor(partnerId));
    }

    private String keyFor(Long partnerId) { return "partner:" + partnerId; }
}
