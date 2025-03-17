package com.example.ride_service.controller;

import com.example.ride_service.dto.RideDto;
import com.example.ride_service.dto.RideRequestDto;
import com.example.ride_service.enums.RideStatus;
import com.example.ride_service.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
@Tag(name = "Ride Management", description = "APIs for managing rides in the ride-sharing application")
public class RideController {
    private final RideService rideService;

    @PostMapping
    @Operation(
            summary = "Create a new ride",
            description = "Create a new ride with the provided details",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ride created successfully",
                            content = @Content(schema = @Schema(implementation = RideStatus.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input provided"
                    )
            }
    )
    public ResponseEntity<RideStatus> createRide(@Valid @RequestBody RideRequestDto request) {
        return ResponseEntity.ok(rideService.createRide(request));
    }

    @GetMapping("/passenger/{id}")
    @Operation(
            summary = "Get rides by passenger ID",
            description = "Retrieve all rides associated with a specific passenger",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Rides retrieved successfully",
                            content = @Content(schema = @Schema(implementation = RideDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Passenger not found"
                    )
            }
    )
    public List<RideDto> getPassengerRides(
            @Parameter(description = "ID of the passenger", required = true, example = "1")
            @PathVariable Long id
    ) {
        return rideService.getRidesByPassengerId(id);
    }

    @GetMapping("/ride/{rideId}")
    @Operation(
            summary = "Get ride by ID",
            description = "Retrieve a ride by its unique ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ride retrieved successfully",
                            content = @Content(schema = @Schema(implementation = RideDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Ride not found"
                    )
            }
    )
    public RideDto getRideById(
            @Parameter(description = "ID of the ride", required = true, example = "65a1f2b3c4d5e6f7g8h9i0j")
            @PathVariable String rideId
    ) {
        return rideService.findRideById(rideId);
    }

    @GetMapping("/driver/{userId}")
    @Operation(
            summary = "Get rides by driver ID",
            description = "Retrieve all rides associated with a specific driver",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Rides retrieved successfully",
                            content = @Content(schema = @Schema(implementation = RideDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Driver not found"
                    )
            }
    )
    public List<RideDto> getDriverRides(
            @Parameter(description = "ID of the driver", required = true, example = "1")
            @PathVariable Long userId
    ) {
        return rideService.getRidesByDriverId(userId);
    }

    @GetMapping("/status/{status}")
    @Operation(
            summary = "Get rides by status",
            description = "Retrieve all rides with a specific status",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Rides retrieved successfully",
                            content = @Content(schema = @Schema(implementation = RideDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid status provided"
                    )
            }
    )
    public List<RideDto> getRidesByStatus(
            @Parameter(description = "Status of the rides", required = true, example = "IN_PROGRESS")
            @PathVariable RideStatus status
    ) {
        return rideService.getRidesByStatus(status);
    }

    @PutMapping("/{rideId}/assign-driver")
    @Operation(
            summary = "Assign a driver to a ride",
            description = "Assign a driver to a specific ride by ride ID and driver ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Driver assigned successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Ride or driver not found"
                    )
            }
    )
    public void assignDriver(
            @Parameter(description = "ID of the ride", required = true, example = "65a1f2b3c4d5e6f7g8h9i0j")
            @PathVariable String rideId,
            @Parameter(description = "ID of the driver", required = true, example = "1")
            @RequestParam Long driverId
    ) {
        rideService.assignDriver(rideId, driverId);
    }

    @PutMapping("/{rideId}/pay")
    @Operation(
            summary = "Process ride payment",
            description = "Confirm payment for a specific ride with specified amount",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Payment processed successfully",
                            content = @Content(schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input or insufficient funds"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Ride not found"
                    )
            }
    )
    public String payRide(
            @Parameter(
                    description = "ID of the ride to pay for",
                    required = true,
                    example = "65a1f2b3c4d5e6f7g8h9i0j"
            )
            @PathVariable String rideId,

            @Parameter(
                    description = "Payment amount in decimal format",
                    required = true,
                    example = "15.75"
            )
            @RequestParam
            @Positive(message = "Amount must be positive")
            BigDecimal amount
    ) {
        return rideService.payRide(rideId, RideStatus.PAID, amount);
    }

    @PutMapping("/{rideId}/start")
    @Operation(
            summary = "Start a ride",
            description = "Change the status of a ride to IN_PROGRESS",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ride started successfully",
                            content = @Content(schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Ride not found"
                    )
            }
    )
    public String startRide(
            @Parameter(description = "ID of the ride", required = true, example = "65a1f2b3c4d5e6f7g8h9i0j")
            @PathVariable String rideId
    ) {
        return rideService.changeStatus(rideId, RideStatus.IN_PROGRESS);
    }

    @PutMapping("/{rideId}/complete")
    @Operation(
            summary = "Complete a ride",
            description = "Change the status of a ride to COMPLETED",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ride completed successfully",
                            content = @Content(schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Ride not found"
                    )
            }
    )
    public String completeRide(
            @Parameter(description = "ID of the ride", required = true, example = "65a1f2b3c4d5e6f7g8h9i0j")
            @PathVariable String rideId
    ) {
        return rideService.changeStatus(rideId, RideStatus.COMPLETED);
    }

    @PatchMapping("/{rideId}/cancel")
    @Operation(
            summary = "Cancel a ride",
            description = "Change the status of a ride to CANCELLED",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ride cancelled successfully",
                            content = @Content(schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Ride not found"
                    )
            }
    )
    public String cancelRide(
            @Parameter(description = "ID of the ride", required = true, example = "65a1f2b3c4d5e6f7g8h9i0j")
            @PathVariable String rideId
    ) {
        return rideService.changeStatus(rideId, RideStatus.CANCELLED);
    }
}