package com.example.trackingservice.service;

import org.springframework.stereotype.Service;

@Service
public class EtaService {
    private static final int EARTH_RADIUS_KM = 6371;

    public double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Estimate ETA in minutes given two coordinates and average speed in km/h.
     * If averageSpeedKmH is zero or negative, defaults to 30 km/h.
     */
    public long estimateEtaMinutes(double fromLat, double fromLng, double toLat, double toLng, double averageSpeedKmH) {
        if (averageSpeedKmH <= 0) averageSpeedKmH = 30.0;
        double dist = distanceKm(fromLat, fromLng, toLat, toLng);
        double hours = dist / averageSpeedKmH;
        return (long) Math.max(1, Math.round(hours * 60));
    }
}
