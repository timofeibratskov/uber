package com.example.passenger_service.controller;

import com.example.passenger_service.dto.LoginPassengerRequest;
import com.example.passenger_service.dto.PassengerDto;
import com.example.passenger_service.dto.PassengerRequest;
import com.example.passenger_service.service.PassengerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/passengers")
public class PassengerController {

    private final PassengerService passengerService;

    @PostMapping("/register")
    public ResponseEntity<String> registerPassenger(
            @RequestBody @Valid PassengerRequest request) {
        return ResponseEntity.status(201).body(passengerService.registerPassenger(request));
    }

    @GetMapping("/all")
    public ResponseEntity<List<PassengerDto>> getAllPassengers() {
        List<PassengerDto> passengers = passengerService.findAllPassengers();
        return ResponseEntity.ok(passengers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassengerDto> getPassengers(@PathVariable Long id) {
        return ResponseEntity.ok(passengerService.findPassenger(id));
    }

    @PostMapping("/login")
    public ResponseEntity<PassengerDto> loginPassenger(@RequestBody LoginPassengerRequest request) {
        return ResponseEntity.status(200).body(passengerService.loginPassenger(request));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updatePassenger(@PathVariable Long id, @RequestBody PassengerRequest request) {
        return ResponseEntity.ok(passengerService.updatePassenger(id, request));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletePassenger(@PathVariable Long id) {
        passengerService.deletePassenger(id);
        return ResponseEntity.noContent().build();
    }
}