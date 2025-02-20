package com.example.passenger_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PassengerDto {
    private Long id;
    private String name;
    private String gmail;
    private String password;
    private String phoneNumber;
    private Long cardId;
    private Float rating;
    private Long listOfRidesId;

}
