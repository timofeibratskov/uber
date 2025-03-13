package com.example.ride_service.dto;

import com.example.ride_service.enums.RideStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideDto {
    private String id;
    private String pointA;
    private String pointB;
    private Long creatorId;
    private Byte seats;
    private Long driverId;
    private BigDecimal amount;
    private RideStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedIn;
    private Long passengerRatingId;
    private Long driverRatingId;
}