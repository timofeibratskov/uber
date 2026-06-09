package com.example.payment_service.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "ride-service")
public interface RideServiceClient {
    @GetMapping("/api/v1/rides/{rideId}/canPay")
    ResponseEntity<Boolean> canPayRide(@PathVariable UUID rideId);
}
