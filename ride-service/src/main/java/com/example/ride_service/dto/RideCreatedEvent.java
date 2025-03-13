package com.example.ride_service.dto;

import lombok.Setter;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.ToString;

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
}
