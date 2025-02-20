package com.example.passenger_service.controller;

import com.example.passenger_service.dto.DeletePassengerDto;
import com.example.passenger_service.dto.RegisterPassengerDto;
import com.example.passenger_service.dto.PassengerDto;
import com.example.passenger_service.dto.LoginPassengerDto;
import com.example.passenger_service.service.PassengerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
@RestController
@RequestMapping("/api/passengers")
public class PassengerController {
    private final PassengerService passengerService;

    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }


    @PostMapping("/register")
    public ResponseEntity<RegisterPassengerDto> registerPassenger(@RequestBody RegisterPassengerDto passengerDto) {
        return ResponseEntity.status(201).body(passengerService.registerPassenger(passengerDto));
    }

    @GetMapping("/all")
    public ResponseEntity<List<PassengerDto>> getAllPassengers() {
        List<PassengerDto> passengers = passengerService.findAllPassengers();
        return ResponseEntity.ok(passengers);
    }

    @PostMapping("/login")
    public ResponseEntity<PassengerDto> loginPassenger(@RequestBody LoginPassengerDto loginPassengerDto) {
        return ResponseEntity.status(200).body(passengerService.loginPassenger(loginPassengerDto));
    }


    @DeleteMapping("/delete")
    public ResponseEntity<Void> deletePassenger(@RequestBody DeletePassengerDto deletePassengerDto) {
        passengerService.deletePassenger(deletePassengerDto);
        return ResponseEntity.noContent().build();
    }
}