package com.example.ride_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class RideCreatedEvent {
    private String id;
    private String pointA;
    private String pointB;
    private Long creatorId;
    private Byte seats;
    private LocalDateTime time;
}
