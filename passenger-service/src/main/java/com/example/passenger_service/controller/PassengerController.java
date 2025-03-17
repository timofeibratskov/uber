package com.example.passenger_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import com.example.passenger_service.dto.PassengerDto;
import com.example.passenger_service.dto.PassengerRequest;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.passenger_service.service.PassengerService;
import com.example.passenger_service.dto.LoginPassengerRequest;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/passengers")
@Tag(name = "Passenger Management", description = "APIs for managing passengers")
public class PassengerController {

    private final PassengerService passengerService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new passenger",
            description = "Registers a new passenger and returns a confirmation message",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Passenger registered successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input provided")
            }
    )
    public ResponseEntity<String> registerPassenger(
            @RequestBody @Valid PassengerRequest request) {
        return ResponseEntity.status(201).body(passengerService.registerPassenger(request));
    }

    @GetMapping("/all")
    @Operation(
            summary = "Get all passengers",
            description = "Retrieves a list of all registered passengers",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Passengers retrieved successfully")
            }
    )
    public ResponseEntity<List<PassengerDto>> getAllPassengers() {
        List<PassengerDto> passengers = passengerService.findAllPassengers();
        return ResponseEntity.ok(passengers);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get passenger by ID",
            description = "Retrieves a passenger based on their ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Passenger retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Passenger not found")
            }
    )
    public ResponseEntity<PassengerDto> getPassenger(
            @Parameter(description = "ID of the passenger", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(passengerService.findPassenger(id));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login passenger",
            description = "Authenticates a passenger and returns passenger details",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Passenger logged in successfully"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            }
    )
    public ResponseEntity<PassengerDto> loginPassenger(
            @RequestBody LoginPassengerRequest request) {
        return ResponseEntity.status(200).body(passengerService.loginPassenger(request));
    }

    @PutMapping("/update/{id}")
    @Operation(
            summary = "Update passenger details",
            description = "Updates an existing passenger's details",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Passenger updated successfully"),
                    @ApiResponse(responseCode = "404", description = "Passenger not found")
            }
    )
    public ResponseEntity<String> updatePassenger(
            @Parameter(description = "ID of the passenger", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody @Valid PassengerRequest request) {
        return ResponseEntity.ok(passengerService.updatePassenger(id, request));
    }

    @DeleteMapping("/delete/{id}")
    @Operation(
            summary = "Delete passenger",
            description = "Deletes a passenger by their ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Passenger deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Passenger not found")
            }
    )
    public ResponseEntity<Void> deletePassenger(
            @Parameter(description = "ID of the passenger", required = true, example = "1")
            @PathVariable Long id) {
        passengerService.deletePassenger(id);
        return ResponseEntity.noContent().build();
    }
}
