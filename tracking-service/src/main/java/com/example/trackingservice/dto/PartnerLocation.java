package com.example.trackingservice.dto;

import java.time.Instant;

public class PartnerLocation {
    private Long partnerId;
    private double lat;
    private double lng;
    private Instant timestamp;

    public PartnerLocation() {}

    public PartnerLocation(Long partnerId, double lat, double lng, Instant timestamp) {
        this.partnerId = partnerId;
        this.lat = lat;
        this.lng = lng;
        this.timestamp = timestamp;
    }

    public Long getPartnerId() { return partnerId; }
    public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }
    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
