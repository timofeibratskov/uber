package com.example.ride_service.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "rides")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideEntity {
    @Id
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
    private Long passengerRatingId; //айди рейтинга который отправил passenger
    private Long driverRatingId; //send driver
}

