package com.example.ride_service.model.cache;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@RedisHash("RideEstimate")
public class RideEstimateCache {
    @Id
    private UUID passengerId;

    private double distanceKm;
    private long durationMin;
    private BigDecimal price;
    private String polyline;

    private String startAddress;
    private String stopAddress;
    private Point startPoint;
    private Point stopPoint;

    @TimeToLive
    private Long expiration;
}